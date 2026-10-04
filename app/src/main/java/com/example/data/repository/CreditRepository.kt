package com.example.data.repository

import com.example.data.config.CreditConfig
import com.example.data.db.CreditCodeEntity
import com.example.data.db.CreditDao
import com.example.data.db.CreditHistoryEntity
import com.example.data.db.UserCreditsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

sealed class CodeVerificationResult {
    data class InvalidFormat(val message: String) : CodeVerificationResult()
    data class RateLimited(val message: String) : CodeVerificationResult()
    data class NotFound(val message: String) : CodeVerificationResult()
    data class AlreadyUsed(val message: String) : CodeVerificationResult()
    data class Valid(val code: String, val creditAmount: Int) : CodeVerificationResult()
}

sealed class CodeClaimResult {
    data class Success(val newBalance: Int, val creditsAdded: Int) : CodeClaimResult()
    data class Error(val message: String) : CodeClaimResult()
}

class CreditRepository(private val creditDao: CreditDao) {

    private val verificationTimestamps = ConcurrentHashMap<String, MutableList<Long>>()
    private val claimMutex = Mutex()

    fun getUserCreditsFlow(userId: String = "default_user"): Flow<UserCreditsEntity?> {
        return creditDao.getUserCreditsFlow(userId)
    }

    suspend fun ensureUserCreditsInitialized(userId: String = "default_user"): UserCreditsEntity = withContext(Dispatchers.IO) {
        val existing = creditDao.getUserCredits(userId)
        if (existing != null) {
            existing
        } else {
            val newUser = UserCreditsEntity(userId = userId, balance = CreditConfig.INITIAL_USER_BALANCE)
            creditDao.insertOrUpdateUserCredits(newUser)
            // Initial Welcome History Record
            creditDao.insertHistory(
                CreditHistoryEntity(
                    userId = userId,
                    action = "🎁 Welcome Credits Granted",
                    amount = CreditConfig.INITIAL_USER_BALANCE,
                    balanceAfter = CreditConfig.INITIAL_USER_BALANCE,
                    transactionId = "TXN_WELCOME_${System.currentTimeMillis()}"
                )
            )
            newUser
        }
    }

    suspend fun deductCredits(
        userId: String = "default_user",
        featureName: String,
        requiredCredits: Int
    ): Boolean = withContext(Dispatchers.IO) {
        if (requiredCredits <= 0) return@withContext true // Free feature!

        claimMutex.withLock {
            val userCredits = ensureUserCreditsInitialized(userId)
            if (userCredits.balance < requiredCredits) {
                return@withLock false // Insufficient credits
            }

            val newBalance = userCredits.balance - requiredCredits
            creditDao.insertOrUpdateUserCredits(userCredits.copy(balance = newBalance))
            creditDao.insertHistory(
                CreditHistoryEntity(
                    userId = userId,
                    action = "$featureName (-$requiredCredits)",
                    amount = -requiredCredits,
                    balanceAfter = newBalance,
                    transactionId = "TXN_${UUID.randomUUID().toString().take(8)}"
                )
            )
            true
        }
    }

    /**
     * Verifies credit code server-side:
     * 1. Rate limiting check.
     * 2. Complete format validation against all 5 DAKU AI rules.
     * 3. Database lookup for exact code.
     * 4. Confirmation of UNUSED status.
     * Note: Credits are NOT awarded during verification.
     */
    suspend fun verifyCode(codeInput: String): CodeVerificationResult = withContext(Dispatchers.IO) {
        val cleanCode = codeInput.trim()

        // Rate limiting check: max 5 attempts per minute
        val now = System.currentTimeMillis()
        val attempts = verificationTimestamps.getOrPut("global") { mutableListOf() }
        synchronized(attempts) {
            attempts.removeAll { now - it > 60000 }
            if (attempts.size >= 5) {
                return@withContext CodeVerificationResult.RateLimited("Too many verification attempts. Please wait 1 minute.")
            }
            attempts.add(now)
        }

        // Structural validation against all 5 rules
        if (!CreditConfig.isCodeFormatValid(cleanCode)) {
            return@withContext CodeVerificationResult.InvalidFormat("❌ Invalid Code")
        }

        // Database lookup for exact code
        val codeEntity = creditDao.getCodeByValue(cleanCode)
            ?: return@withContext CodeVerificationResult.NotFound("❌ Invalid Code")

        // Status check
        if (codeEntity.status == "USED") {
            return@withContext CodeVerificationResult.AlreadyUsed("❌ Code Already Used")
        }

        CodeVerificationResult.Valid(code = codeEntity.code, creditAmount = codeEntity.creditAmount)
    }

    /**
     * Atomically claims a credit code for a user:
     * - Re-checks format & database existence.
     * - Confirms status is still UNUSED under mutex lock to eliminate race conditions.
     * - Marks code permanently as USED with account ID and timestamp.
     * - Credits the user account balance.
     * - Creates a permanent transaction history entry.
     */
    suspend fun claimCode(
        userId: String = "default_user",
        codeInput: String
    ): CodeClaimResult = withContext(Dispatchers.IO) {
        val cleanCode = codeInput.trim()

        // Server-side validation check
        if (!CreditConfig.isCodeFormatValid(cleanCode)) {
            return@withContext CodeClaimResult.Error("❌ Invalid Code")
        }

        claimMutex.withLock {
            val codeEntity = creditDao.getCodeByValue(cleanCode)
                ?: return@withLock CodeClaimResult.Error("❌ Invalid Code")

            if (codeEntity.status == "USED") {
                return@withLock CodeClaimResult.Error("❌ Code Already Used")
            }

            // Mark code permanently as USED
            val updatedCode = codeEntity.copy(
                status = "USED",
                redeemedBy = userId,
                redeemedAt = System.currentTimeMillis()
            )
            creditDao.updateCode(updatedCode)

            // Update user balance atomically
            val userCredits = ensureUserCreditsInitialized(userId)
            val newBalance = userCredits.balance + codeEntity.creditAmount
            creditDao.insertOrUpdateUserCredits(userCredits.copy(balance = newBalance))

            // Record transaction history
            creditDao.insertHistory(
                CreditHistoryEntity(
                    userId = userId,
                    action = "+${codeEntity.creditAmount} Credits Claimed",
                    amount = codeEntity.creditAmount,
                    balanceAfter = newBalance,
                    transactionId = "TXN_${UUID.randomUUID().toString().take(8)}"
                )
            )

            CodeClaimResult.Success(newBalance = newBalance, creditsAdded = codeEntity.creditAmount)
        }
    }

    /**
     * Generates a unique, valid DAKU AI credit code following:
     * [CAPITAL LETTER][a]DAKU[SPECIAL SYMBOL][5 DIGITS]=RG
     * Example: KaDAKU@12345=RG
     * Enforces database uniqueness.
     */
    suspend fun generateAdminCustomerCode(
        creditAmount: Int = CreditConfig.CREDITS_PER_CODE
    ): CreditCodeEntity = withContext(Dispatchers.IO) {
        val random = SecureRandom()
        val capitalLetters = ('A'..'Z').toList()
        val allowedSymbols = CreditConfig.ALLOWED_SPECIAL_SYMBOLS
        var generatedCode: String
        var codeEntity: CreditCodeEntity

        while (true) {
            val capital = capitalLetters[random.nextInt(capitalLetters.size)]
            val symbol = allowedSymbols[random.nextInt(allowedSymbols.size)]
            val digits = (0..4).map { random.nextInt(10) }.joinToString("")
            generatedCode = "${capital}aDAKU${symbol}${digits}=RG"

            val hash = hashString(generatedCode)
            codeEntity = CreditCodeEntity(
                code = generatedCode,
                codeHash = hash,
                creditAmount = creditAmount,
                status = "UNUSED",
                createdBy = "ADMIN",
                createdAt = System.currentTimeMillis()
            )
            try {
                creditDao.insertCode(codeEntity)
                break // Successfully inserted unique code
            } catch (e: Exception) {
                // Collision prevention retry
            }
        }
        codeEntity
    }

    /**
     * Helper to insert a pre-defined test code into database (for testing)
     */
    suspend fun insertTestCode(code: String, creditAmount: Int = CreditConfig.CREDITS_PER_CODE): Boolean = withContext(Dispatchers.IO) {
        if (!CreditConfig.isCodeFormatValid(code)) return@withContext false
        try {
            creditDao.insertCode(
                CreditCodeEntity(
                    code = code,
                    codeHash = hashString(code),
                    creditAmount = creditAmount,
                    status = "UNUSED",
                    createdBy = "TEST_SYSTEM",
                    createdAt = System.currentTimeMillis()
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getCreditHistory(userId: String = "default_user"): Flow<List<CreditHistoryEntity>> {
        return creditDao.getCreditHistory(userId)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

