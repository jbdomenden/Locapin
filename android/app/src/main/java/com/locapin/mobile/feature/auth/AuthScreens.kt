package com.locapin.mobile.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locapin.mobile.R

@Composable
fun LoginScreen(
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    onSuccess: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    if (state.isAuthenticated) onSuccess()
    AuthFormScreen(
        title = "Welcome to LocaPin",
        button = "Login",
        showName = false,
        error = state.error,
        onSubmit = { _, email, password -> vm.login(email, password) },
        altAction = { TextButton(onClick = onRegister) { Text("Create account") } },
        extraAction = { TextButton(onClick = onForgotPassword) { Text("Forgot password?") } }
    )
}

@Composable
fun RegisterScreen(onBack: () -> Unit, onSuccess: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    if (state.isAuthenticated) onSuccess()
    AuthFormScreen(
        title = "Create your LocaPin account",
        button = "Register",
        showName = true,
        error = state.error,
        onSubmit = vm::register,
        altAction = { TextButton(onClick = onBack) { Text("Back to login") } }
    )
}

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    val state by vm.state.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reset password", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.forgotPassword(email) }, modifier = Modifier.fillMaxWidth()) { Text("Send reset link") }
        TextButton(onClick = onBack) { Text("Back") }
    }
}

@Composable
private fun AuthFormScreen(
    title: String,
    button: String,
    showName: Boolean,
    error: String?,
    onSubmit: (name: String, email: String, password: String) -> Unit,
    altAction: @Composable (() -> Unit)? = null,
    extraAction: @Composable (() -> Unit)? = null
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.ic_locapin_logo), contentDescription = "LocaPin")
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (showName) OutlinedTextField(name, { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onSubmit(name, email, password) }, modifier = Modifier.fillMaxWidth()) { Text(button) }
        altAction?.invoke(); extraAction?.invoke()
    }
}
