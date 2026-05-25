package com.example.nebulagourmet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulagourmet.ui.theme.*
import com.example.nebulagourmet.data.model.Producto
import com.example.nebulagourmet.ui.viewmodel.ProductState
import com.example.nebulagourmet.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: ProductViewModel,
    onBackClick: () -> Unit,
    onProductClick: (Long) -> Unit,
    onCartClick: () -> Unit
) {
    val productState by viewModel.productState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchProductos()
    }

    Scaffold(
        containerColor = BoutiqueBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "L A   C A R T A",
                        style = MaterialTheme.typography.titleMedium.copy(
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Serif
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Regresar",
                            modifier = Modifier.size(20.dp),
                            tint = BoutiquePrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            Icons.Default.ShoppingCart, 
                            contentDescription = "Carrito",
                            modifier = Modifier.size(22.dp),
                            tint = BoutiquePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BoutiqueBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BoutiqueBackground)
        ) {
            when (productState) {
                is ProductState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BoutiquePrimary,
                        strokeWidth = 2.dp
                    )
                }
                is ProductState.Success -> {
                    val productos = (productState as ProductState.Success).productos
                    val productosPorCategoria = productos.groupBy { it.categoria }

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 40.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        productosPorCategoria.forEach { (categoria, listaProductos) ->
                            item {
                                CategoryHeader(titulo = categoria)
                            }
                            items(listaProductos) { producto ->
                                ProductItem(producto = producto, onClick = { producto.id?.let { onProductClick(it) } })
                            }
                        }
                    }
                }
                is ProductState.Error -> {
                    ErrorStateView { viewModel.fetchProductos() }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProductItem(producto: Producto, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = producto.nombre.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 1.sp,
                        fontSize = 17.sp
                    ),
                    color = BoutiquePrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$${producto.precioBase}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp
                    ),
                    color = BoutiquePrimary
                )
            }
            
            // Subtítulo de categoría (como en la imagen)
            Text(
                text = producto.categoria.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = BoutiquePrimary.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 22.sp
                ),
                color = BoutiqueSecondary,
                maxLines = 3
            )
        }
    }
}

@Composable
fun CategoryHeader(titulo: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = titulo.uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp
            ),
            color = BoutiquePrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Línea sutil decorativa
        Box(
            modifier = Modifier
                .width(45.dp)
                .height(1.5.dp)
                .background(BoutiquePrimary)
        )
    }
}

@Composable
fun ErrorStateView(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(BoutiqueBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SERVICIO NO DISPONIBLE",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = BoutiquePrimary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BoutiquePrimary),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text("REINTENTAR", color = BoutiqueBackground)
        }
    }
}
