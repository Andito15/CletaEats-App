package com.cletaeats.app.ui.screens.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cletaeats.app.data.model.CalificacionRequest
import com.cletaeats.app.data.model.QuejaRequest
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    pedidoId: Long,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var amabilidad by remember { mutableStateOf(5) }
    var tiempo by remember { mutableStateOf(5) }
    var presentacion by remember { mutableStateOf(5) }
    var comentario by remember { mutableStateOf("") }

    var categoria by remember { mutableStateOf("SERVICIO") }
    var descripcionQueja by remember { mutableStateOf("") }

    var loadingCalificacion by remember { mutableStateOf(false) }
    var loadingQueja by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    CletaScaffold(
        title = "Calificar",
        onBack = onBack
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Experiencia con el repartidor",
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold
                    )

                    RatingRow(
                        title = "Amabilidad",
                        value = amabilidad,
                        onChange = { amabilidad = it }
                    )

                    RatingRow(
                        title = "Tiempo",
                        value = tiempo,
                        onChange = { tiempo = it }
                    )

                    RatingRow(
                        title = "Presentación",
                        value = presentacion,
                        onChange = { presentacion = it }
                    )

                    OutlinedTextField(
                        value = comentario,
                        onValueChange = { comentario = it },
                        label = { Text("Comentario") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(18.dp)
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                loadingCalificacion = true
                                error = null
                                message = null

                                try {
                                    api.registrarCalificacion(
                                        pedidoId = pedidoId,
                                        request = CalificacionRequest(
                                            puntajeAmabilidad = amabilidad,
                                            puntajeTiempo = tiempo,
                                            puntajePresentacion = presentacion,
                                            comentario = comentario.ifBlank { null }
                                        )
                                    )

                                    message = "Calificación registrada."
                                } catch (e: Exception) {
                                    error = e.message ?: "No se pudo registrar la calificación."
                                } finally {
                                    loadingCalificacion = false
                                }
                            }
                        },
                        enabled = !loadingCalificacion,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (loadingCalificacion) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null
                            )

                            Text("  Guardar")
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ReportProblem,
                            contentDescription = null,
                            tint = DangerRed
                        )

                        Text(
                            text = "  Registrar queja",
                            color = PrimaryDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "AMABILIDAD",
                            "TIEMPO",
                            "PRESENTACION",
                            "SERVICIO",
                            "OTRA"
                        ).forEach { cat ->
                            FilterChip(
                                selected = categoria == cat,
                                onClick = { categoria = cat },
                                label = {
                                    Text(
                                        text = when (cat) {
                                            "AMABILIDAD" -> "Ama."
                                            "TIEMPO" -> "Tiempo"
                                            "PRESENTACION" -> "Pres."
                                            "SERVICIO" -> "Serv."
                                            else -> "Otra"
                                        }
                                    )
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = descripcionQueja,
                        onValueChange = { descripcionQueja = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(18.dp)
                    )

                    Button(
                        onClick = {
                            if (descripcionQueja.isBlank()) {
                                error = "Describí la queja."
                                return@Button
                            }

                            scope.launch {
                                loadingQueja = true
                                error = null
                                message = null

                                try {
                                    api.registrarQueja(
                                        pedidoId = pedidoId,
                                        request = QuejaRequest(
                                            categoria = categoria,
                                            descripcion = descripcionQueja.trim()
                                        )
                                    )

                                    message = "Queja registrada."
                                    descripcionQueja = ""
                                } catch (e: Exception) {
                                    error = e.message ?: "No se pudo registrar la queja."
                                } finally {
                                    loadingQueja = false
                                }
                            }
                        },
                        enabled = !loadingQueja,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (loadingQueja) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.ReportProblem,
                                contentDescription = null
                            )

                            Text("  Enviar")
                        }
                    }
                }
            }

            if (message != null) {
                Text(
                    text = message ?: "",
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null
                    )
                }
            }

            if (error != null) {
                Text(
                    text = error ?: "",
                    color = DangerRed
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RatingRow(
    title: String,
    value: Int,
    onChange: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = TextSoft,
            style = MaterialTheme.typography.bodySmall
        )

        Row {
            (1..5).forEach { index ->
                IconButton(
                    onClick = { onChange(index) }
                ) {
                    Icon(
                        imageVector = if (index <= value) {
                            Icons.Rounded.Star
                        } else {
                            Icons.Outlined.StarBorder
                        },
                        contentDescription = "$title $index",
                        tint = if (index <= value) PrimaryGreen else TextSoft
                    )
                }
            }
        }
    }
}