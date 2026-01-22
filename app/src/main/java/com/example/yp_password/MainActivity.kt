package com.example.yp_password

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.yp_password.ui.PasswordViewModel
import com.example.yp_password.ui.screens.AddPasswordScreen
import com.example.yp_password.ui.screens.LoginScreen
import com.example.yp_password.ui.theme.YP_PasswordTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            YP_PasswordTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Используем новую фабрику без аргументов
                    val viewModel: PasswordViewModel = viewModel(factory = PasswordViewModel.Factory)
                    val navController = rememberNavController()

                    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {

                        composable("login") {
                            if (isAuthenticated) {
                                // Если вошли - летим на главный экран
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }

                            LoginScreen(
                                viewModel = viewModel,
                                isFirstRun = false
                            )
                        }

                        composable("home") {
                            AddPasswordScreen(
                                viewModel = viewModel,
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
