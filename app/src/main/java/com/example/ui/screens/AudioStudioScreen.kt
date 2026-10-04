package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.GeneratedMediaEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlowingAmber
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AudioMode
import com.example.ui.viewmodel.AudioStudioViewModel

@Composable
fun AudioStudioScreen(viewModel: AudioStudioViewModel) {
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val selectedVoice by viewModel.selectedVoice.collectAsStateWithLifecycle()
    val musicStyle by viewModel.musicStyle.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val statusText by viewModel.statusText.collectAsStateWithLifecycle()
    val audioHistory by viewModel.audioHistory.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF064E3B), Color(0xFF0F172A))))
                    .border(1.dp, CyberCyan, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Audio Studio",
                        tint = CyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Audio & Music Studio",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Voice Generation (TTS), Lyria AI Music Tracks & Voice Cloning",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Mode Selector Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AudioMode.entries) { mode ->
                    val isSelected = mode == selectedMode
                    val icon = when (mode) {
                        AudioMode.VOICE_GEN -> Icons.Default.RecordVoiceOver
                        AudioMode.MUSIC_GEN -> Icons.Default.MusicNote
                        AudioMode.VOICE_CLONE -> Icons.Default.GraphicEq
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SurfaceCard else Color(0xFF111827))
                            .border(
                                1.5.dp,
                                if (isSelected) Brush.horizontalGradient(listOf(CyberCyan, NeonViolet)) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.setMode(mode) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = icon, contentDescription = mode.label, tint = if (isSelected) CyberCyan else TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.label,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Mode specific controls
        if (selectedMode == AudioMode.VOICE_GEN) {
            item {
                Column {
                    Text(text = "Select Prebuilt Voice:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Kore", "Puck", "Fenrir", "Aoede").forEach { v ->
                            val isSel = v == selectedVoice
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) CyberCyan else Color(0xFF111827))
                                    .clickable { viewModel.setVoice(v) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = v,
                                    color = if (isSel) Color.Black else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedMode == AudioMode.MUSIC_GEN) {
            item {
                Column {
                    Text(text = "Music Genre / Vibe:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Cinematic Lo-Fi Chill Beats", "Epic Orchestral Trailer", "Upbeat Pop Synth", "Ambient Zen Meditation", "Cyberpunk Electronic")) { style ->
                            val isSel = style == musicStyle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) GlowingAmber else Color(0xFF111827))
                                    .clickable { viewModel.setMusicStyle(style) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = style,
                                    color = if (isSel) Color.Black else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.setInputText(it) },
                        placeholder = {
                            Text(
                                text = if (selectedMode == AudioMode.VOICE_GEN) "Enter text to convert to voice speech..."
                                else if (selectedMode == AudioMode.MUSIC_GEN) "Describe music prompt, mood, or instruments..."
                                else "Enter target voice script for cloning...",
                                color = TextSecondary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF2E3E5C),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("audio_text_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.generateAudio() },
                        enabled = !isLoading && inputText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_audio_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Composing Audio...")
                        } else {
                            Text(
                                text = if (selectedMode == AudioMode.VOICE_GEN) "Generate Voice Speech"
                                else if (selectedMode == AudioMode.MUSIC_GEN) "Generate Lyria Music Track"
                                else "Clone & Synthesize Voice",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    statusText?.let { txt ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = txt, color = GlowingAmber, fontSize = 12.sp)
                    }
                }
            }
        }

        // Generated Audio History
        if (audioHistory.isNotEmpty()) {
            item {
                Text(
                    text = "Generated Audio Library",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(audioHistory) { audio ->
                AudioHistoryCard(media = audio, onDelete = { viewModel.deleteAudio(audio.id) })
            }
        }
    }
}

@Composable
fun AudioHistoryCard(media: GeneratedMediaEntity, onDelete: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${media.stylePreset} • ${media.resolution}",
                    color = CyberCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CyberCyan)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = media.prompt, color = TextPrimary, fontSize = 12.sp, maxLines = 1)
                        Text(
                            text = if (isPlaying) "Playing audio track..." else "Ready to play",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Wave",
                        tint = if (isPlaying) GlowingAmber else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
