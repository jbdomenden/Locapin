package com.locapin.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locapin.mobile.core.common.LocaPinResult
import com.locapin.mobile.core.datastore.UserPreferencesDataStore
import com.locapin.mobile.domain.model.Category
import com.locapin.mobile.domain.model.Destination
import com.locapin.mobile.domain.model.User
import com.locapin.mobile.domain.repository.AuthRepository
import com.locapin.mobile.domain.repository.DestinationRepository
import com.locapin.mobile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

data class MainUiState(
    val loading: Boolean = true,
    val onboarded: Boolean = false,
    val authed: Boolean = false,
    val destinations: List<Destination> = emptyList(),
    val categories: List<Category> = emptyList(),
    val favorites: List<Destination> = emptyList(),
    val selectedDestination: Destination? = null,
    val profile: User? = null,
    val searchResults: List<Destination> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefs: UserPreferencesDataStore,
    private val authRepository: AuthRepository,
    private val destinationRepository: DestinationRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()
    val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            prefs.hasCompletedOnboarding.collectLatest { onboarded ->
                _state.value = _state.value.copy(onboarded = onboarded, loading = false)
            }
        }
        viewModelScope.launch {
            authRepository.authToken.collectLatest { token ->
                _state.value = _state.value.copy(authed = !token.isNullOrBlank())
                if (!token.isNullOrBlank()) bootstrap()
            }
        }
        viewModelScope.launch {
            queryFlow.debounce(300).collectLatest { query ->
                if (query.isNotBlank()) {
                    when (val res = destinationRepository.getDestinations(query = query)) {
                        is LocaPinResult.Success -> _state.value = _state.value.copy(searchResults = res.data)
                        else -> Unit
                    }
                } else _state.value = _state.value.copy(searchResults = emptyList())
            }
        }
    }

    fun completeOnboarding() = viewModelScope.launch { prefs.setOnboardingCompleted(true) }
    fun setSearchQuery(q: String) {
        queryFlow.value = q
        viewModelScope.launch { if (q.isNotBlank()) prefs.addRecentSearch(q) }
    }

    fun bootstrap() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val destinations = (destinationRepository.getDestinations() as? LocaPinResult.Success)?.data.orEmpty()
            val categories = (destinationRepository.getCategories() as? LocaPinResult.Success)?.data.orEmpty()
            val favorites = (destinationRepository.getFavorites() as? LocaPinResult.Success)?.data.orEmpty()
            val profile = (profileRepository.getProfile() as? LocaPinResult.Success)?.data
            _state.value = _state.value.copy(
                loading = false,
                destinations = destinations,
                categories = categories,
                favorites = favorites,
                profile = profile
            )
        }
    }

    fun openDestination(id: String) {
        viewModelScope.launch {
            when (val detail = destinationRepository.getDestinationDetail(id)) {
                is LocaPinResult.Success -> _state.value = _state.value.copy(selectedDestination = detail.data)
                is LocaPinResult.Error -> _state.value = _state.value.copy(error = detail.message)
                else -> Unit
            }
        }
    }

    fun toggleFavorite(destination: Destination) {
        viewModelScope.launch {
            destinationRepository.setFavorite(destination.id, !destination.isFavorite)
            bootstrap()
        }
    }

    fun logout() = viewModelScope.launch { authRepository.logout() }
}
