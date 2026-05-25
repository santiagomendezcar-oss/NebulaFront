package com.example.nebulagourmet.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nebulagourmet.data.model.Producto
import com.example.nebulagourmet.data.model.ProductoPersonalizadoDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CartItem(
    val producto: Producto,
    val cantidad: Int,
    val adicionalesIds: List<Long> = emptyList(),
    val eliminadosIds: List<Long> = emptyList(),
    val precioUnitario: Double
)

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("nebula_cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    init {
        cargarCarrito()
    }

    val total: Double
        get() = _items.value.sumOf { it.precioUnitario * it.cantidad }

    private fun guardarCarrito() {
        viewModelScope.launch {
            val json = gson.toJson(_items.value)
            sharedPreferences.edit().putString("cart_items", json).apply()
        }
    }

    private fun cargarCarrito() {
        val json = sharedPreferences.getString("cart_items", null)
        if (json != null) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            val itemsGuardados: List<CartItem> = gson.fromJson(json, type)
            _items.value = itemsGuardados
        }
    }

    fun agregarAlCarrito(
        producto: Producto,
        cantidad: Int,
        adicionalesIds: List<Long>,
        eliminadosIds: List<Long>,
        precioUnitario: Double
    ) {
        val nuevoItem = CartItem(producto, cantidad, adicionalesIds, eliminadosIds, precioUnitario)
        _items.value = _items.value + nuevoItem
        guardarCarrito()
    }

    fun eliminarDelCarrito(item: CartItem) {
        _items.value = _items.value - item
        guardarCarrito()
    }

    fun limpiarCarrito() {
        _items.value = emptyList()
        guardarCarrito()
    }

    fun toProductoPersonalizadoDTOs(): List<ProductoPersonalizadoDTO> {
        return _items.value.map {
            ProductoPersonalizadoDTO(
                productoId = it.producto.id!!,
                cantidad = it.cantidad,
                ingredientesAdicionalesIds = it.adicionalesIds,
                ingredientesEliminadosIds = it.eliminadosIds
            )
        }
    }
}
