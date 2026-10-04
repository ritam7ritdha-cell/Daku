package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.config.CreditConfig
import com.example.data.repository.CodeClaimResult
import com.example.data.repository.CodeVerificationResult
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.UserProfile
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileDialog(
    userProfile: UserProfile,
    authVm: AuthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val userCredits by authVm.userCredits.collectAsStateWithLifecycle()
    val creditHistory by authVm.creditHistory.collectAsStateWithLifecycle()

    var codeInput by remember { mutableStateOf("") }
    var verificationMessage by remember { mutableStateOf<String?>(null) }
    var verificationSuccess by remember { mutableStateOf(false) }
    var verifiedCodeValue by remember { mutableStateOf<String?>(null) }
    var verifiedCreditAmount by remember { mutableStateOf(50) }

    val handleDismiss = {
        focusManager.clearFocus()
        onDismiss()
    }

    Dialog(onDismissRequest = handleDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .border(1.5.dp, Brush.horizontalGradient(listOf(CyberCyan, GlowingAmber, NeonViolet)), RoundedCornerShape(20.dp))
                .testTag("profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxHeight()
            ) {
                // Dialog Top Bar Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.daku_ai_logo_1786777862759),
                            contentDescription = "DAKU AI Logo",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.dp, CyberCyan, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAKU AI Profile & Credits",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = handleDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(color = Color(0xFF2E3E5C), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. USER PROFILE HEADER CARD
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Brush.radialGradient(listOf(CyberCyan, NeonViolet)))
                                ) {
                                    Text(
                                        text = userProfile.name.take(1).uppercase(),
                                        color = Color.Black,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = userProfile.name,
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = userProfile.email,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = userProfile.plan,
                                        color = GlowingAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 2. CREDITS BALANCE CARD
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlowingAmber, RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = GlowingAmber,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "⚡ Credits Balance", color = TextSecondary, fontSize = 12.sp)
                                        Text(
                                            text = "${userCredits?.balance ?: 50} Credits",
                                            color = GlowingAmber,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CreditConfig.WHATSAPP_CHANNEL_URL))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GlowingAmber),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "🎁 Get Code", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 3. OFFICIAL WHATSAPP CHANNEL PURPOSE & GIVEAWAYS CARD
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF25D366).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .testTag("whatsapp_purpose_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_whatsapp),
                                            contentDescription = "WhatsApp Channel",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "DAKU AI Updates & Giveaways",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF064E3B))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Official • Optional",
                                            color = Color(0xFF34D399),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "The official DAKU AI WhatsApp Channel provides:",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text(text = "• 🚀 DAKU AI updates & new feature announcements", color = TextPrimary, fontSize = 11.sp)
                                    Text(text = "• 🛠️ Maintenance & important system notices", color = TextPrimary, fontSize = 11.sp)
                                    Text(text = "• 🎟️ Credit-code drops & free-credit campaigns", color = TextPrimary, fontSize = 11.sp)
                                    Text(text = "• 🎁 Official giveaways & promotional events", color = TextPrimary, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Giveaway Policy Box
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Gavel,
                                                contentDescription = null,
                                                tint = GlowingAmber,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Fair Giveaways & Participation Rules",
                                                color = GlowingAmber,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Every giveaway has clear eligibility criteria, step-by-step participation rules, prize breakdown, and transparent winner selection. Joining is completely optional and never required to use DAKU AI. Opening or joining does not automatically award credits.",
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CreditConfig.WHATSAPP_CHANNEL_URL))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .testTag("open_whatsapp_channel_button")
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_whatsapp),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Open WhatsApp Channel",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. ENTER CODE & VERIFY -> CLAIM FLOW
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "🎟️ Enter Your Code", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Enter your DAKU AI credit code to redeem ₹${CreditConfig.PRICE_RUPEES} = ${CreditConfig.CREDITS_PER_CODE} Credits", color = TextSecondary, fontSize = 11.sp)
                                Text(text = "Format: [Capital][a]DAKU[Symbol][5 Digits]=RG", color = GlowingAmber.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = codeInput,
                                        onValueChange = {
                                            codeInput = it.take(25)
                                            verificationMessage = null
                                            verificationSuccess = false
                                        },
                                        placeholder = { Text("e.g. KaDAKU@12345=RG", color = TextSecondary, fontSize = 12.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = Color(0xFF2E3E5C),
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("code_input_field")
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            val repo = authVm.creditRepository
                                            if (repo == null) {
                                                verificationMessage = "Database initializing..."
                                                return@Button
                                            }
                                            scope.launch {
                                                when (val result = repo.verifyCode(codeInput)) {
                                                    is CodeVerificationResult.InvalidFormat -> {
                                                        verificationMessage = result.message
                                                        verificationSuccess = false
                                                    }
                                                    is CodeVerificationResult.RateLimited -> {
                                                        verificationMessage = result.message
                                                        verificationSuccess = false
                                                    }
                                                    is CodeVerificationResult.NotFound -> {
                                                        verificationMessage = result.message
                                                        verificationSuccess = false
                                                    }
                                                    is CodeVerificationResult.AlreadyUsed -> {
                                                        verificationMessage = result.message
                                                        verificationSuccess = false
                                                    }
                                                    is CodeVerificationResult.Valid -> {
                                                        verificationMessage = "✅ Code Verified!\n🎁 ${result.creditAmount} Credits Available"
                                                        verificationSuccess = true
                                                        verifiedCodeValue = result.code
                                                        verifiedCreditAmount = result.creditAmount
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(52.dp).testTag("verify_code_button")
                                    ) {
                                        Text(text = "Verify", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                verificationMessage?.let { msg ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = msg,
                                        color = if (verificationSuccess) GlowingAmber else Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (verificationSuccess && verifiedCodeValue != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val repo = authVm.creditRepository ?: return@Button
                                            val codeToClaim = verifiedCodeValue ?: return@Button
                                            scope.launch {
                                                when (val claimRes = repo.claimCode("default_user", codeToClaim)) {
                                                    is CodeClaimResult.Success -> {
                                                        Toast.makeText(context, "🎉 +${claimRes.creditsAdded} Credits Claimed!", Toast.LENGTH_LONG).show()
                                                        verificationMessage = "🎉 Success! +${claimRes.creditsAdded} Credits added to your account."
                                                        verificationSuccess = false
                                                        verifiedCodeValue = null
                                                        codeInput = ""
                                                    }
                                                    is CodeClaimResult.Error -> {
                                                        verificationMessage = claimRes.message
                                                        verificationSuccess = false
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GlowingAmber),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("claim_credits_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Claim $verifiedCreditAmount Credits", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 4. CREDIT HISTORY LIST
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = GlowingAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "📜 Credit History", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (creditHistory.isEmpty()) {
                        item {
                            Text(text = "No credit transactions recorded yet.", color = TextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        items(creditHistory) { historyItem ->
                            val isPositive = historyItem.amount > 0
                            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            val dateStr = dateFormat.format(Date(historyItem.timestamp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = historyItem.action, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "$dateStr • Balance: ${historyItem.balanceAfter}", color = TextSecondary, fontSize = 10.sp)
                                    }
                                    Text(
                                        text = if (isPositive) "+${historyItem.amount}" else "${historyItem.amount}",
                                        color = if (isPositive) GlowingAmber else Color(0xFFEF4444),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        authVm.logout()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Sign Out", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
