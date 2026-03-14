package dev.moonsetter.shengcat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import dev.moonsetter.shengcat.ui.theme.ShengCatTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShengCatTheme {
                AppNavigation()
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AppPreview() {
    AppNavigation()
}