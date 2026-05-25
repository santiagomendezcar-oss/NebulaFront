package com.example.nebulagourmet.data.repository

import com.example.nebulagourmet.data.model.NotificacionDTO
import com.example.nebulagourmet.data.network.ApiService
import retrofit2.Response

class NotificationRepository(private val apiService: ApiService) {

    suspend fun getNotificacionesByUsuario(usuarioId: Long): Response<List<NotificacionDTO>> =
        apiService.getNotificacionesByUsuario(usuarioId)

    suspend fun getNotificacionesByInvitado(sesionId: String): Response<List<NotificacionDTO>> =
        apiService.getNotificacionesByInvitado(sesionId)

    suspend fun marcarComoLeida(id: Long): Response<Map<String, String>> =
        apiService.marcarComoLeida(id)

    suspend fun marcarTodasComoLeidas(usuarioId: Long): Response<Map<String, String>> =
        apiService.marcarTodasComoLeidas(usuarioId)

    suspend fun contarNoLeidas(usuarioId: Long): Response<Map<String, Long>> =
        apiService.contarNoLeidas(usuarioId)
}
