package com.dimitrije.hiddengems

import com.dimitrije.hiddengems.navigation.*
import com.dimitrije.hiddengems.ui.screens.*

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HiddenGemsNavHost()
            }
        }
    }
}

@Composable
fun HiddenGemsNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.Start) {
        composable(AppRoutes.Start) {
            StartScreen(
                onContinue = { navController.navigate(AppRoutes.Splash) }
            )
        }

        composable(AppRoutes.Splash) {
            SplashScreen(
                onAuthenticated = { navController.navigate(AppRoutes.Profile) },
                onUnauthenticated = { navController.navigate(AppRoutes.Login) }
            )
        }

        composable(AppRoutes.Login) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(AppRoutes.Profile) },
                onNavigateToRegister = { navController.navigate(AppRoutes.Register) }
            )
        }

        composable(AppRoutes.Register) {
            RegisterScreen(navController = navController)
        }

        composable(AppRoutes.Profile) {
            ProfileScreen(
                navController = navController,
                onLogout = {
                    navController.popBackStack()
                    navController.navigate(AppRoutes.Login)
                },
                onNavigateToMap = {
                    navController.navigate(AppRoutes.Map)
                }
            )
        }

        composable(AppRoutes.Map) {
            MapScreen(navController = navController)
        }

        composable(AppRoutes.AddGem) {
            AddGemScreen(navController = navController)
        }

//        composable(AppRoutes.Details) { backStackEntry ->
//            val gemId = backStackEntry.arguments?.getString("gemId")
//            DetailsScreen(gemId = gemId)
//        }


    }
}