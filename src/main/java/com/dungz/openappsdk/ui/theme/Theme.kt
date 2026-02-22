package com.dungz.openappsdk.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.dungz.openappsdk.OpenAppConfig

@Composable
fun AdsOpenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeConfig = OpenAppConfig.getThemeConfig()

    val lightColorScheme = lightColorScheme(
        primary = themeConfig.lightPrimary,
        onPrimary = themeConfig.lightOnPrimary,
        primaryContainer = themeConfig.lightPrimaryContainer,
        onPrimaryContainer = themeConfig.lightOnPrimaryContainer,
        secondary = themeConfig.lightSecondary,
        onSecondary = themeConfig.lightOnSecondary,
        secondaryContainer = themeConfig.lightSecondaryContainer,
        onSecondaryContainer = themeConfig.lightOnSecondaryContainer,
        tertiary = themeConfig.lightTertiary,
        onTertiary = themeConfig.lightOnTertiary,
        tertiaryContainer = themeConfig.lightTertiaryContainer,
        onTertiaryContainer = themeConfig.lightOnTertiaryContainer,
        background = themeConfig.lightBackground,
        onBackground = themeConfig.lightOnBackground,
        surface = themeConfig.lightSurface,
        onSurface = themeConfig.lightOnSurface,
        surfaceVariant = themeConfig.lightSurfaceVariant,
        onSurfaceVariant = themeConfig.lightOnSurfaceVariant,
        error = themeConfig.lightError,
        onError = themeConfig.lightOnError
    )

    val darkColorScheme = darkColorScheme(
        primary = themeConfig.darkPrimary,
        onPrimary = themeConfig.darkOnPrimary,
        primaryContainer = themeConfig.darkPrimaryContainer,
        onPrimaryContainer = themeConfig.darkOnPrimaryContainer,
        secondary = themeConfig.darkSecondary,
        onSecondary = themeConfig.darkOnSecondary,
        secondaryContainer = themeConfig.darkSecondaryContainer,
        onSecondaryContainer = themeConfig.darkOnSecondaryContainer,
        tertiary = themeConfig.darkTertiary,
        onTertiary = themeConfig.darkOnTertiary,
        tertiaryContainer = themeConfig.darkTertiaryContainer,
        onTertiaryContainer = themeConfig.darkOnTertiaryContainer,
        background = themeConfig.darkBackground,
        onBackground = themeConfig.darkOnBackground,
        surface = themeConfig.darkSurface,
        onSurface = themeConfig.darkOnSurface,
        surfaceVariant = themeConfig.darkSurfaceVariant,
        onSurfaceVariant = themeConfig.darkOnSurfaceVariant,
        error = themeConfig.darkError,
        onError = themeConfig.darkOnError
    )

    val colorScheme = when {
        themeConfig.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
