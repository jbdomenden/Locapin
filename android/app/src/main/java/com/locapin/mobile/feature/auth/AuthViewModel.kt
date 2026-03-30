package com.locapin.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locapin.mobile.core.common.LocaPinResult
import com.locapin.mobile.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String) = submit { repo.login(email, password) }
    fun register(name: String, email: String, password: String) = submit { repo.register(name, email, password) }
    fun forgotPassword(email: String) = submit { repo.forgotPassword(email) }

    private fun submit(action: suspend () -> LocaPinResult<Unit>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = action()) {
                is LocaPinResult.Success -> _state.value = _state.value.copy(isLoading = false, isAuthenticated = true)
                is LocaPinResult.Error -> _state.value = _state.value.copy(isLoading = false, error = result.message)
                else -> Unit
            }
        }
    }
}
