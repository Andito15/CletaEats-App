package com.cletaeats.app.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cletaeats.app.data.model.RegisterRequest
import com.cletaeats.app.data.remote.RetrofitProvider
import com.cletaeats.app.ui.theme.BackgroundSoft
import com.cletaeats.app.ui.theme.PrimaryGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun validarRol(rol: String): String? {
    if (rol != "CLIENTE" && rol != "REPARTIDOR") return "Rol inválido"
    return null
}

private fun validarNombre(nombre: String): String? {
    if (nombre.trim().isBlank()) return "Nombre obligatorio"
    if (nombre.trim().length < 3) return "Muy corto"
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
    if (limpio.contains(" ")) return "Sin espacios"
    if (limpio.length < 8) return "Teléfono inválido"
    return null
}

private fun validarPassword(password: String): String? {
    if (password.isBlank()) return "Contraseña obligatoria"
    if (password.length < 6) return "Mínimo 6"
    return null
}

private fun validarConfirmPassword(password: String, confirmPassword: String): String? {
    if (confirmPassword.isBlank()) return "Confirmá"
    if (password != confirmPassword) return "No coincide"
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val apiService = remember { RetrofitProvider.createApiService(context) }
    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }
    var rol by remember { mutableStateOf("CLIENTE") }
    var nombre by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var rolError by remember { mutableStateOf<String?>(null) }
    var nombreError by remember { mutableStateOf<String?>(null) }
    var cedulaError by remember { mutableStateOf<String?>(null) }
    var correoError by remember { mutableStateOf<String?>(null) }
    var telefonoError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

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
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registro",
                    color = PrimaryGreen,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = rol,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        label = { Text("Rol") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        isError = rolError != null,
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("CLIENTE", "REPARTIDOR").forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    rol = opcion
                                    rolError = validarRol(opcion)
                                    generalError = null
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (rolError != null) {
                    Text(rolError!!, color = Color.Red, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                        Icon(Icons.Rounded.AccountCircle, contentDescription = null)
                    },
                    isError = nombreError != null,
                    singleLine = true
                )
                if (nombreError != null) {
                    Text(nombreError!!, color = Color.Red, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cedula,
                    onValueChange = {
                        cedula = it
                        cedulaError = validarCedula(it)
                        generalError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cédula") },
                    leadingIcon = {
                        Icon(Icons.Rounded.AccountCircle, contentDescription = null)
                    },
                    isError = cedulaError != null,
                    singleLine = true
                )
                if (cedulaError != null) {
                    Text(cedulaError!!, color = Color.Red, modifier = Modifier.fillMaxWidth())
                }

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
                        Icon(Icons.Rounded.AccountCircle, contentDescription = null)
                    },
                    isError = correoError != null,
                    singleLine = true
                )
                if (correoError != null) {
                    Text(correoError!!, color = Color.Red, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it
                        telefonoError = validarTelefono(it)
                        generalError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Teléfono") },
                    leadingIcon = {
                        Icon(Icons.Rounded.AccountCircle, contentDescription = null)
                    },
                    isError = telefonoError != null,
                    singleLine = true
                )
                if (telefonoError != null) {
                    Text(telefonoError!!, color = Color.Red, modifier = Modifier.fillMaxWidth())
                }

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
                        Icon(Icons.Rounded.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = passwordError != null,
                    singleLine = true
                )
                if (passwordError != null) {
                    Text(passwordError!!, color = Color.Red, modifier = Modifier.fillMaxWidth())
                }

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
                        Icon(Icons.Rounded.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (confirmPasswordVisible) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = confirmPasswordError != null,
                    singleLine = true
                )
                if (confirmPasswordError != null) {
                    Text(confirmPasswordError!!, color = Color.Red, modifier = Modifier.fillMaxWidth())
                }

                if (generalError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = generalError!!,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (successMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = successMessage!!,
                        color = PrimaryGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val rolVal = validarRol(rol)
                        val nombreVal = validarNombre(nombre)
                        val cedulaVal = validarCedula(cedula)
                        val correoVal = validarCorreo(correo)
                        val telefonoVal = validarTelefono(telefono)
                        val passwordVal = validarPassword(password)
                        val confirmVal = validarConfirmPassword(password, confirmPassword)

                        rolError = rolVal
                        nombreError = nombreVal
                        cedulaError = cedulaVal
                        correoError = correoVal
                        telefonoError = telefonoVal
                        passwordError = passwordVal
                        confirmPasswordError = confirmVal
                        generalError = null
                        successMessage = null

                        if (
                            rolVal != null ||
                            nombreVal != null ||
                            cedulaVal != null ||
                            correoVal != null ||
                            telefonoVal != null ||
                            passwordVal != null ||
                            confirmVal != null
                        ) return@Button

                        scope.launch {
                            loading = true
                            try {
                                val response = apiService.register(
                                    RegisterRequest(
                                        rol = rol,
                                        nombre = nombre.trim(),
                                        cedula = cedula.trim(),
                                        correo = correo.trim(),
                                        telefono = telefono.trim(),
                                        password = password
                                    )
                                )

                                if (response.success) {
                                    successMessage = response.message.ifBlank { "Cuenta creada" }
                                    delay(1200)
                                    onBackToLogin()
                                } else {
                                    generalError = response.message
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
                            contentDescription = "Registrar"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onBackToLogin) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        }
    }
}