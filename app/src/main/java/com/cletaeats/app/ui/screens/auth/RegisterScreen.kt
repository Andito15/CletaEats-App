package com.cletaeats.app.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.cletaeats.app.data.model.RegisterRequest
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.data.remote.toUserMessage
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

private fun validarNombre(nombre: String): String? {
    val limpio = nombre.trim()
    if (limpio.isBlank()) return "Nombre obligatorio"
    if (limpio.length < 3) return "Muy corto"
    return null
}

private fun validarCedula(cedula: String): String? {
    val limpio = cedula.trim()
    if (limpio.isBlank()) return "Cédula obligatoria"
    if (limpio.contains(" ")) return "Sin espacios"
    if (limpio.length < 6) return "Cédula inválida"
    return null
}

private fun validarCorreo(correo: String): String? {
    val limpio = correo.trim()
    if (limpio.isBlank()) return "Correo obligatorio"
    if (limpio.contains(" ")) return "Sin espacios"
    if (!Patterns.EMAIL_ADDRESS.matcher(limpio).matches()) return "Correo inválido"
    return null
}

private fun validarTelefono(telefono: String): String? {
    val limpio = telefono.trim()
    if (limpio.isBlank()) return "Teléfono obligatorio"
    if (limpio.length < 8) return "Teléfono inválido"
    return null
}

private fun validarPassword(password: String): String? {
    if (password.isBlank()) return "Contraseña obligatoria"
    if (password.length < 6) return "Mínimo 6"
    return null
}

private fun validarConfirmPassword(
    password: String,
    confirmPassword: String
): String? {
    if (confirmPassword.isBlank()) return "Confirmá la contraseña"
    if (password != confirmPassword) return "No coincide"
    return null
}

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val apiService = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var rol by remember { mutableStateOf("CLIENTE") }
    var nombre by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var nombreError by remember { mutableStateOf<String?>(null) }
    var cedulaError by remember { mutableStateOf<String?>(null) }
    var correoError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var generalError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun validarTodo(): Boolean {
        val nombreVal = validarNombre(nombre)
        val cedulaVal = validarCedula(cedula)
        val correoVal = validarCorreo(correo)
        val telefonoVal = validarTelefono(telefono)
        val passwordVal = validarPassword(password)
        val confirmVal = validarConfirmPassword(password, confirmPassword)

        nombreError = nombreVal
        cedulaError = cedulaVal
        correoError = correoVal
        telefonoError = telefonoVal
        passwordError = passwordVal
        confirmPasswordError = confirmVal

        return listOf(
            nombreVal,
            cedulaVal,
            correoVal,
            telefonoVal,
            passwordVal,
            confirmVal
        ).all { it == null }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
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
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleButton(
                        selected = rol == "CLIENTE",
                        icon = Icons.Rounded.Person,
                        contentDescription = "Cliente",
                        onClick = {
                            rol = "CLIENTE"
                            generalError = null
                        },
                        modifier = Modifier.weight(1f)
                    )

                    RoleButton(
                        selected = rol == "REPARTIDOR",
                        icon = Icons.Rounded.DeliveryDining,
                        contentDescription = "Repartidor",
                        onClick = {
                            rol = "REPARTIDOR"
                            generalError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = validarNombre(it)
                        generalError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    isError = nombreError != null
                )
                ErrorText(nombreError)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cedula,
                    onValueChange = {
                        cedula = it.filter { char -> char.isDigit() }.take(12)
                        cedulaError = validarCedula(cedula)
                        generalError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cédula") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Badge,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    isError = cedulaError != null
                )
                ErrorText(cedulaError)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        correoError = validarCorreo(it)
                        generalError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    isError = correoError != null
                )
                ErrorText(correoError)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it.filter { char -> char.isDigit() }.take(8)
                        telefonoError = validarTelefono(telefono)
                        generalError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Teléfono") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    isError = telefonoError != null
                )
                ErrorText(telefonoError)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = validarPassword(it)
                        confirmPasswordError = validarConfirmPassword(it, confirmPassword)
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
                                contentDescription = null
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
                ErrorText(passwordError)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = validarConfirmPassword(password, it)
                        generalError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirmar") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    isError = confirmPasswordError != null
                )
                ErrorText(confirmPasswordError)

                if (generalError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = generalError.orEmpty(),
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        generalError = null

                        if (!validarTodo()) return@Button

                        scope.launch {
                            loading = true

                            try {
                                val response = apiService.register(
                                    RegisterRequest(
                                        rol = rol,
                                        nombre = nombre.trim(),
                                        cedula = cedula.trim(),
                                        correo = correo.trim().lowercase(),
                                        telefono = telefono.trim(),
                                        password = password
                                    )
                                )

                                if (response.success) {
                                    onBackToLogin()
                                } else {
                                    generalError = response.message.ifBlank {
                                        "No se pudo crear la cuenta."
                                    }
                                }
                            } catch (e: Exception) {
                                generalError = e.toUserMessage()
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
                            contentDescription = "Crear cuenta"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onBackToLogin,
                    enabled = !loading
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) PrimaryGreen else BackgroundSoft,
            contentColor = if (selected) Color.White else PrimaryGreen
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun ErrorText(
    message: String?
) {
    if (message != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            color = Color.Red,
            modifier = Modifier.fillMaxWidth()
        )
    }
}