package com.example.nebulagourmet.data.network

import com.example.nebulagourmet.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Autenticación (AutenticacionController) ---
    @POST("api/auth/registro")
    suspend fun registrar(@Body request: RegistroDTO): Response<Map<String, Any>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginDTO): Response<LoginResponseDTO>

    @POST("api/auth/invitado")
    suspend fun iniciarInvitado(): Response<InvitadoResponse>

    @POST("api/auth/invitado/validar")
    suspend fun validarInvitado(@Body request: ValidarInvitadoRequest): Response<ValidarInvitadoResponse>

    @GET("api/auth/verificar-email")
    suspend fun verificarEmail(@Query("email") email: String): Response<EmailExisteResponse>

    // --- Productos (ProductoController) ---
    @GET("api/productos")
    suspend fun getAllProductos(): Response<List<Producto>>

    @GET("api/productos/disponibles")
    suspend fun getProductosDisponibles(): Response<List<Producto>>

    @GET("api/productos/{id}")
    suspend fun getProductoById(@Path("id") id: Long): Response<Producto>

    @POST("api/productos")
    suspend fun createProducto(@Body productoDTO: ProductoDTO): Response<Producto>

    @PUT("api/productos/{id}")
    suspend fun updateProducto(@Path("id") id: Long, @Body producto: Producto): Response<Producto>

    @DELETE("api/productos/{id}")
    suspend fun deleteProducto(@Path("id") id: Long): Response<Void>

    // --- Ingredientes (IngredienteController) ---
    @GET("api/ingredientes")
    suspend fun getAllIngredientes(): Response<List<Ingrediente>>

    @GET("api/ingredientes/disponibles")
    suspend fun getIngredientesDisponibles(): Response<List<Ingrediente>>

    @GET("api/ingredientes/{id}")
    suspend fun getIngredienteById(@Path("id") id: Long): Response<Ingrediente>

    @POST("api/ingredientes")
    suspend fun createIngrediente(@Body ingrediente: Ingrediente): Response<Ingrediente>

    @PUT("api/ingredientes/{id}")
    suspend fun updateIngrediente(@Path("id") id: Long, @Body ingrediente: Ingrediente): Response<Ingrediente>

    @DELETE("api/ingredientes/{id}")
    suspend fun deleteIngrediente(@Path("id") id: Long): Response<Void>

    // --- Pedidos (PedidoController) ---
    @GET("api/pedidos/usuario/{usuarioId}")
    suspend fun getPedidosByUsuario(@Path("usuarioId") usuarioId: Long): Response<List<Pedido>>

    @GET("api/pedidos/invitado/{sesionId}")
    suspend fun getPedidosByInvitado(@Path("sesionId") sesionId: String): Response<List<Pedido>>

    @GET("api/pedidos")
    suspend fun getAllPedidos(): Response<List<Pedido>>

    @GET("api/pedidos/{id}")
    suspend fun getPedidoById(@Path("id") id: Long): Response<Pedido>

    @POST("api/pedidos")
    suspend fun crearPedido(@Body request: PedidoDTO): Response<Pedido>

    @POST("api/pedidos/con-domicilio")
    suspend fun crearPedidoConDomicilio(@Body request: PedidoDomicilioDTO): Response<Pedido>

    @PATCH("api/pedidos/{id}/estado")
    suspend fun actualizarEstadoPedido(
        @Path("id") id: Long,
        @Query("estado") estado: EstadoPedido
    ): Response<Pedido>

    @POST("api/pedidos/usuario/{usuarioId}")
    suspend fun crearPedidoParaUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Body request: PedidoDTO
    ): Response<Pedido>

    @POST("api/pedidos/invitado/{sesionId}")
    suspend fun crearPedidoParaInvitado(
        @Path("sesionId") sesionId: String,
        @Body request: PedidoDTO
    ): Response<Pedido>

    @POST("api/pedidos/usuario/{usuarioId}/con-domicilio")
    suspend fun crearPedidoConDomicilioParaUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Body request: PedidoDomicilioDTO
    ): Response<Pedido>

    @POST("api/pedidos/invitado/{sesionId}/con-domicilio")
    suspend fun crearPedidoConDomicilioParaInvitado(
        @Path("sesionId") sesionId: String,
        @Body request: PedidoDomicilioDTO
    ): Response<Pedido>

    // --- Domicilios (DomicilioController) ---
    @GET("api/domicilios")
    suspend fun getAllDomicilios(): Response<List<Domicilio>>

    @GET("api/domicilios/pendientes")
    suspend fun getDomiciliosPendientes(): Response<List<Domicilio>>

    @GET("api/domicilios/estado/{estado}")
    suspend fun getDomiciliosPorEstado(@Path("estado") estado: EstadoDomicilio): Response<List<Domicilio>>

    @GET("api/domicilios/{id}")
    suspend fun getDomicilioById(@Path("id") id: Long): Response<Domicilio>

    @GET("api/domicilios/pedido/{pedidoId}")
    suspend fun getDomicilioByPedidoId(@Path("pedidoId") pedidoId: Long): Response<Domicilio>

    @PATCH("api/domicilios/{id}/estado")
    suspend fun actualizarEstadoDomicilio(
        @Path("id") id: Long,
        @Query("estado") estado: EstadoDomicilio
    ): Response<Domicilio>

    @POST("api/domicilios/{domicilioId}/asignar/{domiciliarioId}")
    suspend fun asignarDomiciliario(
        @Path("domicilioId") domicilioId: Long,
        @Path("domiciliarioId") domiciliarioId: Long
    ): Response<Domicilio>

    @POST("api/domicilios/{domicilioId}/asignar-automatico")
    suspend fun asignarDomiciliarioAutomatico(@Path("domicilioId") domicilioId: Long): Response<Domicilio>

    // --- Domiciliarios (DomiciliarioController) ---
    @GET("api/domiciliarios")
    suspend fun getAllDomiciliarios(): Response<List<Domiciliario>>

    @GET("api/domiciliarios/{id}")
    suspend fun getDomiciliarioById(@Path("id") id: Long): Response<Domiciliario>

    @GET("api/domiciliarios/menos-ocupado")
    suspend fun getDomiciliarioMenosOcupado(): Response<Domiciliario>

    @POST("api/domiciliarios")
    suspend fun createDomiciliario(@Body domiciliario: Domiciliario): Response<Domiciliario>

    @PUT("api/domiciliarios/{id}/calificacion")
    suspend fun updateCalificacion(
        @Path("id") id: Long,
        @Query("calificacion") calificacion: Double
    ): Response<Domiciliario>

    // --- Notificaciones (NotificacionController) ---
    @GET("api/notificaciones/usuario/{usuarioId}")
    suspend fun getNotificacionesByUsuario(@Path("usuarioId") usuarioId: Long): Response<List<NotificacionDTO>>

    @GET("api/notificaciones/invitado/{sesionId}")
    suspend fun getNotificacionesByInvitado(@Path("sesionId") sesionId: String): Response<List<NotificacionDTO>>

    @PATCH("api/notificaciones/{id}/leer")
    suspend fun marcarComoLeida(@Path("id") id: Long): Response<Map<String, String>>

    @PATCH("api/notificaciones/usuario/{usuarioId}/leer-todas")
    suspend fun marcarTodasComoLeidas(@Path("usuarioId") usuarioId: Long): Response<Map<String, String>>

    @GET("api/notificaciones/usuario/{usuarioId}/no-leidas")
    suspend fun contarNoLeidas(@Path("usuarioId") usuarioId: Long): Response<Map<String, Long>>
}
