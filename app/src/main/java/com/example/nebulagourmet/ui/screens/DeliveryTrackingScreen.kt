package com.example.nebulagourmet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulagourmet.data.model.EstadoPedido
import com.example.nebulagourmet.ui.viewmodel.OrderState
import com.example.nebulagourmet.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryTrackingScreen(
    orderViewModel: OrderViewModel,
    usuarioId: Long?,
    sesionId: String?,
    onBackClick: () -> Unit
) {
    val orderState by orderViewModel.orderState.collectAsState()

    LaunchedEffect(Unit) {
        orderViewModel.fetchPedidos(usuarioId, sesionId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "DOMICILIOS", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            letterSpacing = 4.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Light
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background)) {
            when (orderState) {
                is OrderState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                is OrderState.ListSuccess -> {
                    val activos = (orderState as OrderState.ListSuccess).pedidos.filter { 
                        it.esDomicilio == true && it.estado != EstadoPedido.ENTREGADO && it.estado != EstadoPedido.CANCELADO
                    }

                    if (activos.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NO TIENES PEDIDOS EN CURSO",
                                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 2.sp),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(activos) { pedido ->
                                ActiveOrderCard(pedido)
                            }
                        }
                    }
                }
                is OrderState.Error -> {
                    Text(
                        text = (orderState as OrderState.Error).message,
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                else -> {}
            }
        }
    }
}
