package com.sosauce.cutemusic.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.kmpalette.color
import com.kmpalette.rememberDominantColorState
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.rememberDynamicMaterialThemeState
import com.sosauce.cutemusic.R
import com.sosauce.cutemusic.data.datastore.rememberFollowSys
import com.sosauce.cutemusic.data.datastore.rememberUseAmoledMode
import com.sosauce.cutemusic.data.datastore.rememberUseArtTheme
import com.sosauce.cutemusic.data.datastore.rememberUseDarkMode


private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Composable
fun CuteMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    artImageBitmap: ImageBitmap,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val useDarkMode by rememberUseDarkMode()
    val useAmoledMode by rememberUseAmoledMode()
    val followSys by rememberFollowSys()
    val useArtTheme by rememberUseArtTheme()

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    val colorSchemeToUse = when {
        useAmoledMode -> colorScheme.copy(
            surface = Color.Black,
            inverseSurface = Color.White,
            background = Color.Black,
        )

        followSys -> colorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                context
            )
        }

        else -> DarkColors
    }


    if (useArtTheme) {
        val paletteState = rememberDominantColorState()

        LaunchedEffect(artImageBitmap) {
            paletteState.updateFrom(artImageBitmap)
        }

        val state = rememberDynamicMaterialThemeState(
            seedColor = paletteState.result?.paletteOrNull?.vibrantSwatch?.color
                ?: MaterialTheme.colorScheme.primary,
            isDark = if (followSys) isSystemInDarkTheme() else useDarkMode,
            isAmoled = useAmoledMode
        )

        DynamicMaterialTheme(
            state = state,
            animate = true,
            content = content
        )
    } else {
        MaterialTheme(
            colorScheme = colorSchemeToUse,
            typography = Typography(),
            content = content
        )
    }

}

val GlobalFont = FontFamily(Font(R.font.nunito))
