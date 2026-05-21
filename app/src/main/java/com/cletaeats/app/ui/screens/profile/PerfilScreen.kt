package com.cletaeats.app.ui.screens.profile

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cletaeats.app.domain.payment.PaymentCard
import com.cletaeats.app.domain.payment.PaymentCardsManager
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

    val nombre = sessionManager.getNombre().orEmpty()
    val correo = sessionManager.getCorreo().orEmpty()
    val rol = sessionManager.getRol().orEmpty()
    val clienteId = sessionManager.getClienteId()
    val repartidorId = sessionManager.getRepartidorId()

    var tarjetas by remember {
        mutableStateOf(PaymentCardsManager.getCards(context))
    }

    var showCardForm by remember {
        mutableStateOf(false)
    }

    var numeroTarjeta by remember {
        mutableStateOf("")
    }

    var titularTarjeta by remember {
        mutableStateOf("")
    }

    var mesTarjeta by remember {
        mutableStateOf("")
    }

    var anioTarjeta by remember {
        mutableStateOf("")
    }

    var cvvTarjeta by remember {
        mutableStateOf("")
    }

    var cvvVisible by remember {
        mutableStateOf(false)
    }

    var mensajeTarjeta by remember {
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
                    nombre = nombre.ifBlank {
                        if (rol == "REPARTIDOR") "Repartidor" else "Cliente"
                    },
                    correo = correo,
                    rol = rol
                )
            }

            item {
                InfoCard(
                    title = "Información",
                    rows = buildList {
                        add(
                            ProfileRow(
                                icon = Icons.Rounded.Person,
                                label = "Nombre",
                                value = nombre.ifBlank { "No registrado" }
                            )
                        )

                        add(
                            ProfileRow(
                                icon = Icons.Rounded.Email,
                                label = "Correo",
                                value = correo.ifBlank { "No registrado" }
                            )
                        )

                        add(
                            ProfileRow(
                                icon = Icons.Rounded.AccountCircle,
                                label = "Rol",
                                value = rol.ifBlank { "Cliente" }
                            )
                        )

                        if (clienteId != null) {
                            add(
                                ProfileRow(
                                    icon = Icons.Rounded.AccountCircle,
                                    label = "Cliente ID",
                                    value = clienteId.toString()
                                )
                            )
                        }

                        if (repartidorId != null) {
                            add(
                                ProfileRow(
                                    icon = Icons.Rounded.AccountCircle,
                                    label = "Repartidor ID",
                                    value = repartidorId.toString()
                                )
                            )
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
                    PaymentCardsSection(
                        tarjetas = tarjetas,
                        showForm = showCardForm,
                        numeroTarjeta = numeroTarjeta,
                        titularTarjeta = titularTarjeta,
                        mesTarjeta = mesTarjeta,
                        anioTarjeta = anioTarjeta,
                        cvvTarjeta = cvvTarjeta,
                        cvvVisible = cvvVisible,
                        mensaje = mensajeTarjeta,
                        onToggleForm = {
                            showCardForm = !showCardForm
                            mensajeTarjeta = null
                        },
                        onNumeroChange = { value ->
                            numeroTarjeta = value
                                .filter { it.isDigit() }
                                .take(19)

                            mensajeTarjeta = null
                        },
                        onTitularChange = { value ->
                            titularTarjeta = value
                            mensajeTarjeta = null
                        },
                        onMesChange = { value ->
                            mesTarjeta = value
                                .filter { it.isDigit() }
                                .take(2)

                            mensajeTarjeta = null
                        },
                        onAnioChange = { value ->
                            anioTarjeta = value
                                .filter { it.isDigit() }
                                .take(4)

                            mensajeTarjeta = null
                        },
                        onCvvChange = { value ->
                            cvvTarjeta = value
                                .filter { it.isDigit() }
                                .take(4)

                            mensajeTarjeta = null
                        },
                        onToggleCvv = {
                            cvvVisible = !cvvVisible
                        },
                        onSave = {
                            if (cvvTarjeta.length < 3) {
                                mensajeTarjeta = "CVV inválido."
                            } else {
                                try {
                                    PaymentCardsManager.addCard(
                                        context = context,
                                        numero = numeroTarjeta,
                                        titular = titularTarjeta,
                                        mes = mesTarjeta,
                                        anio = anioTarjeta
                                    )

                                    tarjetas = PaymentCardsManager.getCards(context)

                                    numeroTarjeta = ""
                                    titularTarjeta = ""
                                    mesTarjeta = ""
                                    anioTarjeta = ""
                                    cvvTarjeta = ""
                                    showCardForm = false

                                    mensajeTarjeta = "Tarjeta agregada correctamente."
                                } catch (e: Exception) {
                                    mensajeTarjeta = e.message ?: "No se pudo agregar la tarjeta."
                                }
                            }
                        },
                        onDefault = { cardId ->
                            PaymentCardsManager.setDefault(context, cardId)
                            tarjetas = PaymentCardsManager.getCards(context)
                        },
                        onDelete = { cardId ->
                            PaymentCardsManager.deleteCard(context, cardId)
                            tarjetas = PaymentCardsManager.getCards(context)
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
private fun PaymentCardsSection(
    tarjetas: List<PaymentCard>,
    showForm: Boolean,
    numeroTarjeta: String,
    titularTarjeta: String,
    mesTarjeta: String,
    anioTarjeta: String,
    cvvTarjeta: String,
    cvvVisible: Boolean,
    mensaje: String?,
    onToggleForm: () -> Unit,
    onNumeroChange: (String) -> Unit,
    onTitularChange: (String) -> Unit,
    onMesChange: (String) -> Unit,
    onAnioChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onToggleCvv: () -> Unit,
    onSave: () -> Unit,
    onDefault: (String) -> Unit,
    onDelete: (String) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.CreditCard,
                    contentDescription = null,
                    tint = PrimaryGreen
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Métodos de pago",
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Tarjetas guardadas y opción predeterminada.",
                        color = TextSoft,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = onToggleForm) {
                    Icon(
                        imageVector = if (showForm) {
                            Icons.Rounded.Close
                        } else {
                            Icons.Rounded.Add
                        },
                        contentDescription = null,
                        tint = PrimaryGreen
                    )
                }
            }

            if (tarjetas.isEmpty()) {
                Text(
                    text = "No hay tarjetas registradas.",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                tarjetas.forEach { card ->
                    SavedCardRow(
                        card = card,
                        onDefault = {
                            onDefault(card.id)
                        },
                        onDelete = {
                            onDelete(card.id)
                        }
                    )
                }
            }

            if (showForm) {
                Divider()

                Text(
                    text = "Agregar tarjeta",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = numeroTarjeta,
                    onValueChange = onNumeroChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Número de tarjeta") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CreditCard,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )

                OutlinedTextField(
                    value = titularTarjeta,
                    onValueChange = onTitularChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre del titular") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
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
                    OutlinedTextField(
                        value = mesTarjeta,
                        onValueChange = onMesChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("MM") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp)
                    )

                    OutlinedTextField(
                        value = anioTarjeta,
                        onValueChange = onAnioChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("AA") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp)
                    )

                    OutlinedTextField(
                        value = cvvTarjeta,
                        onValueChange = onCvvChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("CVV") },
                        singleLine = true,
                        visualTransformation = if (cvvVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = onToggleCvv) {
                                Icon(
                                    imageVector = if (cvvVisible) {
                                        Icons.Rounded.VisibilityOff
                                    } else {
                                        Icons.Rounded.Visibility
                                    },
                                    contentDescription = null
                                )
                            }
                        },
                        shape = RoundedCornerShape(18.dp)
                    )
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
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
            }

            if (mensaje != null) {
                Text(
                    text = mensaje,
                    color = if (mensaje.contains("correctamente", ignoreCase = true)) {
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

@Composable
private fun SavedCardRow(
    card: PaymentCard,
    onDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = PrimaryGreen.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CreditCard,
                contentDescription = null,
                tint = PrimaryGreen
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = card.displayName,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${card.titular} · vence ${card.vencimiento}",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onDefault) {
                Icon(
                    imageVector = if (card.predeterminada) {
                        Icons.Rounded.Star
                    } else {
                        Icons.Rounded.StarBorder
                    },
                    contentDescription = "Predeterminada",
                    tint = PrimaryGreen
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Eliminar",
                    tint = DangerRed
                )
            }
        }
    }
}