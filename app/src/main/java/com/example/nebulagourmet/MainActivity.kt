package com.example.nebulagourmet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nebulagourmet.data.model.InvitadoResponse
import com.example.nebulagourmet.data.model.LoginResponseDTO
import com.example.nebulagourmet.ui.screens.*
import com.example.nebulagourmet.ui.theme.NebulaGourmetTheme
import com.example.nebulagourmet.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NebulaGourmetTheme {
                NebulaApp()
            }
        }
    }
}

@Composable
fun NebulaApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()
    
    // Observar el estado de autenticación para redirección inicial o persistencia
    LaunchedEffect(authState) {
        if (authState is AuthState.Success && navController.currentDestination?.route == "login") {
            val isGuest = authViewModel.sesionId != null
            if (authViewModel.userRole == "ADMIN") {
                navController.navigate("admin_home") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                navController.navigate("home/$isGuest") {
                    popUpTo("login") { inclusive = true }
                }
            }
        } else if (authState is AuthState.LoggedOut) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = if (authViewModel.usuarioId != null || authViewModel.sesionId != null) "loading_gate" else "login") {
        composable("loading_gate") {
            // Pantalla intermedia para decidir a dónde ir basándose en el rol persistido
            LaunchedEffect(Unit) {
                if (authViewModel.userRole == "ADMIN") {
                    navController.navigate("admin_home") { popUpTo("loading_gate") { inclusive = true } }
                } else {
                    val isGuest = authViewModel.sesionId != null
                    navController.navigate("home/$isGuest") { popUpTo("loading_gate") { inclusive = true } }
                }
            }
        }

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { isGuest ->
                    if (authViewModel.userRole == "ADMIN") {
                        navController.navigate("admin_home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home/$isGuest") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("admin_home") {
            AdminHomeScreen(
                onNavigateToOrders = { navController.navigate("admin_orders") },
                onNavigateToInventory = { navController.navigate("admin_inventory") },
                onNavigateToDelivery = { navController.navigate("admin_delivery") },
                onLogout = { authViewModel.logout() }
            )
        }

        composable("admin_orders") {
            AdminOrderManagementScreen(
                orderViewModel = orderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("admin_inventory") {
            AdminInventoryScreen(
                productViewModel = productViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("admin_delivery") {
            AdminDeliveryStaffScreen(
                orderViewModel = orderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(
            route = "home/{isGuest}",
            arguments = listOf(navArgument("isGuest") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isGuest = backStackEntry.arguments?.getBoolean("isGuest") ?: false
            HomeScreen(
                isGuest = isGuest,
                onNavigateToMenu = { navController.navigate("menu") },
                onNavigateToOrders = { navController.navigate("orders") },
                onNavigateToDelivery = { navController.navigate("domicilios") },
                onNavigateToCart = { navController.navigate("cart") },
                onLogout = { authViewModel.logout() }
            )
        }

        composable("domicilios") {
            DeliveryTrackingScreen(
                orderViewModel = orderViewModel,
                usuarioId = authViewModel.usuarioId,
                sesionId = authViewModel.sesionId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("orders") {
            OrderHistoryScreen(
                orderViewModel = orderViewModel,
                usuarioId = authViewModel.usuarioId,
                sesionId = authViewModel.sesionId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("menu") {
            MenuScreen(
                viewModel = productViewModel,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate("product_detail/$productId")
                },
                onCartClick = { navController.navigate("cart") }
            )
        }

        composable(
            route = "product_detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            ProductDetailScreen(
                productId = productId,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                onBackClick = { navController.popBackStack() },
                onAddedToCart = { navController.navigate("cart") }
            )
        }

        composable("cart") {
            CartScreen(
                cartViewModel = cartViewModel,
                onBackClick = { navController.popBackStack() },
                onCheckoutClick = { 
                    orderViewModel.resetOrderState()
                    navController.navigate("checkout") 
                },
                onAddMoreClick = { navController.navigate("menu") }
            )
        }

        composable("checkout") {
            CheckoutScreen(
                navController = navController,
                cartViewModel = cartViewModel,
                orderViewModel = orderViewModel,
                usuarioId = authViewModel.usuarioId,
                sesionId = authViewModel.sesionId,
                onBackClick = { navController.popBackStack() },
                onOrderPlaced = {
                    navController.navigate("orders") {
                        popUpTo("home/${authViewModel.usuarioId == null}") { inclusive = false }
                    }
                }
            )
        }
    }
}
