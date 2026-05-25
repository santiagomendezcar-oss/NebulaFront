package com.example.nebulagourmet.data.repository

import com.example.nebulagourmet.data.model.Ingrediente
import com.example.nebulagourmet.data.model.Producto
import com.example.nebulagourmet.data.network.ApiService
import retrofit2.Response

class ProductRepository(private val apiService: ApiService) {

    suspend fun getAllProductos(): Response<List<Producto>> {
        return apiService.getAllProductos()
    }

    suspend fun getProductosDisponibles(): Response<List<Producto>> {
        return apiService.getProductosDisponibles()
    }

    suspend fun getProductoById(id: Long): Response<Producto> {
        return apiService.getProductoById(id)
    }

    suspend fun getAllIngredientes(): Response<List<Ingrediente>> {
        return apiService.getAllIngredientes()
    }

    suspend fun getIngredientesDisponibles(): Response<List<Ingrediente>> {
        return apiService.getIngredientesDisponibles()
    }

    suspend fun createProducto(productoDTO: com.example.nebulagourmet.data.model.ProductoDTO): Response<Producto> {
        return apiService.createProducto(productoDTO)
    }

    suspend fun updateProducto(id: Long, producto: Producto): Response<Producto> {
        return apiService.updateProducto(id, producto)
    }

    suspend fun deleteProducto(id: Long): Response<Void> {
        return apiService.deleteProducto(id)
    }

    suspend fun createIngrediente(ingrediente: Ingrediente): Response<Ingrediente> {
        return apiService.createIngrediente(ingrediente)
    }

    suspend fun updateIngrediente(id: Long, ingrediente: Ingrediente): Response<Ingrediente> {
        return apiService.updateIngrediente(id, ingrediente)
    }

    suspend fun deleteIngrediente(id: Long): Response<Void> {
        return apiService.deleteIngrediente(id)
    }
}
