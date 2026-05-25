package com.example.nebulagourmet.domain.model

data class Ingrediente(
    val id: Long? = null,
    val nombre: String,
    val precioExtra: Double,
    val categoria: String? = null,
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
