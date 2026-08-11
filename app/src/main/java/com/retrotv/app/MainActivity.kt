package com.retrotv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.retrotv.app.ui.MainMenuScreen
import com.retrotv.app.ui.theme.RetroTVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RetroTVTheme {
                MainMenuScreen(
                    onItemSelected = { item ->
                        // Stage 1: navigation between screens (Channels, TV Guide, etc.)
                        // will be implemented in later stages.
                    }
                )
            }
        }
    }
}
