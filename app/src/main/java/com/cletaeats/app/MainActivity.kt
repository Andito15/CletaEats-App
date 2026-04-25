package com.cletaeats.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cletaeats.app.navigation.AppNavGraph
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.CletaEatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CletaEatsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundSoft
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}