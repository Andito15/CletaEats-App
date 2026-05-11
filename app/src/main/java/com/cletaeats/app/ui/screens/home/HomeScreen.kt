package com.cletaeats.app.ui.screens.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cletaeats.app.R
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.components.IconBubble
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.DangerRed
import com.cletaeats.app.ui.theme.PrimaryDeep
import com.cletaeats.app.ui.theme.PrimaryGreen
import com.cletaeats.app.ui.theme.TextSoft
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onRestaurantes: () -> Unit,
    onDirecciones: () -> Unit,
    onMisPedidos: () -> Unit,
    onPedidosRepartidor: () -> Unit,
    onLogout: () -> Unit
){
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val nombre = sessionManager.getNombre().orEmpty()
    val correo = sessionManager.getCorreo().orEmpty()
    val rol = sessionManager.getRol().orEmpty()

    val logoutAction = {
        sessionManager.clearSession()
        onLogout()
    }

    HomeDrawerScaffold(
        nombre = nombre,
        correo = correo,
        rol = rol,
        onRestaurantes = onRestaurantes,
        onDirecciones = onDirecciones,
        onMisPedidos = onMisPedidos,
        onPedidosRepartidor = onPedidosRepartidor,
        onLogout = logoutAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeDrawerScaffold(
    nombre: String,
    correo: String,
    rol: String,
    onRestaurantes: () -> Unit,
    onDirecciones: () -> Unit,
    onMisPedidos: () -> Unit,
    onPedidosRepartidor: () -> Unit,
    onLogout: () -> Unit
){
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isDelivery = rol == "REPARTIDOR"

    fun closeAndGo(action: () -> Unit) {
        scope.launch { drawerState.close() }
        action()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_solo),
                        contentDescription = "CletaEats",
                        modifier = Modifier.size(76.dp)
                    )

                    Text(
                        text = nombre.ifBlank { if (isDelivery) "Repartidor" else "Cliente" },
                        color = PrimaryDeep,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = correo.ifBlank { "Sesión activa" },
                        color = TextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = true,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                if (isDelivery) {
                    NavigationDrawerItem(
                        label = { Text("Pedidos asignados") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.DeliveryDining,
                                contentDescription = null
                            )
                        },
                        onClick = { closeAndGo(onPedidosRepartidor) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text("Restaurantes") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Restaurant,
                                contentDescription = null
                            )
                        },
                        onClick = { closeAndGo(onRestaurantes) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Historial") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null
                            )
                        },
                        onClick = { closeAndGo(onMisPedidos) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Direcciones") },
                        selected = false,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null
                            )
                        },
                        onClick = { closeAndGo(onDirecciones) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                }

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Salir",
                            color = DangerRed
                        )
                    },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Logout,
                            contentDescription = null,
                            tint = DangerRed
                        )
                    },
                    onClick = onLogout,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 12.dp
                    )
                )
            }
        }
    ) {
        Scaffold(
            containerColor = BackgroundSoft,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isDelivery) "CletaEats Rider" else "CletaEats",
                            color = PrimaryDeep,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menú"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.Rounded.Logout,
                                contentDescription = "Salir",
                                tint = DangerRed
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BackgroundSoft
                    )
                )
            }
        ) { padding ->
            if (isDelivery) {
                DeliveryHomeScreen(
                    nombre = nombre,
                    correo = correo,
                    onPedidos = onPedidosRepartidor,
                    modifier = Modifier.padding(padding)
                )
            } else {
                ClientHomeScreen(
                    nombre = nombre,
                    correo = correo,
                    onRestaurantes = onRestaurantes,
                    onDirecciones = onDirecciones,
                    onMisPedidos = onMisPedidos,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(icon = icon)

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = PrimaryDeep,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = subtitle,
                    color = TextSoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ClientHomeScreen(
    nombre: String,
    correo: String,
    onRestaurantes: () -> Unit,
    onDirecciones: () -> Unit,
    onMisPedidos: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeCard(
            nombre = nombre.ifBlank { "Cliente" },
            correo = correo,
            icon = Icons.Rounded.Person,
            headline = "Listo para ordenar"
        )

        DashboardCard(
            title = "Restaurantes",
            subtitle = "Elegí comida, combos y confirmá el pedido.",
            icon = Icons.Rounded.Restaurant,
            onClick = onRestaurantes
        )

        DashboardCard(
            title = "Historial",
            subtitle = "Estados, facturas y pedidos entregados.",
            icon = Icons.Rounded.History,
            onClick = onMisPedidos
        )

        DashboardCard(
            title = "Direcciones",
            subtitle = "Casa, trabajo y ubicación para entrega.",
            icon = Icons.Rounded.LocationOn,
            onClick = onDirecciones
        )
    }
}

@Composable
fun DeliveryHomeScreen(
    nombre: String,
    correo: String,
    onPedidos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeCard(
            nombre = nombre.ifBlank { "Repartidor" },
            correo = correo,
            icon = Icons.Rounded.DeliveryDining,
            headline = "Ruta activa"
        )

        DashboardCard(
            title = "Pedidos asignados",
            subtitle = "Revisá dirección, detalle y actualizá el estado.",
            icon = Icons.Rounded.DeliveryDining,
            onClick = onPedidos
        )
    }
}

@Composable
private fun WelcomeCard(
    nombre: String,
    correo: String,
    icon: ImageVector,
    headline: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = headline,
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = nombre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                if (correo.isNotBlank()) {
                    Text(
                        text = correo,
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}