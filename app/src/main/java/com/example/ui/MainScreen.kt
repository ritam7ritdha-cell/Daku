package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.config.CreditConfig
import com.example.ui.components.AuthDialog
import com.example.ui.components.InsufficientCreditsDialog
import com.example.ui.components.ProfileDialog
import com.example.ui.screens.AudioStudioScreen
import com.example.ui.screens.GeneralAiScreen
import com.example.ui.screens.ImageStudioScreen
import com.example.ui.screens.ResearchStudioScreen
import com.example.ui.screens.TalkingAssistantScreen
import com.example.ui.screens.VideoStudioScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AudioStudioViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.GeneralAiViewModel
import com.example.ui.viewmodel.ImageStudioViewModel
import com.example.ui.viewmodel.ResearchStudioViewModel
import com.example.ui.viewmodel.TalkingAssistantViewModel
import com.example.ui.viewmodel.VideoStudioViewModel

data class SuiteTab(
    val title: String,
    val icon: ImageVector,
    val tag: String
)

val suiteTabs = listOf(
    SuiteTab("General AI", Icons.Default.AutoAwesome, "tab_general"),
    SuiteTab("Research", Icons.AutoMirrored.Filled.MenuBook, "tab_research"),
    SuiteTab("Image Studio", Icons.Default.Palette, "tab_image"),
    SuiteTab("Video Studio", Icons.Default.Videocam, "tab_video"),
    SuiteTab("Audio & Music", Icons.Default.MusicNote, "tab_audio"),
    SuiteTab("Talking Assistant", Icons.Default.RecordVoiceOver, "tab_talking")
)

@Composable
fun MainScreen(
    generalVm: GeneralAiViewModel,
    researchVm: ResearchStudioViewModel,
    imageVm: ImageStudioViewModel,
    videoVm: VideoStudioViewModel,
    audioVm: AudioStudioViewModel,
    talkingVm: TalkingAssistantViewModel,
    authVm: AuthViewModel
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val userProfile by authVm.userProfile.collectAsStateWithLifecycle()
    val userCredits by authVm.userCredits.collectAsStateWithLifecycle()
    val showAuthDialog by authVm.showAuthDialog.collectAsStateWithLifecycle()

    val imageInsufficientCredits by imageVm.insufficientCreditsInfo.collectAsStateWithLifecycle()
    val videoInsufficientCredits by videoVm.insufficientCreditsInfo.collectAsStateWithLifecycle()
    val audioInsufficientCredits by audioVm.insufficientCreditsInfo.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(suiteTabs.indices.toList()) { index ->
                        val tab = suiteTabs[index]
                        val isSelected = selectedTabIndex == index

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF0F4C81), Color(0xFF380E5E)))
                                    else Brush.horizontalGradient(listOf(Color(0xFF111827), Color(0xFF111827)))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CyberCyan else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedTabIndex = index }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag(tab.tag)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) CyberCyan else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top App Bar Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.daku_ai_logo_1786777862759),
                            contentDescription = "DAKU AI Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Brush.linearGradient(listOf(CyberCyan, NeonViolet)), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DAKU AI",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "History Making AI Suite",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Right Top Actions: Credits Badge & Profile Icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1B4B))
                                .border(1.dp, GlowingAmber, RoundedCornerShape(12.dp))
                                .clickable { showProfileDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("credits_badge")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = GlowingAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${userCredits?.balance ?: 50} CR",
                                    color = GlowingAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.testTag("top_profile_icon")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = CyberCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Small Optional Shortcut: [ WhatsApp Icon ] DAKU AI Updates & Giveaways
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .border(0.5.dp, Color(0xFF1E293B))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CreditConfig.WHATSAPP_CHANNEL_URL))
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("whatsapp_channel_shortcut"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_whatsapp),
                        contentDescription = "WhatsApp",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DAKU AI Updates & Giveaways",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Open",
                        color = Color(0xFF25D366),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Screen Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTabIndex) {
                    0 -> GeneralAiScreen(viewModel = generalVm)
                    1 -> ResearchStudioScreen(viewModel = researchVm)
                    2 -> ImageStudioScreen(viewModel = imageVm)
                    3 -> VideoStudioScreen(viewModel = videoVm)
                    4 -> AudioStudioScreen(viewModel = audioVm)
                    5 -> TalkingAssistantScreen(viewModel = talkingVm)
                }
            }
        }
    }

    // Dialogs
    if (showProfileDialog) {
        ProfileDialog(
            userProfile = userProfile,
            authVm = authVm,
            onDismiss = { showProfileDialog = false }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            isSignUpMode = authVm.isSignUpMode.collectAsStateWithLifecycle().value,
            onDismiss = { authVm.closeAuthDialog() },
            onLogin = { email, name -> authVm.login(email, name) },
            onToggleMode = { authVm.toggleAuthMode() }
        )
    }

    imageInsufficientCredits?.let { info ->
        InsufficientCreditsDialog(
            currentBalance = info.currentBalance,
            requiredCredits = info.requiredCredits,
            featureName = info.featureName,
            onDismiss = { imageVm.dismissInsufficientCreditsDialog() },
            onOpenProfile = { showProfileDialog = true }
        )
    }

    videoInsufficientCredits?.let { info ->
        InsufficientCreditsDialog(
            currentBalance = info.currentBalance,
            requiredCredits = info.requiredCredits,
            featureName = info.featureName,
            onDismiss = { videoVm.dismissInsufficientCreditsDialog() },
            onOpenProfile = { showProfileDialog = true }
        )
    }

    audioInsufficientCredits?.let { info ->
        InsufficientCreditsDialog(
            currentBalance = info.currentBalance,
            requiredCredits = info.requiredCredits,
            featureName = info.featureName,
            onDismiss = { audioVm.dismissInsufficientCreditsDialog() },
            onOpenProfile = { showProfileDialog = true }
        )
    }
}
