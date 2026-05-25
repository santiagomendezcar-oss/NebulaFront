package com.example.nebulagourmet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulagourmet.data.model.Ingrediente
import com.example.nebulagourmet.data.model.Producto
import com.example.nebulagourmet.data.model.ProductoDTO
import com.example.nebulagourmet.ui.viewmodel.AdminActionState
import com.example.nebulagourmet.ui.viewmodel.ProductState
import com.example.nebulagourmet.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInventoryScreen(
    productViewModel: ProductViewModel,
    onBackClick: () -> Unit
) {
    val state by productViewModel.productState.collectAsState()
    val adminActionState by productViewModel.adminActionState.collectAsState()
    val allIngredientes by productViewModel.allIngredientes.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("PRODUCTOS", "INGREDIENTES")

    var showProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Producto?>(null) }
    
    var showIngredientDialog by remember { mutableStateOf(false) }
    var ingredientToEdit by remember { mutableStateOf<Ingrediente?>(null) }
    
    var showDeleteConfirm by remember { mutableStateOf<Any?>(null) }

    val refreshAll = {
        productViewModel.fetchProductos(admin = true)
        productViewModel.fetchAllIngredientes()
    }

    LaunchedEffect(Unit) {
        refreshAll()
    }

    // Handle Admin Action Success
    LaunchedEffect(adminActionState) {
        if (adminActionState is AdminActionState.Success) {
            showProductDialog = false
            productToEdit = null
            showIngredientDialog = false
            ingredientToEdit = null
            productViewModel.resetAdminActionState()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("INVENTARIO", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = refreshAll) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                        }
                        IconButton(onClick = { 
                            if (selectedTab == 0) {
                                productToEdit = null
                                showProductDialog = true 
                            } else {
                                ingredientToEdit = null
                                showIngredientDialog = true
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (selectedTab == 0) {
                // Products List
                when (state) {
                    is ProductState.Success -> {
                        val productos = (state as ProductState.Success).productos
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(productos) { producto ->
                                InventoryItemCard(
                                    nombre = producto.nombre,
                                    subtext = producto.categoria,
                                    price = producto.precioBase,
                                    disponible = producto.disponible,
                                    onEdit = {
                                        productToEdit = producto
                                        showProductDialog = true
                                    },
                                    onDelete = {
                                        showDeleteConfirm = producto
                                    }
                                )
                            }
                        }
                    }
                    is ProductState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    is ProductState.Error -> Text((state as ProductState.Error).message, Modifier.align(Alignment.Center))
                    else -> {}
                }
            } else {
                // Ingredients List
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allIngredientes) { ingrediente ->
                        InventoryItemCard(
                            nombre = ingrediente.nombre,
                            subtext = ingrediente.categoria ?: "Sin categoría",
                            price = ingrediente.precioExtra,
                            disponible = ingrediente.disponible,
                            onEdit = {
                                ingredientToEdit = ingrediente
                                showIngredientDialog = true
                            },
                            onDelete = {
                                showDeleteConfirm = ingrediente
                            }
                        )
                    }
                }
            }

            if (adminActionState is AdminActionState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }

        // Dialogs
        if (showProductDialog) {
            ProductFormDialog(
                producto = productToEdit,
                allIngredientes = allIngredientes,
                onDismiss = { showProductDialog = false },
                onConfirm = { dto ->
                    if (productToEdit == null) {
                        productViewModel.createProducto(dto)
                    } else {
                        val updated = productToEdit!!.copy(
                            nombre = dto.nombre,
                            descripcion = dto.descripcion,
                            precioBase = dto.precioBase,
                            categoria = dto.categoria,
                            disponible = dto.disponible ?: true,
                            ingredientesBase = allIngredientes.filter { dto.ingredientesBaseIds.contains(it.id) }
                        )
                        productViewModel.updateProducto(productToEdit!!.id!!, updated)
                    }
                }
            )
        }

        if (showIngredientDialog) {
            IngredientFormDialog(
                ingrediente = ingredientToEdit,
                onDismiss = { showIngredientDialog = false },
                onConfirm = { ing ->
                    if (ingredientToEdit == null) {
                        productViewModel.createIngrediente(ing)
                    } else {
                        productViewModel.updateIngrediente(ingredientToEdit!!.id!!, ing)
                    }
                }
            )
        }

        if (showDeleteConfirm != null) {
            val name = when (val item = showDeleteConfirm) {
                is Producto -> item.nombre
                is Ingrediente -> item.nombre
                else -> ""
            }
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title = { Text("Confirmar Eliminación") },
                text = { Text("¿Estás seguro de que deseas eliminar '$name'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when (val item = showDeleteConfirm) {
                                is Producto -> productViewModel.deleteProducto(item.id!!)
                                is Ingrediente -> productViewModel.deleteIngrediente(item.id!!)
                            }
                            showDeleteConfirm = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    nombre: String,
    subtext: String,
    price: Double,
    disponible: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.Bold)
                Text(subtext, fontSize = 12.sp, color = Color.Gray)
                Text("$${price}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Text(if (disponible) "Disponible" else "Agotado", 
                    fontSize = 12.sp, 
                    color = if (disponible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientFormDialog(
    ingrediente: Ingrediente?,
    onDismiss: () -> Unit,
    onConfirm: (Ingrediente) -> Unit
) {
    var nombre by remember { mutableStateOf(ingrediente?.nombre ?: "") }
    var precioExtra by remember { mutableStateOf(ingrediente?.precioExtra?.toString() ?: "") }
    var categoria by remember { mutableStateOf(ingrediente?.categoria ?: "") }
    var disponible by remember { mutableStateOf(ingrediente?.disponible ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ingrediente == null) "Nuevo Ingrediente" else "Editar Ingrediente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(
                    value = precioExtra, 
                    onValueChange = { precioExtra = it }, 
                    label = { Text("Precio Extra") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría") })
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = disponible, onCheckedChange = { disponible = it })
                    Text("Disponible")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(Ingrediente(
                    id = ingrediente?.id,
                    nombre = nombre,
                    precioExtra = precioExtra.toDoubleOrNull() ?: 0.0,
                    categoria = categoria,
                    disponible = disponible
                ))
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    producto: Producto?,
    allIngredientes: List<Ingrediente>,
    onDismiss: () -> Unit,
    onConfirm: (ProductoDTO) -> Unit
) {
    var nombre by remember { mutableStateOf(producto?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(producto?.descripcion ?: "") }
    var precioBase by remember { mutableStateOf(producto?.precioBase?.toString() ?: "") }
    var categoria by remember { mutableStateOf(producto?.categoria ?: "") }
    var disponible by remember { mutableStateOf(producto?.disponible ?: true) }
    var selectedIngIds by remember { 
        mutableStateOf(producto?.ingredientesBase?.mapNotNull { it.id }?.toSet() ?: emptySet()) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (producto == null) "Nuevo Producto" else "Editar Producto") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                    OutlinedTextField(
                        value = precioBase, 
                        onValueChange = { precioBase = it }, 
                        label = { Text("Precio Base") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría") })
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = disponible, onCheckedChange = { disponible = it })
                        Text("Disponible")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ingredientes Base:", fontWeight = FontWeight.SemiBold)
                }
                
                items(allIngredientes) { ingrediente ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedIngIds = if (selectedIngIds.contains(ingrediente.id)) {
                                selectedIngIds - ingrediente.id!!
                            } else {
                                selectedIngIds + ingrediente.id!!
                            }
                        }
                    ) {
                        Checkbox(
                            checked = selectedIngIds.contains(ingrediente.id),
                            onCheckedChange = null
                        )
                        Text("${ingrediente.nombre} (+$${ingrediente.precioExtra})")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val dto = ProductoDTO(
                    nombre = nombre,
                    descripcion = descripcion,
                    precioBase = precioBase.toDoubleOrNull() ?: 0.0,
                    categoria = categoria,
                    disponible = disponible,
                    ingredientesBaseIds = selectedIngIds.toList()
                )
                onConfirm(dto)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
