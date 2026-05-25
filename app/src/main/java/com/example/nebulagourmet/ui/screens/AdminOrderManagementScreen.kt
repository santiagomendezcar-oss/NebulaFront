package com.example.nebulagourmet.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulagourmet.data.model.Domiciliario
import com.example.nebulagourmet.data.model.EstadoPedido
import com.example.nebulagourmet.data.model.Pedido
import com.example.nebulagourmet.ui.viewmodel.OrderState
import com.example.nebulagourmet.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderManagementScreen(
    orderViewModel: OrderViewModel,
    onBackClick: () -> Unit
) {
    val orderState by orderViewModel.orderState.collectAsState()
    val domiciliarios by orderViewModel.domiciliarios.collectAsState()

    var showDeliveryDialog by remember { mutableStateOf<Pedido?>(null) }

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllPedidosAdmin()
        orderViewModel.fetchDomiciliarios()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GESTIÓN DE ÓRDENES", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (orderState) {
                is OrderState.ListSuccess -> {
                    val pedidos = (orderState as OrderState.ListSuccess).pedidos
                    val pedidosOrdenados = remember(pedidos) {
                        pedidos.sortedByDescending { it.id }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(pedidosOrdenados, key = { it.id ?: it.hashCode() }) { pedido ->
                            AdminOrderCard(
                                pedido = pedido,
                                viewModel = orderViewModel,
                                onUpdateStatus = { nuevoEstado ->
                                    orderViewModel.actualizarEstadoPedido(pedido.id!!, nuevoEstado)
                                },
                                onAssignDelivery = {
                                    showDeliveryDialog = pedido
                                }
                            )
                        }
                    }
                }
                is OrderState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> {}
            }
        }

        if (showDeliveryDialog != null) {
            DeliveryAssignmentDialog(
                domiciliarios = domiciliarios,
                onDismiss = { showDeliveryDialog = null },
                onSelect = { domiciliarioId ->
                    showDeliveryDialog?.domicilio?.id?.let { domicilioId ->
                        orderViewModel.asignarDomiciliario(domicilioId, domiciliarioId)
                    }
                    showDeliveryDialog = null
                }
            )
        }
    }
}

@Composable
fun AdminOrderCard(
    pedido: Pedido, 
    viewModel: OrderViewModel,
    onUpdateStatus: (EstadoPedido) -> Unit,
    onAssignDelivery: () -> Unit
) {
    // Cargar detalles si no están presentes
    LaunchedEffect(pedido.id) {
        if ((pedido.items == null || pedido.items.isEmpty()) && pedido.id != null) {
            viewModel.fetchDetallesPedido(pedido.id)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ORDEN #${pedido.id}", fontWeight = FontWeight.Bold)
                Text(pedido.estado.toString(), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
            
            Text("Cliente: ${pedido.usuario?.nombre ?: "Invitado"}", fontSize = 12.sp)
            if (pedido.esDomicilio == true) {
                Text("Tipo: DOMICILIO", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                pedido.domicilio?.let { dom ->
                    Text("Dirección: ${dom.direccion}", fontSize = 11.sp)
                    Text("Estado Envío: ${dom.estado}", fontSize = 11.sp)
                    Text("Repartidor: ${dom.domiciliario?.nombre ?: "Sin asignar"}", fontSize = 11.sp)
                }
            } else {
                Text("Tipo: RETIRO EN LOCAL", fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (pedido.items == null) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
                Text("Cargando productos...", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            } else if (pedido.items.isEmpty()) {
                Text("No hay productos registrados en este pedido", fontSize = 11.sp, color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            } else {
                pedido.items.forEach { item ->
                    Text("- ${item.cantidad}x ${item.nombreAMostrar}", fontSize = 11.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (pedido.estado == EstadoPedido.PENDIENTE) {
                    Button(
                        onClick = { onUpdateStatus(EstadoPedido.EN_PREPARACION) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ACEPTAR", fontSize = 10.sp)
                    }
                    Button(
                        onClick = { onUpdateStatus(EstadoPedido.CANCELADO) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RECHAZAR", fontSize = 10.sp)
                    }
                }
                
                if (pedido.estado == EstadoPedido.EN_PREPARACION) {
                    Button(
                        onClick = { onUpdateStatus(EstadoPedido.LISTO) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("LISTO PARA ENVÍO", fontSize = 10.sp)
                    }
                }

                if (pedido.estado == EstadoPedido.LISTO && pedido.esDomicilio == true && pedido.domicilio?.domiciliarioId == null) {
                    Button(
                        onClick = onAssignDelivery,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ASIGNAR REPARTIDOR", fontSize = 10.sp)
                    }
                }
                
                if (pedido.estado == EstadoPedido.LISTO && pedido.esDomicilio == false) {
                    Button(
                        onClick = { onUpdateStatus(EstadoPedido.ENTREGADO) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("MARCAR ENTREGADO", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryAssignmentDialog(
    domiciliarios: List<Domiciliario>,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar Repartidor") },
        text = {
            LazyColumn {
                items(domiciliarios) { rep ->
                    ListItem(
                        headlineContent = { Text(rep.nombre) },
                        supportingContent = { Text("Rating: ${rep.calificacion} | Tel: ${rep.telefono}") },
                        modifier = Modifier.clickable { onSelect(rep.id!!) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
