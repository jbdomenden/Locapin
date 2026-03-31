package com.locapin.mobile.core.common

sealed interface LocaPinResult<out T> {
    data class Success<T>(val data: T) : LocaPinResult<T>
    data class Error(val message: String, val code: Int? = null) : LocaPinResult<Nothing>
    data object Loading : LocaPinResult<Nothing>
}
