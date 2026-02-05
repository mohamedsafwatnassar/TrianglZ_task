package com.trianglz.chatapp.presentation.navigation

import com.trianglz.chatapp.presentation.userInput.UserInputScreen

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trianglz.chatapp.presentation.chat.ChatRoomScreen

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    data object UserInput : Screen("userInput")
    data object ChatRoom : Screen("chat_room")
}

/**
 * Main navigation graph
 */
@Composable
fun AppNavigation(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.UserInput.route) {
            UserInputScreen(
                onUsernameSet = {
                    navController.navigate(Screen.ChatRoom.route) {
                        popUpTo(Screen.UserInput.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ChatRoom.route) {
            ChatRoomScreen()
        }
    }
}