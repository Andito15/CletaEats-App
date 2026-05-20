package com.cletaeats.app.ui.screens.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft

@Composable
fun PerfilScreen(
    onBack: () -> Unit,
    onDirecciones: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val prefs = remember {
        context.getSharedPreferences("cletaeats_payment", Context.MODE_PRIVATE)
    }

    val nombre = sessionManager.getNombre().orEmpty()
    val correo = sessionManager.getCorreo().orEmpty()
    val rol = sessionManager.getRol().orEmpty()
    val clienteId = sessionManager.getClienteId()
    val repartidorId = sessionManager.getRepartidorId()

    var tarjetaUltimos4 by remember {
        mutableStateOf(prefs.getString("tarjetaUltimos4", "").orEmpty())
    }

    var tarjetaInput by remember {
        mutableStateOf(tarjetaUltimos4)
    }

    var mensaje by remember {
        mutableStateOf<String?>(null)
    }

    CletaScaffold(
        title = "Perfil",
        onBack = onBack
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundSoft)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                ProfileHeaderCard(
                    nombre = nombre.ifBlank { if (rol == "REPARTIDOR") "Repartidor" else "Cliente" },
                    correo = correo,
                    rol = rol
                )
            }

            item {
                InfoCard(
                    title = "Información",
                    rows = buildList {
                        add(ProfileRow(Icons.Rounded.Person, "Nombre", nombre.ifBlank { "No registrado" }))
                        add(ProfileRow(Icons.Rounded.Email, "Correo", correo.ifBlank { "No registrado" }))
                        add(ProfileRow(Icons.Rounded.AccountCircle, "Rol", rol.ifBlank { "Cliente" }))

                        if (clienteId != null) {
                            add(ProfileRow(Icons.Rounded.AccountCircle, "Cliente ID", clienteId.toString()))
                        }

                        if (repartidorId != null) {
                            add(ProfileRow(Icons.Rounded.AccountCircle, "Repartidor ID", repartidorId.toString()))
                        }
                    }
                )
            }

            if (rol != "REPARTIDOR") {
                item {
                    AddressAccessCard(
                        onDirecciones = onDirecciones
                    )
                }

                item {
                    PaymentCard(
                        tarjetaUltimos4 = tarjetaUltimos4,
                        tarjetaInput = tarjetaInput,
                        mensaje = mensaje,
                        onInputChange = { value ->
                            tarjetaInput = value
                                .filter { it.isDigit() }
                                .take(4)

                            mensaje = null
                        },
                        onSave = {
                            if (tarjetaInput.length != 4) {
                                mensaje = "Ingresá los últimos 4 dígitos."
                                return@PaymentCard
                            }

                            prefs.edit()
                                .putString("tarjetaUltimos4", tarjetaInput)
                                .apply()

                            tarjetaUltimos4 = tarjetaInput
                            mensaje = "Método de pago guardado."
                        },
                        onDelete = {
                            prefs.edit()
                                .remove("tarjetaUltimos4")
                                .apply()

                            tarjetaUltimos4 = ""
                            tarjetaInput = ""
                            mensaje = "Método de pago eliminado."
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

private data class ProfileRow(
    val icon: ImageVector,
    val label: String,
    val value: String
)

@Composable
private fun ProfileHeaderCard(
    nombre: String,
    correo: String,
    rol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(8.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nombre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = correo.ifBlank { "Sesión activa" },
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (rol == "REPARTIDOR") "Repartidor" else "Cliente",
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    rows: List<ProfileRow>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = PrimaryDeep,
                fontWeight = FontWeight.Bold
            )

            Divider()

            rows.forEach { row ->
                InfoRow(row = row)
            }
        }
    }
}

@Composable
private fun InfoRow(
    row: ProfileRow
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = row.icon,
            contentDescription = null,
            tint = PrimaryGreen
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = row.label,
                color = TextSoft,
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = row.value,
                color = PrimaryDeep,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddressAccessCard(
    onDirecciones: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = PrimaryGreen
                )

                Text(
                    text = "Direcciones",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Administrá tus direcciones de entrega.",
                color = TextSoft,
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = onDirecciones,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun PaymentCard(
    tarjetaUltimos4: String,
    tarjetaInput: String,
    mensaje: String?,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.CreditCard,
                    contentDescription = null,
                    tint = PrimaryGreen
                )

                Text(
                    text = "Método de pago",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )
            }

            if (tarjetaUltimos4.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryGreen
                    )

                    Text(
                        text = "Tarjeta terminada en $tarjetaUltimos4",
                        color = PrimaryDeep,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = "Agregá los últimos 4 dígitos de tu tarjeta.",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = tarjetaInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Últimos 4 dígitos") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.CreditCard,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = "Guardar"
                    )
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Eliminar",
                        tint = DangerRed
                    )
                }
            }

            if (mensaje != null) {
                Text(
                    text = mensaje,
                    color = if (mensaje.contains("guardado", ignoreCase = true)) {
                        PrimaryGreen
                    } else {
                        DangerRed
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}