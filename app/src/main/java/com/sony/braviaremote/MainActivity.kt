package com.sony.braviaremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sony.braviaremote.ui.MainViewModel
import com.sony.braviaremote.ui.RemoteScreen
import com.sony.braviaremote.ui.theme.BraviaRemoteTheme
import com.sony.braviaremote.ui.theme.DarkBackground

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BraviaRemoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    RemoteScreen(viewModel = viewModel)
                }
            }
        }
    }
}
