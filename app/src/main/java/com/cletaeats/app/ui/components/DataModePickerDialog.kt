package com.cletaeats.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.app.domain.datamode.DataMode
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft

@Composable
fun DataModePickerDialog(
    selectedMode: DataMode,
    onModeSelected: (DataMode) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Modo de datos",
                color = PrimaryDeep,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Elegí la fuente de datos para esta sesión.",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )

                DataModeOptionCard(
                    mode = DataMode.API,
                    selected = selectedMode == DataMode.API,
                    icon = Icons.Rounded.Storage,
                    title = "API remota",
                    description = "Backend público + MySQL.",
                    onClick = {
                        onModeSelected(DataMode.API)
                    }
                )

                DataModeOptionCard(
                    mode = DataMode.LOCAL,
                    selected = selectedMode == DataMode.LOCAL,
                    icon = Icons.Rounded.PhoneAndroid,
                    title = "SQLite local",
                    description = "Datos guardados en el celular.",
                    onClick = {
                        onModeSelected(DataMode.LOCAL)
                    }
                )

                DataModeOptionCard(
                    mode = DataMode.CLOUD,
                    selected = selectedMode == DataMode.CLOUD,
                    icon = Icons.Rounded.Cloud,
                    title = "Cloud",
                    description = "Firebase Firestore.",
                    onClick = {
                        onModeSelected(DataMode.CLOUD)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = TextSoft
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(26.dp)
    )
}

@Composable
private fun DataModeOptionCard(
    mode: DataMode,
    selected: Boolean,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                PrimaryGreen.copy(alpha = 0.10f)
            } else {
                Color(0xFFF8FAF8)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
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