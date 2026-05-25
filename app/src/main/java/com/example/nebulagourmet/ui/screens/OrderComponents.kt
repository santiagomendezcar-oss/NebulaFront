package com.example.nebulagourmet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulagourmet.data.model.EstadoPedido
import com.example.nebulagourmet.data.model.EstadoDomicilio
import com.example.nebulagourmet.data.model.Pedido
import com.example.nebulagourmet.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveOrderCard(pedido: Pedido) {
    var expanded by remember { mutableStateOf(true) }
    
    // Definimos los colores de estado al principio para usarlos en todo el card
    val (statusText, statusColor) = when (pedido.estado) {
        EstadoPedido.PENDIENTE -> Pair("RECIBIDO", Color(0xFFE6A15C))
        EstadoPedido.EN_PREPARACION -> Pair("EN COCINA", Color(0xFFF57C00))
        EstadoPedido.LISTO -> Pair(if (pedido.esDomicilio == true) "POR ENVIAR" else "LISTO", Color(0xFF1976D2))
        else -> Pair("ACTIVO", MaterialTheme.colorScheme.primary)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp), // Esquinas rectas boutique
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ORDEN #${pedido.id}",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (pedido.esDomicilio == true) "ENTREGA A DOMICILIO" else "RECOGER EN LOCAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar animada con el color del estado
            val progress = when(pedido.estado) {
                EstadoPedido.PENDIENTE -> 0.2f
                EstadoPedido.EN_PREPARACION -> 0.5f
                EstadoPedido.LISTO -> 0.8f
                else -> 0.9f
            }
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tracking Message
            val trackingMsg = when (pedido.estado) {
                EstadoPedido.PENDIENTE -> "Tu pedido ha sido recibido y está esperando turno."
                EstadoPedido.EN_PREPARACION -> "¡El chef está preparando tus platos!"
                EstadoPedido.LISTO -> if (pedido.esDomicilio == true) 
                    "Tu pedido está empacado y esperando al repartidor." 
                    else "¡Todo listo! Ya puedes pasar por tu pedido."
                else -> ""
            }
            
            Text(
                text = trackingMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            // Sección del Domiciliario (Si aplica)
            if (pedido.esDomicilio == true) {
                val domicilio = pedido.domicilio
                if (domicilio != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(0.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when(domicilio.estado) {
                                    EstadoDomicilio.ASIGNADO -> "REPARTIDOR ASIGNADO"
                                    EstadoDomicilio.EN_CAMINO -> "EN CAMINO A TU UBICACIÓN"
                                    EstadoDomicilio.EN_ENTREGA -> "EL REPARTIDOR ESTÁ CERCA"
                                    else -> "PREPARANDO ENVÍO"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp
                            )
                        }
                        
                        if (domicilio.domiciliario != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(0.dp)).border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(0.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Repartidor: ${domicilio.domiciliario.nombre.uppercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                
                pedido.items?.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.cantidad}x ${item.nombreAMostrar.uppercase()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Text("$${(item.precioUnitario ?: 0.0) * item.cantidad}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("TOTAL: $${pedido.total}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                }
            }
        }
    }
}
