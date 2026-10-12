package com.example.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberContainer
import com.example.cyberpunkandroid.theme.CyberTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CyberTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberTheme.colors.background
                ) {
                    SampleApp()
                }
            }
        }
    }
}

@Composable
fun SampleApp(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .safeDrawingPadding()
            .fillMaxSize()
            .padding(CyberTheme.spacing.dp16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CyberContainer(
            modifier = Modifier.padding(CyberTheme.spacing.dp16)
        ) {
            Column(
                modifier = Modifier.padding(CyberTheme.spacing.dp24),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NEON DECK",
                    style = CyberTheme.typography.display,
                    color = CyberTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(CyberTheme.spacing.dp8))
                Text(
                    text = "Cyberpunk UI Showcase Scaffold",
                    style = CyberTheme.typography.body,
                    color = CyberTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(CyberTheme.spacing.dp16))
                CyberButton(
                    onClick = { }
                ) {
                    Text("INITIALIZE")
                }
            }
        }
    }
}
