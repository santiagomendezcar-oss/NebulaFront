package com.example.nebulagourmet.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulagourmet.ui.viewmodel.AuthState
import com.example.nebulagourmet.ui.viewmodel.AuthViewModel
import com.example.nebulagourmet.ui.theme.*

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BoutiqueBackground)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        Text(
            text = "N E B U L A",
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 8.sp,
            color = BoutiquePrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "CREAR CUENTA",
            fontSize = 32.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Thin,
            color = BoutiquePrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(1.dp)
                .background(BoutiquePrimary.copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(48.dp))

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BoutiquePrimary,
            unfocusedBorderColor = BoutiquePrimary.copy(alpha = 0.3f),
            focusedLabelColor = BoutiquePrimary,
            unfocusedLabelColor = BoutiquePrimary.copy(alpha = 0.5f),
            focusedTextColor = BoutiquePrimary,
            unfocusedTextColor = BoutiquePrimary,
            cursorColor = BoutiquePrimary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )

        val fieldModifier = Modifier.fillMaxWidth()

        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                localError = null
            },
            label = { Text("NOMBRE COMPLETO", fontSize = 9.sp, letterSpacing = 2.sp) },
            modifier = fieldModifier,
            shape = RoundedCornerShape(0.dp),
            colors = textFieldColors,
            singleLine = true,
            enabled = authState !is AuthState.Loading
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                localError = null
            },
            label = { Text("CORREO ELECTRÓNICO", fontSize = 9.sp, letterSpacing = 2.sp) },
            modifier = fieldModifier,
            shape = RoundedCornerShape(0.dp),
            colors = textFieldColors,
            singleLine = true,
            enabled = authState !is AuthState.Loading
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { 
                phone = it
                localError = null
            },
            label = { Text("TELÉFONO", fontSize = 9.sp, letterSpacing = 2.sp) },
            modifier = fieldModifier,
            shape = RoundedCornerShape(0.dp),
            colors = textFieldColors,
            singleLine = true,
            enabled = authState !is AuthState.Loading
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                localError = null
            },
            label = { Text("CONTRASEÑA", fontSize = 9.sp, letterSpacing = 2.sp) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = BoutiquePrimary.copy(alpha = 0.4f))
                }
            },
            modifier = fieldModifier,
            shape = RoundedCornerShape(0.dp),
            colors = textFieldColors,
            singleLine = true,
            enabled = authState !is AuthState.Loading
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                localError = null
            },
            label = { Text("VERIFICAR CONTRASEÑA", fontSize = 9.sp, letterSpacing = 2.sp) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = BoutiquePrimary.copy(alpha = 0.4f))
                }
            },
            modifier = fieldModifier,
            shape = RoundedCornerShape(0.dp),
            colors = textFieldColors,
            singleLine = true,
            enabled = authState !is AuthState.Loading
        )

        if (localError != null || authState is AuthState.Error) {
            val errorText = localError ?: (authState as AuthState.Error).message.uppercase()
            Text(
                text = errorText,
                color = Color(0xFF8B0000),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 24.dp),
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Serif
            )
        }

        Spacer(modifier = Modifier.height(56.dp))

        if (authState is AuthState.Loading) {
            CircularProgressIndicator(color = BoutiquePrimary, strokeWidth = 1.dp, modifier = Modifier.size(30.dp))
        } else {
            Button(
                onClick = { 
                    val emailTrimmed = email.trim()
                    val nameTrimmed = name.trim()
                    val phoneTrimmed = phone.trim()
                    
                    when {
                        nameTrimmed.isEmpty() -> localError = "EL NOMBRE ES OBLIGATORIO"
                        emailTrimmed.isEmpty() -> localError = "EL EMAIL ES OBLIGATORIO"
                        !Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches() -> 
                            localError = "INGRESE UN EMAIL VÁLIDO"
                        phoneTrimmed.isEmpty() -> localError = "EL TELÉFONO ES OBLIGATORIO"
                        password.length < 6 -> localError = "LA CONTRASEÑA ES MUY CORTA"
                        password != confirmPassword -> localError = "LAS CONTRASEÑAS NO COINCIDEN"
                        else -> viewModel.registrar(nameTrimmed, emailTrimmed, password, phoneTrimmed)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BoutiquePrimary,
                    contentColor = BoutiqueBackground
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    "REGISTRARSE", 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Light, 
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "¿YA TIENES CUENTA? INICIA SESIÓN", 
                    color = BoutiquePrimary.copy(alpha = 0.6f), 
                    fontSize = 10.sp, 
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Serif
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}
