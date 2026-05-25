package com.example.nebulagourmet.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulagourmet.data.model.EstadoPedido
import com.example.nebulagourmet.data.model.Pedido
import com.example.nebulagourmet.ui.theme.GoldAccent
import com.example.nebulagourmet.ui.viewmodel.OrderState
import com.example.nebulagourmet.ui.viewmodel.OrderViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
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
                        "HISTORIAL", 
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
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
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 2.dp
                    )
                }
                is OrderState.ListSuccess -> {
                    val todosLosPedidos = (orderState as OrderState.ListSuccess).pedidos

                    if (todosLosPedidos.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AÚN NO HAS REALIZADO PEDIDOS",
                                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 2.sp),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Light
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            item {
                                Text(
                                    "TODOS TUS PEDIDOS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(todosLosPedidos, key = { it.id ?: it.hashCode() }) { pedido ->
                                KeyedOrderItem(pedido, orderViewModel)
                            }
                        }
                    }
                }
                is OrderState.Error -> {
                    val errorMsg = (orderState as OrderState.Error).message
                    val displayError = if (errorMsg.contains("Internal Server Error") || errorMsg.contains("500")) {
                        "ERROR DEL SERVIDOR: Verifica que la base de datos tenga la columna 'es_domicilio' y 'domicilio_id'."
                    } else {
                        errorMsg.uppercase()
                    }
                    
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayError,
                            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { orderViewModel.fetchPedidos(usuarioId, sesionId) },
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text("REINTENTAR")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun KeyedOrderItem(pedido: Pedido, viewModel: OrderViewModel) {
    // Usamos el ID del pedido para mantener el estado de expansión si la lista se recrea
    // pero el pedido es el mismo.
    OrderItem(pedido, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItem(pedido: Pedido, viewModel: OrderViewModel) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "rotation"
    )

    // Log para verificar si el estado cambia y la reactividad de la UI
    Log.d("OrderItem", "Rendering Pedido #${pedido.id}. Expanded: $expanded, ItemCount: ${pedido.items?.size ?: "null"}")

    // Cargar detalles de forma proactiva si se expande y no hay artículos (null)
    LaunchedEffect(expanded) {
        if (expanded) {
            Log.i("OrderHistory", "Intento de expansión pedido ${pedido.id}. Items: ${pedido.items?.size ?: "null"}")
            if ((pedido.items == null || pedido.items.isEmpty()) && pedido.id != null) {
                Log.w("OrderHistory", "Faltan detalles para el pedido ${pedido.id}. Iniciando carga...")
                viewModel.fetchDetallesPedido(pedido.id)
            }
        }
    }

    val formattedDate = remember(pedido.fecha) {
        try {
            if (pedido.fecha != null) {
                val cleanedDate = pedido.fecha.substringBefore(".")
                val ldt = LocalDateTime.parse(cleanedDate)
                ldt.format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm", Locale("es", "ES")))
                    .uppercase()
            } else {
                "FECHA NO DISPONIBLE"
            }
        } catch (e: Exception) {
            pedido.fecha ?: "FECHA NO DISPONIBLE"
        }
    }

    Surface(
        onClick = { 
            expanded = !expanded 
            Log.d("OrderHistory", "EVENTO CLIC: Pedido #${pedido.id} -> Expanded: $expanded")
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PEDIDO #${pedido.id}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 2.sp,
                                fontSize = 14.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(rotationState),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                val (statusText, statusColor) = when (pedido.estado) {
                    EstadoPedido.PENDIENTE -> Pair("RECIBIDO", Color(0xFFE6A15C))
                    EstadoPedido.EN_PREPARACION -> Pair("EN COCINA", Color(0xFFF57C00))
                    EstadoPedido.LISTO -> {
                        val text = if (pedido.esDomicilio == true) "POR ENVIAR" else "LISTO"
                        Pair(text, Color(0xFF1976D2))
                    }
                    EstadoPedido.ENTREGADO -> Pair("ENTREGADO", Color(0xFF4CAF50))
                    EstadoPedido.CANCELADO -> Pair("CANCELADO", Color(0xFFD32F2F))
                    else -> Pair("DESCONOCIDO", Color.Gray)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // SECCIÓN DE PRODUCTOS
                    Text(
                        "ARTÍCULOS DEL PEDIDO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        pedido.items == null -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.width(100.dp).height(2.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "BUSCANDO PRODUCTOS...",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        pedido.items.isEmpty() -> {
                            Text(
                                "NO SE ENCONTRARON DETALLES PARA ESTE PEDIDO",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 12.dp),
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> {
                            pedido.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.cantidad}x ${item.nombreAMostrar.uppercase()}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            letterSpacing = 1.sp,
                                            fontSize = 12.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$${(item.precioUnitario ?: 0.0) * item.cantidad}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = FontFamily.Serif
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // TOTAL DESTACADO
                    Surface(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL PAGADO",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "$${pedido.total}",
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Serif),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // SECCIÓN DE SEGUIMIENTO (Solo si está activo)
                    if (pedido.estado != EstadoPedido.ENTREGADO && pedido.estado != EstadoPedido.CANCELADO) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Text(
                                "ESTADO DE ENTREGA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 2.sp
                            )
                            val infoSeguimiento = when (pedido.estado) {
                                EstadoPedido.PENDIENTE -> "Revisando orden..."
                                EstadoPedido.EN_PREPARACION -> "En cocina..."
                                EstadoPedido.LISTO -> if (pedido.esDomicilio == true) "Listo para envío" else "Listo para recoger"
                                else -> ""
                            }
                            Text(
                                text = infoSeguimiento.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            pedido.domicilio?.domiciliario?.let { domi ->
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Text(domi.nombre.take(1).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("REPARTIDOR: ${domi.nombre.uppercase()}", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            if (!expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                val totalItems = pedido.items?.sumOf { it.cantidad } ?: 0
                Text(
                    text = "VER DETALLES ($totalItems ARTÍCULOS)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }
    }
}
