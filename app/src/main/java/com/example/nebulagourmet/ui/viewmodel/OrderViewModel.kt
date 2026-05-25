package com.example.nebulagourmet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nebulagourmet.data.model.*
import com.example.nebulagourmet.data.repository.OrderRepository
import com.example.nebulagourmet.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    data class Success(val pedido: Pedido) : OrderState()
    data class ListSuccess(val pedidos: List<Pedido>) : OrderState()
    data class Error(val message: String) : OrderState()
}

class OrderViewModel @JvmOverloads constructor(
    private val repository: OrderRepository = OrderRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState

    private val _domiciliarios = MutableStateFlow<List<Domiciliario>>(emptyList())
    val domiciliarios: StateFlow<List<Domiciliario>> = _domiciliarios

    fun fetchDomiciliarios() {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            try {
                val response = repository.getAllDomiciliarios()
                if (response.isSuccessful) {
                    _domiciliarios.value = response.body() ?: emptyList()
                    _orderState.value = OrderState.Idle
                } else {
                    _orderState.value = OrderState.Error("Error al obtener repartidores: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error fetching delivery staff", e)
                _orderState.value = OrderState.Error("Fallo de conexión: ${e.message}")
            }
        }
    }

    fun createDomiciliario(domiciliario: Domiciliario) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            try {
                val response = repository.createDomiciliario(domiciliario)
                if (response.isSuccessful) {
                    fetchDomiciliarios()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Sin cuerpo de error"
                    Log.e("OrderViewModel", "Error al crear domiciliario: Code ${response.code()}, Body: $errorBody")
                    _orderState.value = OrderState.Error("No se pudo guardar el repartidor: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error creating delivery staff", e)
                _orderState.value = OrderState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun asignarDomiciliario(domicilioId: Long, domiciliarioId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.asignarDomiciliario(domicilioId, domiciliarioId)
                if (response.isSuccessful) {
                    fetchAllPedidosAdmin() // Refresh to show assignment
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error assigning delivery", e)
            }
        }
    }

    fun crearPedidoConDomicilioParaUsuario(usuarioId: Long, request: PedidoDomicilioDTO) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            try {
                val response = repository.crearPedidoConDomicilioParaUsuario(usuarioId, request)
                handleOrderResponse(response)
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Excepción de red", e)
                _orderState.value = OrderState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun crearPedidoParaUsuario(usuarioId: Long, request: PedidoDTO) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            try {
                val response = repository.crearPedidoParaUsuario(usuarioId, request)
                handleOrderResponse(response)
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Excepción de red", e)
                _orderState.value = OrderState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun crearPedidoConDomicilioParaInvitado(sesionId: String, request: PedidoDomicilioDTO) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            try {
                val response = repository.crearPedidoConDomicilioParaInvitado(sesionId, request)
                handleOrderResponse(response)
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Excepción de red invitado", e)
                _orderState.value = OrderState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun crearPedidoParaInvitado(sesionId: String, request: PedidoDTO) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            try {
                val response = repository.crearPedidoParaInvitado(sesionId, request)
                handleOrderResponse(response)
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Excepción de red invitado", e)
                _orderState.value = OrderState.Error("Error de red: ${e.message}")
            }
        }
    }

    private fun handleOrderResponse(response: retrofit2.Response<Pedido>) {
        if (response.isSuccessful && response.body() != null) {
            _orderState.value = OrderState.Success(response.body()!!)
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            Log.e("OrderViewModel", "Error en pedido: $errorBody")
            val errorMessage = try {
                val json = org.json.JSONObject(errorBody)
                val msg = json.optString("mensaje", json.optString("error", ""))
                when {
                    msg.contains("duplicate key", ignoreCase = true) -> "ERROR: El pedido ya existe en el servidor."
                    msg.isNotBlank() -> msg
                    else -> "Error del servidor (${response.code()})"
                }
            } catch (e: Exception) {
                "Error interno del servidor (${response.code()})"
            }
            _orderState.value = OrderState.Error(errorMessage)
        }
    }

    fun resetOrderState() {
        _orderState.value = OrderState.Idle
    }

    fun fetchDetallesPedido(pedidoId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.getPedidoById(pedidoId)
                if (response.isSuccessful && response.body() != null) {
                    val pedidoActualizado = response.body()!!
                    
                    val currentState = _orderState.value
                    if (currentState is OrderState.ListSuccess) {
                        val nuevaLista = currentState.pedidos.map {
                            if (it.id == pedidoId) {
                                // IMPORTANTE: Solo actualizamos los items y mantenemos el resto del objeto
                                // para evitar que una carga de detalles lenta sobrescriba un cambio de estado optimista.
                                it.copy(items = pedidoActualizado.items ?: emptyList())
                            } else it
                        }
                        _orderState.value = OrderState.ListSuccess(nuevaLista)
                    }
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error cargando detalles: ${e.message}")
            }
        }
    }

    fun fetchPedidos(usuarioId: Long?, sesionId: String?) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            try {
                val response = when {
                    usuarioId != null -> repository.getPedidosByUsuario(usuarioId)
                    sesionId != null -> repository.getPedidosByInvitado(sesionId)
                    else -> null
                }
                if (response != null && response.isSuccessful) {
                    _orderState.value = OrderState.ListSuccess(response.body() ?: emptyList())
                } else {
                    val errorMsg = response?.errorBody()?.string() ?: "Error al obtener historial"
                    _orderState.value = OrderState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Error fetching orders", e)
                val detailedError = when (e) {
                    is com.google.gson.JsonSyntaxException -> "Error de formato JSON: ${e.message}"
                    is java.net.ConnectException -> "No se pudo conectar al servidor"
                    else -> "Error: ${e.localizedMessage}"
                }
                _orderState.value = OrderState.Error(detailedError)
            }
        }
    }

    fun fetchAllPedidosAdmin() {
        viewModelScope.launch {
            // Solo mostramos loading la primera vez o si no hay datos
            if (_orderState.value !is OrderState.ListSuccess) {
                _orderState.value = OrderState.Loading
            }
            try {
                val response = repository.getAllPedidos()
                if (response.isSuccessful) {
                    val pedidos = response.body() ?: emptyList()
                    // Ordenamos aquí en el ViewModel para que la UI no tenga que hacerlo cada vez
                    val pedidosOrdenados = pedidos.sortedByDescending { it.id }
                    _orderState.value = OrderState.ListSuccess(pedidosOrdenados)
                } else {
                    if (_orderState.value !is OrderState.ListSuccess) {
                        _orderState.value = OrderState.Error("Error al obtener pedidos")
                    }
                }
            } catch (e: Exception) {
                if (_orderState.value !is OrderState.ListSuccess) {
                    _orderState.value = OrderState.Error(e.message ?: "Error de red")
                }
            }
        }
    }

    fun actualizarEstadoPedido(id: Long, estado: EstadoPedido) {
        viewModelScope.launch {
            // --- ACTUALIZACIÓN OPTIMISTA ---
            // Cambiamos el estado en la memoria del app de inmediato para que el usuario no espere
            val currentState = _orderState.value
            if (currentState is OrderState.ListSuccess) {
                val listaOptimizada = currentState.pedidos.map {
                    if (it.id == id) it.copy(estado = estado) else it
                }
                _orderState.value = OrderState.ListSuccess(listaOptimizada)
            }

            try {
                val response = repository.actualizarEstadoPedido(id, estado)
                if (!response.isSuccessful) {
                    // Si el servidor dice que no se pudo, refrescamos para volver al estado real
                    Log.e("OrderViewModel", "Error en servidor al actualizar, refrescando...")
                    fetchAllPedidosAdmin()
                }
                // Si tuvo éxito, no hacemos nada extra porque la UI ya se actualizó arriba
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Fallo de red, revirtiendo estado...")
                fetchAllPedidosAdmin()
            }
        }
    }
}

