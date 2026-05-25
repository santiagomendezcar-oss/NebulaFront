package com.example.nebulagourmet.data.repository

import com.example.nebulagourmet.data.model.*
import com.example.nebulagourmet.data.network.ApiService
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    suspend fun registrar(request: RegistroDTO): Response<Map<String, Any>> {
        return apiService.registrar(request)
    }

    suspend fun login(request: LoginDTO): Response<LoginResponseDTO> {
        return apiService.login(request)
    }

    suspend fun iniciarInvitado(): Response<InvitadoResponse> {
        return apiService.iniciarInvitado()
    }

    suspend fun validarInvitado(request: ValidarInvitadoRequest): Response<ValidarInvitadoResponse> {
        return apiService.validarInvitado(request)
    }

    suspend fun verificarEmail(email: String): Response<EmailExisteResponse> {
        return apiService.verificarEmail(email)
    }
}
