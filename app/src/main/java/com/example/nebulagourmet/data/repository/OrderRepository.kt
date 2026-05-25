package com.example.nebulagourmet.data.repository

import com.example.nebulagourmet.data.model.*
import com.example.nebulagourmet.data.network.ApiService
import retrofit2.Response

class OrderRepository(private val apiService: ApiService) {

    suspend fun getAllPedidos(): Response<List<Pedido>> {
        return apiService.getAllPedidos()
    }

    suspend fun getPedidoById(id: Long): Response<Pedido> {
        return apiService.getPedidoById(id)
    }

    suspend fun crearPedido(request: PedidoDTO): Response<Pedido> {
        return apiService.crearPedido(request)
    }

    suspend fun crearPedidoConDomicilio(request: PedidoDomicilioDTO): Response<Pedido> {
        return apiService.crearPedidoConDomicilio(request)
    }

    suspend fun actualizarEstadoPedido(id: Long, estado: EstadoPedido): Response<Pedido> {
        return apiService.actualizarEstadoPedido(id, estado)
    }

    suspend fun crearPedidoParaUsuario(usuarioId: Long, request: PedidoDTO): Response<Pedido> {
        return apiService.crearPedidoParaUsuario(usuarioId, request)
    }

    suspend fun crearPedidoParaInvitado(sesionId: String, request: PedidoDTO): Response<Pedido> {
        return apiService.crearPedidoParaInvitado(sesionId, request)
    }

    suspend fun crearPedidoConDomicilioParaUsuario(usuarioId: Long, request: PedidoDomicilioDTO): Response<Pedido> {
        return apiService.crearPedidoConDomicilioParaUsuario(usuarioId, request)
    }

    suspend fun crearPedidoConDomicilioParaInvitado(sesionId: String, request: PedidoDomicilioDTO): Response<Pedido> {
        return apiService.crearPedidoConDomicilioParaInvitado(sesionId, request)
    }

    suspend fun getPedidosByUsuario(usuarioId: Long): Response<List<Pedido>> {
        return apiService.getPedidosByUsuario(usuarioId)
    }

    suspend fun getPedidosByInvitado(sesionId: String): Response<List<Pedido>> {
        return apiService.getPedidosByInvitado(sesionId)
    }

    // --- Domicilios ---
    suspend fun getAllDomiciliarios(): Response<List<Domiciliario>> {
        return apiService.getAllDomiciliarios()
    }

    suspend fun createDomiciliario(domiciliario: Domiciliario): Response<Domiciliario> {
        return apiService.createDomiciliario(domiciliario)
    }

    suspend fun asignarDomiciliario(domicilioId: Long, domiciliarioId: Long): Response<Domicilio> {
        return apiService.asignarDomiciliario(domicilioId, domiciliarioId)
    }

    suspend fun asignarDomiciliarioAutomatico(domicilioId: Long): Response<Domicilio> {
        return apiService.asignarDomiciliarioAutomatico(domicilioId)
    }
}
