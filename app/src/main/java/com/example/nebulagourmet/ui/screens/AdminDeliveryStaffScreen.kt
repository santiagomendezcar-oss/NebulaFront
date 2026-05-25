package com.example.nebulagourmet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nebulagourmet.data.model.Domiciliario
import com.example.nebulagourmet.ui.viewmodel.OrderState
import com.example.nebulagourmet.ui.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDeliveryStaffScreen(
    orderViewModel: OrderViewModel,
    onBackClick: () -> Unit
) {
    // Suscripción a ambos estados: la lista y el estado de la operación (Loading/Error/Success)
    val domiciliarios by orderViewModel.domiciliarios.collectAsState()
    val orderState by orderViewModel.orderState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        orderViewModel.fetchDomiciliarios()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GESTIÓN DE REPARTIDORES", style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 2.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Mostrar error visual si el backend falla (ej: error de transacción)
            if (orderState is OrderState.Error) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        text = (orderState as OrderState.Error).message.uppercase(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (domiciliarios.isEmpty() && orderState !is OrderState.Loading) {
                    Text(
                        "NO HAY REPARTIDORES REGISTRADOS", 
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    )
                } else if (orderState is OrderState.Loading && domiciliarios.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(domiciliarios) { rep ->
                            DeliveryStaffCard(rep)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddDeliveryStaffDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { nombre, telefono ->
                    orderViewModel.createDomiciliario(Domiciliario(nombre = nombre, telefono = telefono))
                    showAddDialog = false
                },
                isLoading = orderState is OrderState.Loading
            )
        }
    }
}

@Composable
fun DeliveryStaffCard(rep: Domiciliario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rep.nombre.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(rep.nombre.uppercase(), fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 13.sp)
                Text(rep.telefono, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (rep.activo) "ACTIVO" else "INACTIVO",
                    color = if (rep.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${rep.calificacion}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(" ⭐", fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
fun AddDeliveryStaffDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    isLoading: Boolean = false
) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        title = { 
            Text(
                "REGISTRAR REPARTIDOR", 
                style = MaterialTheme.typography.titleMedium,
                letterSpacing = 2.sp,
                fontSize = 16.sp
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )

                OutlinedTextField(
                    value = nombre, 
                    onValueChange = { nombre = it }, 
                    label = { Text("NOMBRE COMPLETO", fontSize = 9.sp, letterSpacing = 1.sp) },
                    shape = RoundedCornerShape(0.dp),
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefono, 
                    onValueChange = { telefono = it }, 
                    label = { Text("TELÉFONO DE CONTACTO", fontSize = 9.sp, letterSpacing = 1.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(0.dp),
                    singleLine = true,
                    colors = colors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if(nombre.isNotBlank() && telefono.isNotBlank()) onConfirm(nombre, telefono) },
                shape = RoundedCornerShape(0.dp),
                enabled = !isLoading && nombre.isNotBlank() && telefono.isNotBlank(),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("CONFIRMAR REGISTRO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("DESCARTAR", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) 
            }
        }
    )
}

