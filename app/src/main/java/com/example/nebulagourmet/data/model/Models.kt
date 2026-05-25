package com.example.nebulagourmet.data.model

import com.google.gson.annotations.SerializedName

// --- Auth Models ---
data class RegistroDTO(
    val nombre: String,
    val email: String,
    val password: String,  // Cambiado de contrasena a password
    val telefono: String? = null
)

data class LoginDTO(
    val email: String,
    val password: String   // Sincronizado también aquí para consistencia
)

data class LoginResponseDTO(
    val token: String,
    val usuarioId: Long,
    val email: String,
    val nombre: String,
    val rol: String,       // Cambiado de List<String> a String
    val mensaje: String    // Agregado
)

data class InvitadoResponse(
    val sesionId: String,
    val mensaje: String,
    val tipo: String
)

data class ValidarInvitadoRequest(
    val sesionId: String
)

data class ValidarInvitadoResponse(
    val valida: Boolean,
    val sesionId: String? = null
)

data class EmailExisteResponse(
    val existe: Boolean
)

// --- Product & Ingredient Models ---
data class Ingrediente(
    val id: Long? = null,
    val nombre: String,
    val precioExtra: Double,  // Cambiado de precio a precioExtra
    val categoria: String? = null, // Agregado
    val disponible: Boolean = true
)

data class Producto(
    val id: Long? = null,
    val nombre: String,
    val descripcion: String,
    val precioBase: Double,
    val categoria: String,
    val disponible: Boolean = true,
    val ingredientesBase: List<Ingrediente> = emptyList()
)

data class ProductoDTO(
    val nombre: String,
    val descripcion: String,
    val precioBase: Double,
    val categoria: String,
    val disponible: Boolean? = true,
    val ingredientesBaseIds: List<Long> = emptyList()
)

// --- Order Models ---
enum class EstadoPedido {
    @SerializedName("PENDIENTE", alternate = ["RECIBIDO", "NUEVO", "pendiente", "recibido"])
    PENDIENTE,
    @SerializedName("EN_PREPARACION", alternate = ["PREPARANDO", "COCINANDO", "EN_COCINA", "en_preparacion", "preparando"])
    EN_PREPARACION,
    @SerializedName("LISTO", alternate = ["POR_RECOGER", "PREPARADO", "listo", "preparado"])
    LISTO,
    @SerializedName("ENTREGADO", alternate = ["FINALIZADO", "COMPLETADO", "entregado", "completado"])
    ENTREGADO,
    @SerializedName("CANCELADO", alternate = ["RECHAZADO", "cancelado", "rechazado"])
    CANCELADO
}

// Estructura para los productos dentro de un pedido
data class ProductoPersonalizadoDTO(
    val productoId: Long,
    val cantidad: Int,
    val ingredientesAdicionalesIds: List<Long> = emptyList(),
    val ingredientesEliminadosIds: List<Long> = emptyList()
)

data class PedidoDTO(
    val clienteNombre: String,
    val metodoPago: String,
    val productos: List<ProductoPersonalizadoDTO>
)

data class DomicilioDTO(
    val direccion: String,
    val barrio: String? = null,
    val ciudad: String = "Ciudad",
    val codigoPostal: String? = null,
    val instruccionesEspeciales: String? = null,
    val telefonoContacto: String,
    val nombreContacto: String? = null
)

data class PedidoDomicilioDTO(
    val pedido: PedidoDTO,
    val domicilio: DomicilioDTO
)

data class UsuarioPedido(
    val id: Long? = null,
    val nombre: String? = null,
    val email: String? = null
)

data class Pedido(
    val id: Long? = null,
    @SerializedName(value = "fechaPedido", alternate = ["fecha", "fecha_pedido", "createdAt"])
    val fecha: String? = null,
    val total: Double? = 0.0,
    val estado: EstadoPedido? = EstadoPedido.PENDIENTE,
    
    // Cambiamos Long por el objeto que envía Spring
    @SerializedName(value = "usuario", alternate = ["cliente", "user"])
    val usuario: UsuarioPedido? = null,
    
    val sesionInvitadoId: String? = null,
    val esDomicilio: Boolean? = false,
    val metodoPago: String? = null,
    
    @SerializedName(value = "detalles", alternate = ["productos", "items", "pedidoItems", "detallesPedido", "detalles_pedido", "items_pedido", "pedido_items", "orderItems", "itemsOrden", "detallesOrden", "lineItems", "detalles_del_pedido", "pedidoDetalles", "productosPedido", "pedido_detalles", "order_items", "order_details", "details"])
    val items: List<PedidoItem>? = null,

    // Agregamos la relación con domicilio para ver el seguimiento
    @SerializedName(value = "domicilio", alternate = ["envio", "delivery"])
    val domicilio: Domicilio? = null
)

data class PedidoItem(
    val id: Long? = null,
    @SerializedName(value = "producto", alternate = ["articulo", "item", "producto_base", "product", "producto_id", "product_id"])
    val producto: Producto? = null,
    
    @SerializedName(value = "cantidad", alternate = ["quantity", "unidades", "num_items", "count", "amount", "qty"])
    val cantidad: Int = 1,
    
    @SerializedName(value = "precioUnitario", alternate = ["precio", "precioVenta", "subtotal", "precio_unitario", "precioBase", "valor_unitario", "monto", "unitPrice", "unit_price", "price"])
    val precioUnitario: Double? = 0.0,

    @SerializedName(value = "productoNombre", alternate = ["nombre", "nombreProducto", "producto_nombre", "nombre_producto", "articulo_nombre", "productName", "product_name", "item_name"])
    val productoNombre: String? = null
) {
    val nombreAMostrar: String 
        get() = (producto?.nombre ?: productoNombre ?: "PRODUCTO").uppercase()
}

// --- Delivery Models ---
enum class EstadoDomicilio {
    PENDIENTE,
    ASIGNADO,      // Agregado
    EN_CAMINO,
    RECOGIDO,      // Agregado
    EN_ENTREGA,    // Agregado
    ENTREGADO,
    CANCELADO,
    DEVUELTO       // Agregado
}

data class Domiciliario(
    val id: Long? = null,
    val nombre: String,
    val telefono: String,
    val calificacion: Double = 0.0,
    val activo: Boolean = true
)

data class Domicilio(
    val id: Long? = null,
    val pedidoId: Long,
    val domiciliarioId: Long? = null,
    val domiciliario: Domiciliario? = null, // Agregado para mostrar nombre
    val direccion: String,
    val estado: EstadoDomicilio,
    val fechaAsignacion: String? = null
)

// --- Notification Models ---
data class NotificacionDTO(
    val id: Long,
    val mensaje: String,
    val fecha: String,
    val leida: Boolean,
    val tipo: String
)
