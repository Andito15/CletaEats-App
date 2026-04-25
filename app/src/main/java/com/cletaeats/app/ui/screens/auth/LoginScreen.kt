package com.cletaeats.app.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cletaeats.app.R
import com.cletaeats.app.data.model.LoginRequest
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.domain.session.SessionManager
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

private fun validarCorreoManual(correo: String): String? {
    val limpio = correo.trim()
    if (limpio.isBlank()) return "Correo obligatorio"
    if (limpio.contains(" ")) return "Sin espacios"
    if (!Patterns.EMAIL_ADDRESS.matcher(limpio).matches()) return "Correo inválido"
    return null
}

private fun validarPasswordManual(password: String): String? {
    if (password.isBlank()) return "Contraseña obligatoria"
    if (password.trim().length < 4) return "Muy corta"
    return null
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val apiService = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var correoError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_completo),
                    contentDescription = "CletaEats",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        correoError = validarCorreoManual(it)
                        generalError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo") },
                    placeholder = { Text("correo@ejemplo.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    isError = correoError != null
                )

                if (correoError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = correoError ?: "",
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = validarPasswordManual(it)
                        generalError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (passwordVisible) {
                                    "Ocultar"
                                } else {
                                    "Mostrar"
                                }
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    isError = passwordError != null
                )

                if (passwordError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = passwordError ?: "",
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (generalError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = generalError ?: "",
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val correoValidacion = validarCorreoManual(correo)
                        val passwordValidacion = validarPasswordManual(password)

                        correoError = correoValidacion
                        passwordError = passwordValidacion
                        generalError = null

                        if (correoValidacion != null || passwordValidacion != null) {
                            return@Button
                        }

                        scope.launch {
                            loading = true

                            try {
                                val response = apiService.login(
                                    LoginRequest(
                                        correo = correo.trim(),
                                        password = password
                                    )
                                )

                                if (response.success && !response.token.isNullOrBlank()) {
                                    val rolApp = if (response.rol == "REPARTIDOR") {
                                        "REPARTIDOR"
                                    } else {
                                        "CLIENTE"
                                    }

                                    sessionManager.saveToken(response.token)
                                    sessionManager.saveUserData(
                                        usuarioId = response.usuarioId,
                                        clienteId = response.clienteId,
                                        repartidorId = response.repartidorId,
                                        nombre = response.nombre,
                                        correo = response.correo,
                                        rol = rolApp
                                    )

                                    onLoginSuccess()
                                } else {
                                    generalError = response.message.ifBlank { "No se pudo iniciar sesión" }
                                }
                            } catch (e: Exception) {
                                generalError = e.message ?: "Error de conexión"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Entrar"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onGoToRegister) {
                    Text("Crear cuenta", color = PrimaryGreen)
                }
            }
        }
    }
}