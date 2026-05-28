package com.cletaeats.app.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.cletaeats.app.data.model.ClienteDireccionRequest
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.data.remote.toUserMessage
import com.cletaeats.app.data.repository.ClienteDireccionRepository
import com.cletaeats.app.domain.datamode.DataMode
import com.cletaeats.app.domain.datamode.DataModeManager
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DireccionFormScreen(
    direccionId: Long? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val sessionManager = remember { SessionManager(context) }
    val dataModeManager = remember { DataModeManager(context) }
    val dataMode = dataModeManager.getMode()

    val repository = remember {
        ClienteDireccionRepository(
            context = context,
            api = api
        )
    }

    val scope = rememberCoroutineScope()

    val defaultLatLng = remember {
        LatLng(9.9281, -84.0907)
    }

    var selectedLatLng by remember {
        mutableStateOf(defaultLatLng)
    }

    var alias by remember { mutableStateOf("") }
    var direccionTexto by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var esPredeterminada by remember { mutableStateOf(false) }

    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var loading by remember { mutableStateOf(direccionId != null) }

    LaunchedEffect(direccionId) {
        if (direccionId == null) return@LaunchedEffect

        try {
            val clienteId = sessionManager.getClienteId()
                ?: throw IllegalStateException("No se encontró el cliente en sesión.")

            val direccion = repository.obtener(
                clienteId = clienteId,
                direccionId = direccionId
            ) ?: throw IllegalStateException("Dirección no encontrada.")

            alias = direccion.alias
            direccionTexto = direccion.direccionTexto
            latitud = direccion.latitud?.toString().orEmpty()
            longitud = direccion.longitud?.toString().orEmpty()
            esPredeterminada = direccion.esPredeterminada == true

            val lat = direccion.latitud ?: 9.9281
            val lon = direccion.longitud ?: -84.0907
            selectedLatLng = LatLng(lat, lon)
        } catch (e: Exception) {
            error = e.toUserMessage()
        } finally {
            loading = false
        }
    }

    CletaScaffold(
        title = if (direccionId == null) "Agregar dirección" else "Editar dirección",
        onBack = onBack
    ) { modifier ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundSoft)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DataModeFormCard(mode = dataMode)

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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = PrimaryGreen
                        )
                        Text(
                            text = "Datos de entrega",
                            color = PrimaryDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = alias,
                        onValueChange = {
                            alias = it
                            error = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Alias") },
                        placeholder = { Text("Casa, trabajo, universidad") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp)
                    )

                    OutlinedTextField(
                        value = direccionTexto,
                        onValueChange = {
                            direccionTexto = it
                            error = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Dirección") },
                        placeholder = { Text("Ej. Heredia, Santo Domingo...") },
                        minLines = 3,
                        shape = RoundedCornerShape(18.dp)
                    )

                    MapLocationPicker(
                        selectedLatLng = selectedLatLng,
                        onLocationSelected = { latLng ->
                            selectedLatLng = latLng
                            latitud = String.format(Locale.US, "%.7f", latLng.latitude)
                            longitud = String.format(Locale.US, "%.7f", latLng.longitude)
                            error = null
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = latitud,
                            onValueChange = { value ->
                                latitud = value.filter { char ->
                                    char.isDigit() || char == '.' || char == '-'
                                }

                                val lat = latitud.toDoubleOrNull()
                                val lon = longitud.toDoubleOrNull()

                                if (lat != null && lon != null) {
                                    selectedLatLng = LatLng(lat, lon)
                                }

                                error = null
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Latitud") },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp)
                        )

                        OutlinedTextField(
                            value = longitud,
                            onValueChange = { value ->
                                longitud = value.filter { char ->
                                    char.isDigit() || char == '.' || char == '-'
                                }

                                val lat = latitud.toDoubleOrNull()
                                val lon = longitud.toDoubleOrNull()

                                if (lat != null && lon != null) {
                                    selectedLatLng = LatLng(lat, lon)
                                }

                                error = null
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Longitud") },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Predeterminada",
                                color = PrimaryDeep,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Usar esta dirección por defecto.",
                                color = TextSoft,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Switch(
                            checked = esPredeterminada,
                            onCheckedChange = {
                                esPredeterminada = it
                                error = null
                            }
                        )
                    }
                }
            }

            if (error != null) {
                Text(
                    text = error ?: "Error",
                    color = DangerRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    val aliasLimpio = alias.trim()
                    val direccionLimpia = direccionTexto.trim()

                    if (aliasLimpio.isBlank()) {
                        error = "Ingresá un alias."
                        return@Button
                    }

                    if (direccionLimpia.isBlank()) {
                        error = "Ingresá la dirección."
                        return@Button
                    }

                    val lat = latitud.toDoubleOrNull() ?: selectedLatLng.latitude
                    val lon = longitud.toDoubleOrNull() ?: selectedLatLng.longitude

                    scope.launch {
                        saving = true
                        error = null

                        try {
                            val clienteId = sessionManager.getClienteId()
                                ?: throw IllegalStateException("No se encontró el cliente en sesión.")

                            val request = ClienteDireccionRequest(
                                alias = aliasLimpio,
                                direccionTexto = direccionLimpia,
                                latitud = lat,
                                longitud = lon,
                                esPredeterminada = esPredeterminada
                            )

                            if (direccionId == null) {
                                repository.crear(
                                    clienteId = clienteId,
                                    request = request
                                )
                            } else {
                                repository.actualizar(
                                    clienteId = clienteId,
                                    direccionId = direccionId,
                                    request = request
                                )
                            }

                            onSaved()
                        } catch (e: Exception) {
                            error = e.toUserMessage()
                        } finally {
                            saving = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !saving,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = null
                    )

                    Text(
                        text = if (direccionId == null) "  Guardar" else "  Actualizar"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MapLocationPicker(
    selectedLatLng: LatLng,
    onLocationSelected: (LatLng) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            selectedLatLng,
            15f
        )
    }

    val markerState = remember {
        MarkerState(position = selectedLatLng)
    }

    LaunchedEffect(selectedLatLng) {
        markerState.position = selectedLatLng
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            selectedLatLng,
            15f
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ubicación en mapa",
            color = PrimaryDeep,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Tocá el mapa para seleccionar el punto exacto de entrega.",
            color = TextSoft,
            style = MaterialTheme.typography.bodySmall
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = BackgroundSoft
            )
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    myLocationButtonEnabled = false
                ),
                onMapClick = { latLng ->
                    onLocationSelected(latLng)
                }
            ) {
                Marker(
                    state = markerState,
                    title = "Punto de entrega",
                    snippet = "Ubicación seleccionada"
                )
            }
        }
    }
}

@Composable
private fun DataModeFormCard(
    mode: DataMode
) {
    val icon = when (mode) {
        DataMode.API -> Icons.Rounded.Storage
        DataMode.LOCAL -> Icons.Rounded.PhoneAndroid
        DataMode.CLOUD -> Icons.Rounded.Cloud
    }

    val title = when (mode) {
        DataMode.API -> "Guardando en API remota"
        DataMode.LOCAL -> "Guardando en SQLite local"
        DataMode.CLOUD -> "Guardando en modo Cloud"
    }

    val message = when (mode) {
        DataMode.API -> "La dirección se guarda en el backend y se copia localmente."
        DataMode.LOCAL -> "La dirección queda guardada en el dispositivo para uso offline."
        DataMode.CLOUD -> "La dirección se guarda directamente en Firebase Cloud."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = PrimaryGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = message,
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}