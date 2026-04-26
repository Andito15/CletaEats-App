package com.cletaeats.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.app.data.model.ClienteDireccionResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder

@Composable
fun DireccionesScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val apiService = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    val clienteId = sessionManager.getClienteId()

    var items by remember { mutableStateOf<List<ClienteDireccionResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun cargar() {
        if (clienteId == null) {
            error = "Sin sesión"
            loading = false
            return
        }

        scope.launch {
            loading = true
            error = null

            try {
                items = apiService.listarDirecciones(clienteId)
            } catch (e: Exception) {
                error = e.message ?: "Error"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Volver"
                    )
                }

                Text(
                    text = "Direcciones",
                    color = PrimaryGreen,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Nueva"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            loading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(
                    text = error ?: "Error",
                    color = Color.Red
                )
            }

            items.isEmpty() -> {
                Text(
                    text = "Sin direcciones",
                    color = TextSoft
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {

                                // 🔹 ICONO
                                Icon(
                                    imageVector = if (item.esPredeterminada) {
                                        Icons.Rounded.CheckCircle
                                    } else {
                                        Icons.Rounded.LocationOn
                                    },
                                    contentDescription = null,
                                    tint = PrimaryGreen
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 🔹 TEXTO
                                Text(
                                    text = item.alias,
                                    fontSize = 18.sp
                                )

                                Text(
                                    text = item.direccionTexto,
                                    color = TextSoft
                                )

                                Text(
                                    text = "${item.latitud}, ${item.longitud}",
                                    color = TextSoft,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // 🔥 ACCIONES
                                androidx.compose.foundation.layout.Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    // ⭐ predeterminada
                                    IconButton(
                                        onClick = {
                                            val cId = clienteId ?: return@IconButton
                                            val dId = item.direccionId ?: return@IconButton

                                            if (item.esPredeterminada) return@IconButton

                                            scope.launch {
                                                try {
                                                    apiService.marcarDireccionPredeterminada(cId, dId)
                                                    cargar()
                                                } catch (_: Exception) {
                                                }
                                            }
                                        }
                                    ) {
                                        val isDefault = item.esPredeterminada == true

                                        Icon(
                                            imageVector = if (isDefault) {
                                                Icons.Rounded.Star
                                            } else {
                                                Icons.Outlined.StarBorder
                                            },
                                            contentDescription = "Predeterminada",
                                            tint = PrimaryGreen
                                        )
                                    }

                                    // 🗑 eliminar
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                val cId = clienteId ?: return@launch
                                                val dId = item.direccionId ?: return@launch

                                                try {
                                                    apiService.eliminarDireccion(cId, dId)
                                                    cargar()
                                                } catch (_: Exception) {
                                                }
                                            }
                                        }
                                    ){
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}