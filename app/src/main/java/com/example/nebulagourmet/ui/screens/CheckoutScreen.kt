package com.example.nebulagourmet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import com.example.nebulagourmet.data.model.DomicilioDTO
import com.example.nebulagourmet.data.model.PedidoDTO
import com.example.nebulagourmet.data.model.PedidoDomicilioDTO
import com.example.nebulagourmet.ui.viewmodel.CartViewModel
import com.example.nebulagourmet.ui.viewmodel.OrderState
import com.example.nebulagourmet.ui.viewmodel.OrderViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    usuarioId: Long?,
    sesionId: String?,
    onBackClick: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val scrollState = rememberScrollState()
    var esDomicilio by remember { mutableStateOf(true) }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var metodoPago by remember { mutableStateOf("EFECTIVO") }
    var nombreCliente by remember { mutableStateOf("") }
    
    val orderState by orderViewModel.orderState.collectAsState()

    LaunchedEffect(orderState) {
        if (orderState is OrderState.Success) {
            cartViewModel.limpiarCarrito()
            val pedido = (orderState as OrderState.Success).pedido
            if (pedido.esDomicilio == true) {
                navController.navigate("domicilios") {
                    popUpTo("home/${usuarioId == null}") { inclusive = false }
                }
            } else {
                onOrderPlaced()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "REVISAR PEDIDO", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Serif
                        ) 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.primary)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Manejo de Errores Boutique
            if (orderState is OrderState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = (orderState as OrderState.Error).message.uppercase(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Sección: Tipo de Entrega
            Column {
                SectionLabel("MÉTODO DE ENTREGA")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BoutiqueChip(
                        selected = !esDomicilio,
                        text = "RECOGER EN LOCAL",
                        onClick = { esDomicilio = false },
                        modifier = Modifier.weight(1f)
                    )
                    BoutiqueChip(
                        selected = esDomicilio,
                        text = "DOMICILIO",
                        onClick = { esDomicilio = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sección: Datos del Cliente
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionLabel("INFORMACIÓN DEL CLIENTE")
                
                BoutiqueTextField(
                    value = nombreCliente,
                    onValueChange = { nombreCliente = it },
                    label = "NOMBRE PARA EL PEDIDO"
                )

                if (esDomicilio) {
                    BoutiqueTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = "DIRECCIÓN DE ENTREGA"
                    )
                    BoutiqueTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = "TELÉFONO DE CONTACTO"
                    )
                    BoutiqueTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = "INSTRUCCIONES ESPECIALES (OPCIONAL)",
                        singleLine = false
                    )
                }
            }

            // Resumen de Pago Estilo Factura
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(0.dp))
                    .padding(24.dp)
            ) {
                SectionLabel("RESUMEN DE PAGO")
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SUBTOTAL", fontSize = 11.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("$${cartViewModel.total}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("TOTAL A PAGAR", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text(
                        "$${cartViewModel.total}", 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Light, 
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Botón de Acción Principal
            if (orderState is OrderState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 2.dp
                )
            } else {
                Button(
                    onClick = {
                        if (nombreCliente.isBlank() || (esDomicilio && (direccion.isBlank() || telefono.isBlank()))) return@Button
                        
                        val pedidoDTO = PedidoDTO(
                            clienteNombre = nombreCliente,
                            metodoPago = metodoPago,
                            productos = cartViewModel.toProductoPersonalizadoDTOs()
                        )
                        
                        if (esDomicilio) {
                            val request = PedidoDomicilioDTO(
                                pedido = pedidoDTO,
                                domicilio = DomicilioDTO(direccion = direccion, telefonoContacto = telefono, instruccionesEspeciales = notas, nombreContacto = nombreCliente)
                            )
                            if (usuarioId != null) orderViewModel.crearPedidoConDomicilioParaUsuario(usuarioId, request)
                            else if (sesionId != null) orderViewModel.crearPedidoConDomicilioParaInvitado(sesionId, request)
                        } else {
                            if (usuarioId != null) orderViewModel.crearPedidoParaUsuario(usuarioId, pedidoDTO)
                            else if (sesionId != null) orderViewModel.crearPedidoParaInvitado(sesionId, pedidoDTO)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    enabled = nombreCliente.isNotBlank() && (!esDomicilio || (direccion.isNotBlank() && telefono.isNotBlank()))
                ) {
                    Text("CONFIRMAR Y FINALIZAR", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    )
}

@Composable
fun BoutiqueTextField(value: String, onValueChange: (String) -> Unit, label: String, singleLine: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 9.sp, letterSpacing = 1.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        singleLine = singleLine
    )
}

@Composable
fun BoutiqueChip(selected: Boolean, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(0.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 9.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 1.sp,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
