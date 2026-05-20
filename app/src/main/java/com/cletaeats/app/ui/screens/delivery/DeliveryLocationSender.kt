package com.cletaeats.app.ui.screens.delivery

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cletaeats.app.data.model.UbicacionRepartidorRequest
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.data.remote.toDeliveryMessage
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

@Composable
fun DeliveryLocationSender(
    pedidoId: Long,
    enabled: Boolean
) {
    val context = LocalContext.current

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    var hasPermission by remember {
        mutableStateOf(hasLocationPermission())
    }

    var permissionRequested by remember {
        mutableStateOf(false)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        permissionRequested = true
    }

    LaunchedEffect(enabled) {
        hasPermission = hasLocationPermission()
    }

    if (!enabled) {
        TrackingWaitingCard()
        return
    }

    if (!hasPermission) {
        TrackingPermissionCard(
            permissionRequested = permissionRequested,
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )
        return
    }

    DeliveryLocationUpdates(
        pedidoId = pedidoId
    )
}

@Composable
private fun TrackingWaitingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.GpsFixed,
                contentDescription = null,
                tint = TextSoft
            )

            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = "Tracking listo",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Marcá el pedido como EN CAMINO para iniciar la ubicación.",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TrackingPermissionCard(
    permissionRequested: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOff,
                    contentDescription = null,
                    tint = DangerRed
                )

                Text(
                    text = "  Ubicación requerida",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = if (permissionRequested) {
                    "No se pudo activar el permiso. Revisá los permisos de ubicación en ajustes."
                } else {
                    "Activá la ubicación para que el cliente pueda ver la entrega en tiempo real."
                },
                color = TextSoft,
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = null
                )

                Text("  Activar ubicación")
            }
        }
    }
}

@Composable
private fun TrackingActiveCard(
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.GpsFixed,
                contentDescription = null,
                tint = PrimaryGreen
            )

            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = "Tracking activo",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = status,
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun DeliveryLocationUpdates(
    pedidoId: Long
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var status by remember {
        mutableStateOf("Esperando señal GPS...")
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(1f)
            .build()
    }

    fun enviarUbicacion(
        latitud: Double,
        longitud: Double,
        precision: Double?
    ) {
        scope.launch {
            runCatching {
                api.actualizarUbicacionRepartidor(
                    UbicacionRepartidorRequest(
                        pedidoId = pedidoId,
                        latitud = latitud,
                        longitud = longitud,
                        precisionMetros = precision
                    )
                )
            }.onSuccess {
                status = "Ubicación enviada al cliente."
            }.onFailure {
                status = it.toDeliveryMessage()
            }
        }
    }

    LaunchedEffect(pedidoId) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    enviarUbicacion(
                        latitud = location.latitude,
                        longitud = location.longitude,
                        precision = location.accuracy.toDouble()
                    )
                } else {
                    status = "Sin ubicación inicial. Configurá GPS en el emulador."
                }
            }
            .addOnFailureListener {
                status = "No se pudo leer ubicación inicial."
            }
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                enviarUbicacion(
                    latitud = location.latitude,
                    longitud = location.longitude,
                    precision = location.accuracy.toDouble()
                )
            }
        }
    }

    DisposableEffect(pedidoId) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    TrackingActiveCard(status = status)
}