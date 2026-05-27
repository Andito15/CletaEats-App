package com.cletaeats.app.ui.screens.datamode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.app.domain.datamode.DataMode
import com.cletaeats.app.domain.datamode.DataModeManager
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft

@Composable
fun DataModeSelectionScreen(
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val dataModeManager = remember { DataModeManager(context) }

    var selectedMode by remember {
        mutableStateOf(dataModeManager.getMode())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = null,
            tint = PrimaryGreen
        )

        Text(
            text = "Modo de datos",
            color = PrimaryDeep,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Elegí cómo querés trabajar.",
            color = TextSoft,
            style = MaterialTheme.typography.bodyMedium
        )

        Column(
            modifier = Modifier.padding(top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeCard(
                title = "API remota",
                description = "Backend público + MySQL.",
                icon = Icons.Rounded.Storage,
                selected = selectedMode == DataMode.API,
                onClick = {
                    selectedMode = DataMode.API
                }
            )

            ModeCard(
                title = "SQLite local",
                description = "Direcciones guardadas en el celular.",
                icon = Icons.Rounded.PhoneAndroid,
                selected = selectedMode == DataMode.LOCAL,
                onClick = {
                    selectedMode = DataMode.LOCAL
                }
            )

            ModeCard(
                title = "Cloud",
                description = "Direcciones en Firebase Firestore.",
                icon = Icons.Rounded.Cloud,
                selected = selectedMode == DataMode.CLOUD,
                onClick = {
                    selectedMode = DataMode.CLOUD
                }
            )
        }

        Button(
            onClick = {
                dataModeManager.saveMode(selectedMode)
                onContinue()
            },
            modifier = Modifier
                .padding(top = 24.dp)
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Continuar",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                PrimaryGreen.copy(alpha = 0.12f)
            } else {
                Color.White
            }
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryGreen
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}