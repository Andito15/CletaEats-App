package com.cletaeats.app.ui.screens.client

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Route
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.core.content.ContextCompat
import com.cletaeats.app.data.model.UbicacionRepartidorResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.data.remote.toTrackingMessage
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.components.cleanStateLabel
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun PedidoTrackingScreen(
    pedidoId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val api = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

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

    var tracking by remember {
        mutableStateOf<UbicacionRepartidorResponse?>(null)
    }

    var clientLocation by remember {
        mutableStateOf<Location?>(null)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    DisposableEffect(pedidoId) {
        val job: Job = scope.launch {
            while (isActive) {
                try {
                    tracking = api.obtenerTrackingPedido(pedidoId)
                    error = null
                } catch (e: Exception) {
                    error = e.toTrackingMessage()
                } finally {
                    loading = false
                }

                delay(3000)
            }
        }

        onDispose {
            job.cancel()
        }
    }

    if (hasPermission) {
        ClientLocationTracker(
            onLocationChanged = { location ->
                clientLocation = location
            }
        )
    }

    CletaScaffold(
        title = "Entrega en vivo",
        onBack = onBack
    ) { modifier ->
        when {
            loading -> LoadingBox(modifier = modifier)

            tracking?.latitud == null || tracking?.longitud == null -> EmptyState(
                icon = Icons.Rounded.LocationOff,
                title = "Sin ubicación todavía",
                message = error ?: "El repartidor aún no ha iniciado el tracking.",
                modifier = modifier.fillMaxSize()
            )

            !hasPermission -> LocationPermissionCard(
                modifier = modifier.fillMaxSize(),
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            else -> TrackingMap(
                tracking = tracking!!,
                clientLocation = clientLocation,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun ClientLocationTracker(
    onLocationChanged: (Location) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(2f)
            .build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                onLocationChanged(location)
            }
        }
    }

    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationChanged(location)
            }
        }
    }

    DisposableEffect(Unit) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}

@Composable
private fun LocationPermissionCard(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = modifier.padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOff,
                    contentDescription = null,
                    tint = DangerRed
                )

                Text(
                    text = "Ubicación requerida",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Activá tu ubicación para ver la línea entre vos y el repartidor.",
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
}

@Composable
private fun TrackingMap(
    tracking: UbicacionRepartidorResponse,
    clientLocation: Location?,
    modifier: Modifier = Modifier
) {
    val repartidorPosition = LatLng(
        tracking.latitud ?: 0.0,
        tracking.longitud ?: 0.0
    )

    val clientePosition = clientLocation?.let {
        LatLng(
            it.latitude,
            it.longitude
        )
    }

    val centerPosition = if (clientePosition != null) {
        LatLng(
            (repartidorPosition.latitude + clientePosition.latitude) / 2.0,
            (repartidorPosition.longitude + clientePosition.longitude) / 2.0
        )
    } else {
        repartidorPosition
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            centerPosition,
            if (clientePosition != null) 14f else 16f
        )
    }

    val repartidorMarkerState = rememberMarkerState(
        position = repartidorPosition
    )

    val clienteMarkerState = rememberMarkerState(
        position = clientePosition ?: repartidorPosition
    )

    var distanciaRestanteKm by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(
        tracking.latitud,
        tracking.longitud,
        clientLocation?.latitude,
        clientLocation?.longitude
    ) {
        repartidorMarkerState.position = repartidorPosition

        if (clientePosition != null) {
            clienteMarkerState.position = clientePosition

            val results = FloatArray(1)
            Location.distanceBetween(
                repartidorPosition.latitude,
                repartidorPosition.longitude,
                clientePosition.latitude,
                clientePosition.longitude,
                results
            )

            distanciaRestanteKm = results[0] / 1000f

            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                centerPosition,
                14f
            )
        } else {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                repartidorPosition,
                16f
            )
        }
    }

    Box(
        modifier = modifier
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = true,
                myLocationButtonEnabled = false
            )
        ) {
            Marker(
                state = repartidorMarkerState,
                title = tracking.repartidorNombre ?: "Repartidor",
                snippet = "Ubicación actual",
                icon = BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_GREEN
                )
            )

            if (clientePosition != null) {
                Marker(
                    state = clienteMarkerState,
                    title = "Tu ubicación",
                    snippet = "Destino actual",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    )
                )

                Polyline(
                    points = listOf(
                        repartidorPosition,
                        clientePosition
                    ),
                    color = PrimaryGreen,
                    width = 10f
                )
            }
        }

        TrackingInfoCard(
            tracking = tracking,
            clientLocation = clientLocation,
            distanciaRestanteKm = distanciaRestanteKm,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun TrackingInfoCard(
    tracking: UbicacionRepartidorResponse,
    clientLocation: Location?,
    distanciaRestanteKm: Float,
    modifier: Modifier = Modifier
) {
    val etaMin = if (distanciaRestanteKm > 0f) {
        max(
            1,
            ((distanciaRestanteKm / 22f) * 60f).roundToInt()
        )
    } else {
        null
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeliveryDining,
                    contentDescription = null,
                    tint = PrimaryGreen
                )

                Text(
                    text = "  ${tracking.repartidorNombre ?: "Repartidor"}",
                    color = PrimaryDeep,
                    fontWeight = FontWeight.Bold
                )
            }

            InfoText(
                icon = Icons.Rounded.Route,
                text = "Estado: ${cleanStateLabel(tracking.estadoPedido ?: "EN_CAMINO")}"
            )

            tracking.ultimaUbicacionEn?.let {
                InfoText(
                    icon = Icons.Rounded.GpsFixed,
                    text = "Última actualización: $it"
                )
            }

            tracking.precisionMetros?.let {
                InfoText(
                    icon = Icons.Rounded.GpsFixed,
                    text = "Precisión repartidor: ${"%.1f".format(it)} m"
                )
            }

            if (clientLocation != null) {
                InfoText(
                    icon = Icons.Rounded.Place,
                    text = "Distancia restante aprox.: ${"%.2f".format(distanciaRestanteKm)} km"
                )

                etaMin?.let {
                    Text(
                        text = "Llegada estimada: $it min",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "Obteniendo tu ubicación...",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Actualizando cada 3 segundos",
                color = PrimaryGreen,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun InfoText(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSoft
        )

        Text(
            text = "  $text",
            color = TextSoft,
            style = MaterialTheme.typography.bodySmall
        )
    }
}