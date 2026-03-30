package com.locapin.mobile.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = LocaPinPrimary,
    secondary = LocaPinSecondary,
    tertiary = LocaPinAccent,
    background = LocaPinSurface,
    surface = LocaPinSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = LocaPinDark
)

private val DarkColors = darkColorScheme(
    primary = LocaPinSecondary,
    secondary = LocaPinPrimary,
    tertiary = LocaPinAccent
)

@Composable
fun LocaPinTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LocaPinTypography,
        content = content
    )
}
