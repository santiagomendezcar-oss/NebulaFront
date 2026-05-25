package com.example.nebulagourmet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nebulagourmet.data.model.Ingrediente
import com.example.nebulagourmet.data.model.Producto
import com.example.nebulagourmet.data.model.ProductoDTO
import com.example.nebulagourmet.data.repository.ProductRepository
import com.example.nebulagourmet.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductState {
    object Idle : ProductState()
    object Loading : ProductState()
    data class Success(val productos: List<Producto>) : ProductState()
    data class SingleSuccess(val producto: Producto) : ProductState()
    data class Error(val message: String) : ProductState()
}

sealed class AdminActionState {
    object Idle : AdminActionState()
    object Loading : AdminActionState()
    object Success : AdminActionState()
    data class Error(val message: String) : AdminActionState()
}

class ProductViewModel @JvmOverloads constructor(
    private val repository: ProductRepository = ProductRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _productState = MutableStateFlow<ProductState>(ProductState.Idle)
    val productState: StateFlow<ProductState> = _productState.asStateFlow()

    private val _adminActionState = MutableStateFlow<AdminActionState>(AdminActionState.Idle)
    val adminActionState: StateFlow<AdminActionState> = _adminActionState.asStateFlow()

    private val _ingredientesExtra = MutableStateFlow<List<Ingrediente>>(emptyList())
    val ingredientesExtra: StateFlow<List<Ingrediente>> = _ingredientesExtra

    private val _allIngredientes = MutableStateFlow<List<Ingrediente>>(emptyList())
    val allIngredientes: StateFlow<List<Ingrediente>> = _allIngredientes.asStateFlow()

    fun fetchProductos(admin: Boolean = false) {
        viewModelScope.launch {
            _productState.value = ProductState.Loading
            try {
                val response = if (admin) repository.getAllProductos() else repository.getProductosDisponibles()
                if (response.isSuccessful) {
                    _productState.value = ProductState.Success(response.body() ?: emptyList())
                } else {
                    _productState.value = ProductState.Error("Error al cargar productos: ${response.code()}")
                }
            } catch (e: Exception) {
                _productState.value = ProductState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun fetchAllIngredientes() {
        viewModelScope.launch {
            try {
                val response = repository.getAllIngredientes()
                if (response.isSuccessful) {
                    _allIngredientes.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createIngrediente(ingrediente: Ingrediente) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Loading
            try {
                val response = repository.createIngrediente(ingrediente)
                if (response.isSuccessful) {
                    _adminActionState.value = AdminActionState.Success
                    fetchAllIngredientes()
                } else {
                    _adminActionState.value = AdminActionState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _adminActionState.value = AdminActionState.Error(e.message ?: "Error")
            }
        }
    }

    fun updateIngrediente(id: Long, ingrediente: Ingrediente) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Loading
            try {
                val response = repository.updateIngrediente(id, ingrediente)
                if (response.isSuccessful) {
                    _adminActionState.value = AdminActionState.Success
                    fetchAllIngredientes()
                    fetchProductos(true) // Refresh products too as they might use this ingredient
                } else {
                    _adminActionState.value = AdminActionState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _adminActionState.value = AdminActionState.Error(e.message ?: "Error")
            }
        }
    }

    fun deleteIngrediente(id: Long) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Loading
            try {
                val response = repository.deleteIngrediente(id)
                if (response.isSuccessful) {
                    _adminActionState.value = AdminActionState.Success
                    fetchAllIngredientes()
                } else {
                    _adminActionState.value = AdminActionState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _adminActionState.value = AdminActionState.Error(e.message ?: "Error")
            }
        }
    }

    fun createProducto(productoDTO: ProductoDTO) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Loading
            try {
                val response = repository.createProducto(productoDTO)
                if (response.isSuccessful) {
                    _adminActionState.value = AdminActionState.Success
                    fetchProductos(true)
                } else {
                    _adminActionState.value = AdminActionState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _adminActionState.value = AdminActionState.Error(e.message ?: "Error")
            }
        }
    }

    fun updateProducto(id: Long, producto: Producto) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Loading
            try {
                val response = repository.updateProducto(id, producto)
                if (response.isSuccessful) {
                    _adminActionState.value = AdminActionState.Success
                    fetchProductos(true)
                } else {
                    _adminActionState.value = AdminActionState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _adminActionState.value = AdminActionState.Error(e.message ?: "Error")
            }
        }
    }

    fun deleteProducto(id: Long) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Loading
            try {
                val response = repository.deleteProducto(id)
                if (response.isSuccessful) {
                    _adminActionState.value = AdminActionState.Success
                    fetchProductos(true)
                } else {
                    _adminActionState.value = AdminActionState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _adminActionState.value = AdminActionState.Error(e.message ?: "Error")
            }
        }
    }

    fun resetAdminActionState() {
        _adminActionState.value = AdminActionState.Idle
    }

    fun fetchProductById(id: Long) {
        viewModelScope.launch {
            _productState.value = ProductState.Loading
            try {
                val response = repository.getProductoById(id)
                if (response.isSuccessful && response.body() != null) {
                    _productState.value = ProductState.SingleSuccess(response.body()!!)
                    fetchIngredientesExtras()
                } else {
                    _productState.value = ProductState.Error("Error al cargar producto")
                }
            } catch (e: Exception) {
                _productState.value = ProductState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun fetchIngredientesExtras() {
        viewModelScope.launch {
            try {
                val response = repository.getIngredientesDisponibles()
                if (response.isSuccessful) {
                    _ingredientesExtra.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }
}
