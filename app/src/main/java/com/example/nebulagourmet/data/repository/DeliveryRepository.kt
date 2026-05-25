package com.example.nebulagourmet.data.repository

import com.example.nebulagourmet.data.model.*
import com.example.nebulagourmet.data.network.ApiService
import retrofit2.Response

class DeliveryRepository(private val apiService: ApiService) {

    // --- Domicilios ---
    suspend fun getAllDomicilios(): Response<List<Domicilio>> = apiService.getAllDomicilios()

    suspend fun getDomiciliosPendientes(): Response<List<Domicilio>> = apiService.getDomiciliosPendientes()

    suspend fun getDomiciliosPorEstado(estado: EstadoDomicilio): Response<List<Domicilio>> = 
        apiService.getDomiciliosPorEstado(estado)

    suspend fun getDomicilioById(id: Long): Response<Domicilio> = apiService.getDomicilioById(id)

    suspend fun getDomicilioByPedidoId(pedidoId: Long): Response<Domicilio> = 
        apiService.getDomicilioByPedidoId(pedidoId)

    suspend fun actualizarEstadoDomicilio(id: Long, estado: EstadoDomicilio): Response<Domicilio> = 
        apiService.actualizarEstadoDomicilio(id, estado)

    suspend fun asignarDomiciliario(domicilioId: Long, domiciliarioId: Long): Response<Domicilio> = 
        apiService.asignarDomiciliario(domicilioId, domiciliarioId)

    suspend fun asignarDomiciliarioAutomatico(domicilioId: Long): Response<Domicilio> = 
        apiService.asignarDomiciliarioAutomatico(domicilioId)

    // --- Domiciliarios ---
    suspend fun getAllDomiciliarios(): Response<List<Domiciliario>> = apiService.getAllDomiciliarios()

    suspend fun getDomiciliarioById(id: Long): Response<Domiciliario> = apiService.getDomiciliarioById(id)

    suspend fun getDomiciliarioMenosOcupado(): Response<Domiciliario> = apiService.getDomiciliarioMenosOcupado()

    suspend fun createDomiciliario(domiciliario: Domiciliario): Response<Domiciliario> = 
        apiService.createDomiciliario(domiciliario)

    suspend fun updateCalificacion(id: Long, calificacion: Double): Response<Domiciliario> = 
        apiService.updateCalificacion(id, calificacion)
}
