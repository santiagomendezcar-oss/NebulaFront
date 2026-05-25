package com.example.nebulagourmet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulagourmet.data.model.Ingrediente
import com.example.nebulagourmet.data.model.Producto
import com.example.nebulagourmet.ui.viewmodel.CartViewModel
import com.example.nebulagourmet.ui.viewmodel.ProductState
import com.example.nebulagourmet.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    onBackClick: () -> Unit,
    onAddedToCart: () -> Unit
) {
    val productState by productViewModel.productState.collectAsState()
    val ingredientesExtra by productViewModel.ingredientesExtra.collectAsState()

    LaunchedEffect(productId) {
        productViewModel.fetchProductById(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "PERSONALIZAR", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            letterSpacing = 6.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Light
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Atrás",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
            when (productState) {
                is ProductState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 2.dp
                    )
                }
                is ProductState.SingleSuccess -> {
                    val producto = (productState as ProductState.SingleSuccess).producto
                    ProductCustomizationContent(
                        producto = producto,
                        ingredientesExtra = ingredientesExtra,
                        onAddToCart = { qty, extras, eliminados, price ->
                            cartViewModel.agregarAlCarrito(producto, qty, extras, eliminados, price)
                            onAddedToCart()
                        }
                    )
                }
                is ProductState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DETALLE NO DISPONIBLE",
                            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 2.sp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProductCustomizationContent(
    producto: Producto,
    ingredientesExtra: List<Ingrediente>,
    onAddToCart: (Int, List<Long>, List<Long>, Double) -> Unit
) {
    var cantidad by remember { mutableIntStateOf(1) }
    val eliminados = remember { mutableStateListOf<Long>() }
    val adicionales = remember { mutableStateListOf<Long>() }

    val precioCalculado = remember(cantidad, adicionales, eliminados) {
        val extrasPrice = adicionales.sumOf { id -> 
            ingredientesExtra.find { it.id == id }?.precioExtra ?: 0.0 
        }
        (producto.precioBase + extrasPrice) * cantidad
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = producto.nombre.uppercase(), 
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 3.sp
                    ), 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = producto.descripcion, 
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    "INGREDIENTES BASE", 
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(producto.ingredientesBase) { ing ->
                val isEliminado = eliminados.contains(ing.id)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = !isEliminado,
                        onCheckedChange = { checked ->
                            if (checked) eliminados.remove(ing.id) else ing.id?.let { eliminados.add(it) }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        ing.nombre, 
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = if (isEliminado) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary,
                            fontWeight = if (isEliminado) FontWeight.Light else FontWeight.Normal
                        )
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "ADICIONALES", 
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(ingredientesExtra.filter { extra -> producto.ingredientesBase.none { it.id == extra.id } }) { extra ->
                val isSeleccionado = adicionales.contains(extra.id)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSeleccionado,
                        onCheckedChange = { checked ->
                            if (checked) extra.id?.let { adicionales.add(it) } else adicionales.remove(extra.id)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        extra.nombre, 
                        modifier = Modifier.weight(1f), 
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = if (isSeleccionado) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    Text(
                        "+$${extra.precioExtra}",
                        color = MaterialTheme.colorScheme.secondary, 
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(0.dp))
                    .background(Color.Transparent)
            ) {
                IconButton(onClick = { if (cantidad > 1) cantidad-- }) {
                    Text("-", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                }
                Text(
                    "$cantidad", 
                    style = MaterialTheme.typography.titleSmall, 
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { cantidad++ }) {
                    Text("+", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                }
            }
            
            Button(
                onClick = { 
                    onAddToCart(cantidad, adicionales.toList(), eliminados.toList(), precioCalculado / cantidad) 
                },
                modifier = Modifier.height(48.dp).weight(1f).padding(start = 16.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    "AGREGAR • $${precioCalculado}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                )
            }
        }
    }
}
