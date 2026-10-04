package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.db.DakuDatabase
import com.example.data.repository.CreditRepository
import com.example.data.repository.DakuRepository
import com.example.ui.MainScreen
import com.example.ui.theme.DakuAiTheme
import com.example.ui.viewmodel.AudioStudioViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.DakuViewModelFactory
import com.example.ui.viewmodel.GeneralAiViewModel
import com.example.ui.viewmodel.ImageStudioViewModel
import com.example.ui.viewmodel.ResearchStudioViewModel
import com.example.ui.viewmodel.TalkingAssistantViewModel
import com.example.ui.viewmodel.VideoStudioViewModel

class MainActivity : ComponentActivity() {

    private lateinit var database: DakuDatabase
    private lateinit var creditRepository: CreditRepository
    private lateinit var repository: DakuRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = DakuDatabase.getDatabase(this)
        creditRepository = CreditRepository(database.creditDao())
        repository = DakuRepository(
            chatDao = database.chatDao(),
            researchDao = database.researchDao(),
            mediaDao = database.mediaDao(),
            creditRepository = creditRepository
        )

        val factory = DakuViewModelFactory(repository)

        val generalVm: GeneralAiViewModel by viewModels { factory }
        val researchVm: ResearchStudioViewModel by viewModels { factory }
        val imageVm: ImageStudioViewModel by viewModels { factory }
        val videoVm: VideoStudioViewModel by viewModels { factory }
        val audioVm: AudioStudioViewModel by viewModels { factory }
        val talkingVm: TalkingAssistantViewModel by viewModels { factory }
        val authVm = AuthViewModel(creditRepository)

        setContent {
            DakuAiTheme {
                MainScreen(
                    generalVm = generalVm,
                    researchVm = researchVm,
                    imageVm = imageVm,
                    videoVm = videoVm,
                    audioVm = audioVm,
                    talkingVm = talkingVm,
                    authVm = authVm
                )
            }
        }
    }
}
