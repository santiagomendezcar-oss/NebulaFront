package com.example.nebulagourmet.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nebulagourmet.data.model.*
import com.example.nebulagourmet.data.repository.AuthRepository
import com.example.nebulagourmet.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val data: Any) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
}

class AuthViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AuthRepository = AuthRepository(RetrofitClient.apiService)
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("nebula_auth_prefs", Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    var usuarioId by mutableStateOf<Long?>(null)
        private set
    var sesionId by mutableStateOf<String?>(null)
        private set
    var userRole by mutableStateOf<String?>(null)
        private set
    var userName by mutableStateOf<String?>(null)
        private set

    init {
        // Restaurar sesión guardada
        usuarioId = if (prefs.contains("usuario_id")) prefs.getLong("usuario_id", -1L).takeIf { it != -1L } else null
        sesionId = prefs.getString("sesion_id", null)
        userRole = prefs.getString("user_role", null)
        userName = prefs.getString("user_name", null)

        if (usuarioId != null || sesionId != null) {
            // Podríamos validar el token aquí si existiera
            _authState.value = AuthState.Success("Session Restored")
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.login(LoginDTO(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    
                    usuarioId = body.usuarioId
                    sesionId = null
                    userRole = body.rol
                    userName = body.nombre

                    // Guardar en SharedPreferences
                    prefs.edit().apply {
                        putLong("usuario_id", body.usuarioId)
                        putString("user_role", body.rol)
                        putString("user_name", body.nombre)
                        remove("sesion_id")
                        apply()
                    }

                    _authState.value = AuthState.Success(body)
                } else {
                    // ... (resto del código de error igual)
                    val errorMsg = try {
                        val errorJson = response.errorBody()?.string()
                        if (errorJson != null) {
                            val jsonObject = org.json.JSONObject(errorJson)
                            val msg = jsonObject.optString("mensaje", jsonObject.optString("error", ""))
                            if (msg.isNotBlank()) msg else "Credenciales incorrectas"
                        } else {
                            "Error de conexión con el servidor"
                        }
                    } catch (e: Exception) {
                        "Email o contraseña incorrectos"
                    }
                    _authState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun registrar(nombre: String, email: String, password: String, telefono: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.registrar(RegistroDTO(nombre, email, password, telefono))
                if (response.isSuccessful && response.body() != null) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    val errorMsg = try {
                        val errorJson = response.errorBody()?.string()
                        if (errorJson != null) {
                            val jsonObject = org.json.JSONObject(errorJson)
                            val msg = jsonObject.optString("mensaje", jsonObject.optString("error", ""))
                            if (msg.isNotBlank()) msg else "Error en el registro"
                        } else {
                            "Error de conexión con el servidor"
                        }
                    } catch (e: Exception) {
                        "Error al registrar cuenta"
                    }
                    _authState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loginInvitado() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.iniciarInvitado()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sesionId = body.sesionId
                    usuarioId = null
                    userRole = "INVITADO"
                    userName = "Invitado"

                    // Guardar en SharedPreferences
                    prefs.edit().apply {
                        putString("sesion_id", body.sesionId)
                        putString("user_role", "INVITADO")
                        putString("user_name", "Invitado")
                        remove("usuario_id")
                        apply()
                    }

                    _authState.value = AuthState.Success(body)
                } else {
                    // ... (resto del código de error)
                    val errorBody = response.errorBody()?.string() ?: ""
                    val mensaje = if (errorBody.contains("500") || errorBody.contains("Internal Server Error")) {
                        "ERROR DE BASE DE DATOS: El backend falló al crear la sesión de invitado."
                    } else {
                        "Error al iniciar como invitado"
                    }
                    _authState.value = AuthState.Error(mensaje)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        usuarioId = null
        sesionId = null
        userRole = null
        userName = null
        prefs.edit().clear().apply()
        _authState.value = AuthState.LoggedOut
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

