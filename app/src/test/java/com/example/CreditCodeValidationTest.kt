package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.config.CreditConfig
import com.example.data.db.DakuDatabase
import com.example.data.repository.CodeClaimResult
import com.example.data.repository.CodeVerificationResult
import com.example.data.repository.CreditRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CreditCodeValidationTest {

    private lateinit var database: DakuDatabase
    private lateinit var repository: CreditRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DakuDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CreditRepository(database.creditDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `test case 1 - correct format and database code exists and unused - verify succeeds and claim gives 50 credits`() = runBlocking {
        // Generate valid code
        val generated = repository.generateAdminCustomerCode(50)
        val validCode = generated.code

        assertTrue(CreditConfig.isCodeFormatValid(validCode))

        // 1. Verify
        val verifyResult = repository.verifyCode(validCode)
        assertTrue("Verification should succeed", verifyResult is CodeVerificationResult.Valid)
        val validResult = verifyResult as CodeVerificationResult.Valid
        assertEquals(50, validResult.creditAmount)

        // 2. Claim
        val claimResult = repository.claimCode("user_alice", validCode)
        assertTrue("Claim should succeed", claimResult is CodeClaimResult.Success)
        val successClaim = claimResult as CodeClaimResult.Success
        assertEquals(50, successClaim.creditsAdded)
        assertEquals(100, successClaim.newBalance) // 50 initial welcome + 50 claimed = 100
    }

    @Test
    fun `test case 2 - wrong special symbol results in invalid code`() = runBlocking {
        // '%' is not in allowed [@, #, ₹, &]
        val wrongSymbolCode = "KaDAKU%12345=RG"
        assertFalse(CreditConfig.isCodeFormatValid(wrongSymbolCode))

        val verifyResult = repository.verifyCode(wrongSymbolCode)
        assertTrue("Verify must return InvalidFormat", verifyResult is CodeVerificationResult.InvalidFormat)
        assertEquals("❌ Invalid Code", (verifyResult as CodeVerificationResult.InvalidFormat).message)

        val claimResult = repository.claimCode("user_alice", wrongSymbolCode)
        assertTrue("Claim must fail", claimResult is CodeClaimResult.Error)
        assertEquals("❌ Invalid Code", (claimResult as CodeClaimResult.Error).message)
    }

    @Test
    fun `test case 3 - missing DAKU results in invalid code`() = runBlocking {
        val missingDakuCode = "KaROBOT@12345=RG"
        assertFalse(CreditConfig.isCodeFormatValid(missingDakuCode))

        val verifyResult = repository.verifyCode(missingDakuCode)
        assertTrue(verifyResult is CodeVerificationResult.InvalidFormat)
        assertEquals("❌ Invalid Code", (verifyResult as CodeVerificationResult.InvalidFormat).message)

        val claimResult = repository.claimCode("user_alice", missingDakuCode)
        assertTrue(claimResult is CodeClaimResult.Error)
        assertEquals("❌ Invalid Code", (claimResult as CodeClaimResult.Error).message)
    }

    @Test
    fun `test case 4 - wrong number of digits results in invalid code`() = runBlocking {
        val fourDigitsCode = "KaDAKU@1234=RG"
        val sixDigitsCode = "KaDAKU@123456=RG"

        assertFalse(CreditConfig.isCodeFormatValid(fourDigitsCode))
        assertFalse(CreditConfig.isCodeFormatValid(sixDigitsCode))

        val verifyResult4 = repository.verifyCode(fourDigitsCode)
        assertTrue(verifyResult4 is CodeVerificationResult.InvalidFormat)

        val verifyResult6 = repository.verifyCode(sixDigitsCode)
        assertTrue(verifyResult6 is CodeVerificationResult.InvalidFormat)
    }

    @Test
    fun `test case 5 - missing =RG suffix results in invalid code`() = runBlocking {
        val missingSuffixCode = "KaDAKU@12345"
        val wrongSuffixCode = "KaDAKU@12345=XX"

        assertFalse(CreditConfig.isCodeFormatValid(missingSuffixCode))
        assertFalse(CreditConfig.isCodeFormatValid(wrongSuffixCode))

        val verifyResult = repository.verifyCode(missingSuffixCode)
        assertTrue(verifyResult is CodeVerificationResult.InvalidFormat)
    }

    @Test
    fun `test case 6 - wrong capital-letter + a pattern results in invalid code`() = runBlocking {
        val lowercaseStart = "kaDAKU@12345=RG"
        val wrongSecondLetter = "KbDAKU@12345=RG"
        val uppercaseSecondLetter = "KADAKU@12345=RG"

        assertFalse(CreditConfig.isCodeFormatValid(lowercaseStart))
        assertFalse(CreditConfig.isCodeFormatValid(wrongSecondLetter))
        assertFalse(CreditConfig.isCodeFormatValid(uppercaseSecondLetter))

        val verifyResult1 = repository.verifyCode(lowercaseStart)
        assertTrue(verifyResult1 is CodeVerificationResult.InvalidFormat)

        val verifyResult2 = repository.verifyCode(wrongSecondLetter)
        assertTrue(verifyResult2 is CodeVerificationResult.InvalidFormat)

        val verifyResult3 = repository.verifyCode(uppercaseSecondLetter)
        assertTrue(verifyResult3 is CodeVerificationResult.InvalidFormat)
    }

    @Test
    fun `test case 7 - correct looking but never generated code results in invalid code`() = runBlocking {
        // Valid syntax, but never created in DB
        val unseededCode = "ZaDAKU#98765=RG"
        assertTrue(CreditConfig.isCodeFormatValid(unseededCode))

        val verifyResult = repository.verifyCode(unseededCode)
        assertTrue("Must be NotFound in DB", verifyResult is CodeVerificationResult.NotFound)
        assertEquals("❌ Invalid Code", (verifyResult as CodeVerificationResult.NotFound).message)

        val claimResult = repository.claimCode("user_alice", unseededCode)
        assertTrue("Must fail claim", claimResult is CodeClaimResult.Error)
        assertEquals("❌ Invalid Code", (claimResult as CodeClaimResult.Error).message)
    }

    @Test
    fun `test case 8 - previously redeemed code results in Code Already Used`() = runBlocking {
        val codeEntity = repository.generateAdminCustomerCode(50)
        val code = codeEntity.code

        // 1st claim
        val claim1 = repository.claimCode("user_alice", code)
        assertTrue(claim1 is CodeClaimResult.Success)

        // 2nd verify
        val verify2 = repository.verifyCode(code)
        assertTrue("Verify must detect AlreadyUsed", verify2 is CodeVerificationResult.AlreadyUsed)
        assertEquals("❌ Code Already Used", (verify2 as CodeVerificationResult.AlreadyUsed).message)

        // 2nd claim
        val claim2 = repository.claimCode("user_alice", code)
        assertTrue("Claim must fail with Code Already Used", claim2 is CodeClaimResult.Error)
        assertEquals("❌ Code Already Used", (claim2 as CodeClaimResult.Error).message)
    }

    @Test
    fun `test case 9 - same code submitted from another account results in Code Already Used`() = runBlocking {
        val codeEntity = repository.generateAdminCustomerCode(50)
        val code = codeEntity.code

        // User A claims
        val claimA = repository.claimCode("user_alice", code)
        assertTrue(claimA is CodeClaimResult.Success)

        // User B tries to verify & claim
        val verifyB = repository.verifyCode(code)
        assertTrue(verifyB is CodeVerificationResult.AlreadyUsed)
        assertEquals("❌ Code Already Used", (verifyB as CodeVerificationResult.AlreadyUsed).message)

        val claimB = repository.claimCode("user_bob", code)
        assertTrue(claimB is CodeClaimResult.Error)
        assertEquals("❌ Code Already Used", (claimB as CodeClaimResult.Error).message)
    }

    @Test
    fun `test case 10 - two users claim same code simultaneously - only one succeeds`() = runBlocking {
        val codeEntity = repository.generateAdminCustomerCode(50)
        val code = codeEntity.code

        // Concurrently launch two claim calls
        val claimDeferred1 = async { repository.claimCode("user_alice", code) }
        val claimDeferred2 = async { repository.claimCode("user_bob", code) }

        val results = listOf(claimDeferred1, claimDeferred2).awaitAll()

        val successCount = results.count { it is CodeClaimResult.Success }
        val errorCount = results.count { it is CodeClaimResult.Error && it.message == "❌ Code Already Used" }

        assertEquals("Exactly one claim must succeed", 1, successCount)
        assertEquals("The other claim must fail with Code Already Used", 1, errorCount)
    }
}
