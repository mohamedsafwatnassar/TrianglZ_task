package com.trianglz.chatapp.presentation.userInput

import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trianglz.chatapp.presentation.userInput.viewmodel.UserInputViewModel
import com.trianglz.chatapp.presentation.userInput.viewmodel.UsernameEvent

/**
 * Username setup screen
 * First screen shown when user hasn't set up their username
 */
@Composable
fun UserInputScreen(
    onUsernameSet: () -> Unit,
    viewModel: UserInputViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onUsernameSet()
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App branding
                Text(
                    text = "💬",
                    style = MaterialTheme.typography.displayLarge
                )

                Text(
                    text = "Welcome to ChatApp",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Choose a username to get started",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Username input
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { viewModel.onEvent(UsernameEvent.UsernameChanged(it)) },
                    label = { Text("Username") },
                    placeholder = { Text("Enter your name") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    isError = state.error != null,
                    supportingText = if (state.error != null) {
                        { Text(state.error ?: "") }
                    } else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.onEvent(UsernameEvent.SaveUsername(deviceId)) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Continue button
                Button(
                    onClick = { viewModel.onEvent(UsernameEvent.SaveUsername(deviceId)) },
                    enabled = !state.isLoading && state.username.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Continue")
                    }
                }
            }
        }
    }
}