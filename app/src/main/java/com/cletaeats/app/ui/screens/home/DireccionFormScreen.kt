package com.cletaeats.app.ui.screens.home

import android.Manifest
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.cletaeats.app.data.model.ClienteDireccionRequest
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.Locale

private fun validarAlias(alias: String): String? {
    if (alias.trim().isBlank()) return "Alias obligatorio"
    if (alias.trim().length > 50) return "Muy largo"
    return null
}

private fun validarDireccion(direccion: String): String? {
    if (direccion.trim().isBlank()) return "Dirección obligatoria"
    return null
}

@Composable
fun DireccionFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val apiService = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()
    val clienteId = sessionManager.getClienteId()

    var alias by remember { mutableStateOf("") }
    var direccionTexto by remember { mutableStateOf("") }
    var esPredeterminada by remember { mutableStateOf(false) }

    var aliasError by remember { mutableStateOf<String?>(null) }
    var direccionError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    var markerPosition by remember { mutableStateOf(LatLng(9.9281, -84.0907)) }
    var hasPickedLocation by remember { mutableStateOf(false) }

    val markerState = remember { MarkerState(position = markerPosition) }

    LaunchedEffect(markerPosition) {
        markerState.position = markerPosition
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 14f)
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun actualizarDireccionDesdeCoordenadas(latLng: LatLng) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                    val direccion = addresses.firstOrNull()?.getAddressLine(0)
                    if (!direccion.isNullOrBlank()) {
                        direccionTexto = direccion
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                val direccion = addresses?.firstOrNull()?.getAddressLine(0)
                if (!direccion.isNullOrBlank()) {
                    direccionTexto = direccion
                }
            }
        } catch (_: Exception) {
        }
    }

    fun usarUbicacionActual() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val point = LatLng(location.latitude, location.longitude)
                        markerPosition = point
                        hasPickedLocation = true
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(point, 17f)
                            )
                        }
                        actualizarDireccionDesdeCoordenadas(point)
                    } else {
                        generalError = "No se pudo obtener ubicación"
                    }
                }
                .addOnFailureListener {
                    generalError = "No se pudo obtener ubicación"
                }
        } catch (_: SecurityException) {
            generalError = "Permiso requerido"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            usarUbicacionActual()
        } else {
            generalError = "Permiso denegado"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .verticalScroll(rememberScrollState())
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledIconButton(onClick = { alias = "Casa" }) {
                        Icon(Icons.Rounded.Home, contentDescription = "Casa")
                    }

                    FilledIconButton(onClick = { alias = "Trabajo" }) {
                        Icon(Icons.Rounded.Work, contentDescription = "Trabajo")
                    }

                    FilledIconButton(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    ) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = "Ubicación actual")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = alias,
                    onValueChange = {
                        alias = it
                        aliasError = validarAlias(it)
                        generalError = null
                    },
                    label = { Text("Alias") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = aliasError != null,
                    singleLine = true
                )

                if (aliasError != null) {
                    Text(aliasError!!, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = direccionTexto,
                    onValueChange = {
                        direccionTexto = it
                        direccionError = validarDireccion(it)
                        generalError = null
                    },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = direccionError != null
                )

                if (direccionError != null) {
                    Text(direccionError!!, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = esPredeterminada,
                        onCheckedChange = { esPredeterminada = it }
                    )
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = PrimaryGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        markerPosition = latLng
                        hasPickedLocation = true
                        actualizarDireccionDesdeCoordenadas(latLng)
                    }
                ) {
                    if (hasPickedLocation) {
                        Marker(
                            state = markerState,
                            title = alias.ifBlank { "Ubicación" }
                        )
                    }
                }

                if (generalError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(generalError!!, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val aliasVal = validarAlias(alias)
                        val direccionVal = validarDireccion(direccionTexto)

                        aliasError = aliasVal
                        direccionError = direccionVal
                        generalError = null

                        if (clienteId == null) {
                            generalError = "Cliente no disponible"
                            return@Button
                        }

                        if (!hasPickedLocation) {
                            generalError = "Seleccioná ubicación"
                            return@Button
                        }

                        if (aliasVal != null || direccionVal != null) return@Button

                        scope.launch {
                            loading = true
                            try {
                                apiService.crearDireccion(
                                    clienteId = clienteId,
                                    request = ClienteDireccionRequest(
                                        alias = alias.trim(),
                                        direccionTexto = direccionTexto.trim(),
                                        latitud = markerPosition.latitude,
                                        longitud = markerPosition.longitude,
                                        esPredeterminada = esPredeterminada
                                    )
                                )
                                onSaved()
                            } catch (e: Exception) {
                                generalError = e.message ?: "Error"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Guardar"
                        )
                    }
                }
            }
        }
    }
}