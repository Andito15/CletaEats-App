package com.cletaeats.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cletaeats.app.data.model.ClienteDireccionResponse
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.data.remote.toUserMessage
import com.cletaeats.app.data.repository.ClienteDireccionRepository
import com.cletaeats.app.data.sync.ClienteDireccionSyncManager
import com.cletaeats.app.domain.datamode.DataMode
import com.cletaeats.app.domain.datamode.DataModeManager
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.components.CletaScaffold
import com.cletaeats.app.ui.components.EmptyState
import com.cletaeats.app.ui.components.ErrorState
import com.cletaeats.app.ui.components.LoadingBox
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch

@Composable
fun DireccionesScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit
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

    val syncManager = remember {
        ClienteDireccionSyncManager(
            context = context,
            api = api
        )
    }

    val scope = rememberCoroutineScope()

    var direcciones by remember {
        mutableStateOf<List<ClienteDireccionResponse>>(emptyList())
    }

    var search by remember {
        mutableStateOf("")
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var actionLoading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var syncMessage by remember {
        mutableStateOf<String?>(null)
    }

    fun cargarDirecciones() {
        scope.launch {
            loading = true
            error = null
            syncMessage = null

            try {
                val clienteId = sessionManager.getClienteId()
                    ?: throw IllegalStateException("No se encontró el cliente en sesión.")

                direcciones = if (search.isBlank()) {
                    repository.listar(clienteId)
                } else {
                    repository.buscar(
                        clienteId = clienteId,
                        query = search
                    )
                }

                syncMessage = when (dataMode) {
                    DataMode.API -> "Datos cargados desde API remota."
                    DataMode.LOCAL -> "Datos cargados desde SQLite local."
                    DataMode.CLOUD -> "Datos cargados desde modo Cloud."
                }
            } catch (e: Exception) {
                error = e.toUserMessage()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDirecciones()
    }

    LaunchedEffect(search) {
        if (!loading) {
            cargarDirecciones()
        }
    }

    CletaScaffold(
        title = "Direcciones",
        onBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    scope.launch {
                        actionLoading = true
                        error = null

                        try {
                            val result = syncManager.sincronizarPendientes()
                            syncMessage = result.message
                            cargarDirecciones()
                        } catch (e: Exception) {
                            error = e.toUserMessage()
                        } finally {
                            actionLoading = false
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = "Sincronizar",
                    tint = PrimaryGreen
                )
            }

            IconButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Agregar",
                    tint = PrimaryGreen
                )
            }
        }
    ) { modifier ->
        when {
            loading -> LoadingBox(modifier = modifier)

            error != null && direcciones.isEmpty() -> ErrorState(
                message = error ?: "Error inesperado.",
                onRetry = { cargarDirecciones() },
                modifier = modifier
            )

            else -> LazyColumn(
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
                    DataModeInfoCard(
                        mode = dataMode,
                        message = syncMessage
                    )
                }

                item {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        },
                        label = {
                            Text("Buscar dirección")
                        }
                    )
                }

                if (direcciones.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Rounded.LocationOn,
                            title = "Sin direcciones",
                            message = if (search.isBlank()) {
                                "Agregá una dirección de entrega."
                            } else {
                                "No hay resultados para la búsqueda."
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        )
                    }

                    item {
                        Button(
                            onClick = onAdd,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )

                            Text("  Agregar")
                        }
                    }
                } else {
                    items(
                        items = direcciones,
                        key = { direccion ->
                            direccion.direccionId ?: direccion.direccionTexto.hashCode().toLong()
                        }
                    ) { direccion ->
                        DireccionCard(
                            direccion = direccion,
                            actionLoading = actionLoading,
                            onEdit = {
                                direccion.direccionId?.let { id ->
                                    onEdit(id)
                                }
                            },
                            onPredeterminada = {
                                scope.launch {
                                    actionLoading = true
                                    error = null

                                    try {
                                        val clienteId = sessionManager.getClienteId()
                                            ?: throw IllegalStateException("No se encontró el cliente en sesión.")

                                        val direccionId = direccion.direccionId
                                            ?: throw IllegalStateException("Dirección inválida.")

                                        repository.marcarPredeterminada(
                                            clienteId = clienteId,
                                            direccionId = direccionId
                                        )

                                        cargarDirecciones()
                                    } catch (e: Exception) {
                                        error = e.toUserMessage()
                                    } finally {
                                        actionLoading = false
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    actionLoading = true
                                    error = null

                                    try {
                                        val clienteId = sessionManager.getClienteId()
                                            ?: throw IllegalStateException("No se encontró el cliente en sesión.")

                                        val direccionId = direccion.direccionId
                                            ?: throw IllegalStateException("Dirección inválida.")

                                        repository.eliminar(
                                            clienteId = clienteId,
                                            direccionId = direccionId
                                        )

                                        cargarDirecciones()
                                    } catch (e: Exception) {
                                        error = e.toUserMessage()
                                    } finally {
                                        actionLoading = false
                                    }
                                }
                            }
                        )
                    }
                }

                if (actionLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryGreen
                            )
                        }
                    }
                }

                if (error != null) {
                    item {
                        Text(
                            text = error ?: "Error",
                            color = DangerRed,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun DataModeInfoCard(
    mode: DataMode,
    message: String?
) {
    val icon = when (mode) {
        DataMode.API -> Icons.Rounded.Storage
        DataMode.LOCAL -> Icons.Rounded.PhoneAndroid
        DataMode.CLOUD -> Icons.Rounded.Cloud
    }

    val title = when (mode) {
        DataMode.API -> "Modo API remota"
        DataMode.LOCAL -> "Modo SQLite local"
        DataMode.CLOUD -> "Modo Cloud"
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
                    text = message ?: "Modo activo durante esta sesión.",
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DireccionCard(
    direccion: ClienteDireccionResponse,
    actionLoading: Boolean,
    onEdit: () -> Unit,
    onPredeterminada: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = PrimaryGreen
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = direccion.alias,
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = direccion.direccionTexto,
                        color = TextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onPredeterminada,
                    enabled = !actionLoading
                ) {
                    Icon(
                        imageVector = if (direccion.esPredeterminada == true) {
                            Icons.Rounded.Star
                        } else {
                            Icons.Rounded.StarBorder
                        },
                        contentDescription = "Predeterminada",
                        tint = PrimaryGreen
                    )
                }

                IconButton(
                    onClick = onDelete,
                    enabled = !actionLoading
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Eliminar",
                        tint = DangerRed
                    )
                }
            }

            if (direccion.esPredeterminada == true) {
                Divider()

                Text(
                    text = "Predeterminada",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}