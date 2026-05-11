package com.cletaeats.app.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.app.R
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.PrimaryGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }

    LaunchedEffect(Unit) {
        delay(900)
        onFinish(!session.getToken().isNullOrBlank())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_solo),
            contentDescription = "CletaEats",
            modifier = Modifier.size(150.dp)
        )

        Text(
            text = "CletaEats",
            color = PrimaryGreen,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}