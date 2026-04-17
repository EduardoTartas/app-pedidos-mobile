package dev.fslab.pedidos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


/**
 * Cores customizadas para a aplicação que mudam conforme o tema
 */
data class PedidosColors(
    val background: Color,
    val backgroundGradientStart: Color,
    val backgroundGradientEnd: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnPrimary: Color,
    val textInput: Color,
    val primary: Color,
    val primaryDark: Color,
    val iconGray: Color,
    val inputBorder: Color,
    val mediumGray: Color,
    val errorBackground: Color,
    val errorText: Color,
    val errorButton: Color,
    val error: Color,
    val successBackground: Color,
    val successText: Color,
    val success: Color,
    val lightGray: Color,
    // Cores para cards de features (variam entre light/dark)
    val featureBlue: Color,
    val featureGreen: Color,
    val featureOrange: Color,
    val featureCyan: Color,
    val featurePink: Color,
    val featureRed: Color,
    val isDark: Boolean = false  // Flag para identificar o tema
)

/**
 * Cores para tema claro — Paleta Purple/Violet premium
 */
val LightPedidosColors = PedidosColors(
    background = PrimaryLightWhite,
    backgroundGradientStart = PrimaryLightWhite,
    backgroundGradientEnd = SurfaceLight,
    surface = SurfaceWhite,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    textOnPrimary = Color.White,
    textInput = TextBlack,
    primary = PrimaryGreen,
    primaryDark = PrimaryNavy,
    iconGray = IconGray,
    inputBorder = Color(0xFFE5E7EB),
    mediumGray = MediumGray,
    errorBackground = ErrorBackground,
    errorText = ErrorText,
    errorButton = ErrorButton,
    error = Color(0xFFDC2626),
    successBackground = SuccessBackground,
    successText = SuccessText,
    success = Color(0xFF10B981),
    lightGray = LightGray,
    // Cores para cards de features (Light — roxo como destaque)
    featureBlue = Color(0xFF7C6AF6),
    featureGreen = Color(0xFF10B981),
    featureOrange = Color(0xFFF59E0B),
    featureCyan = Color(0xFF06B6D4),
    featurePink = Color(0xFFEC4899),
    featureRed = Color(0xFFEF4444),
    isDark = false
)

/**
 * Cores para tema escuro — Paleta Purple/Navy premium
 */
val DarkPedidosColors = PedidosColors(
    background = PrimaryNavy,
    backgroundGradientStart = PrimaryNavy,
    backgroundGradientEnd = PrimaryNavy,
    surface = SecondaryNavy,
    textPrimary = Color.White,
    textSecondary = Color(0xFFA0AEC0),
    textTertiary = Color(0xFF718096),
    textOnPrimary = Color.White,
    textInput = Color.White,
    primary = PrimaryGreen,
    primaryDark = PrimaryNavy,
    iconGray = Color(0xFFA0AEC0),
    inputBorder = Color(0xFF2D3748),
    mediumGray = Color(0xFF4A5568),
    errorBackground = Color(0xFF3D1A2E),
    errorText = Color(0xFFFF8A9B),
    errorButton = Color(0xFFE05577),
    error = Color(0xFFFF6B81),
    successBackground = Color(0xFF132D24),
    successText = Color(0xFF6EEDB0),
    success = Color(0xFF4ADE80),
    lightGray = Color(0xFF1A202C),
    // Cores para cards de features (Dark — mais vibrantes sobre fundo escuro)
    featureBlue = Color(0xFF6B8AFF),
    featureGreen = Color(0xFF4ADE80),
    featureOrange = Color(0xFFFFBB5C),
    featureCyan = Color(0xFF22D3EE),
    featurePink = Color(0xFFF472B6),
    featureRed = Color(0xFFFF6B81),
    isDark = true
)

/**
 * CompositionLocal para acessar as cores customizadas
 * compositionLocalOf permite mudanças dinâmicas e propaga recomposição
 * ao invés de usar um objeto singleton, usamos CompositionLocal
 * para que as cores possam ser reativas e mudem conforme o tema sem precisar reiniciar a aplicação
 */
val LocalPedidosColors = compositionLocalOf { LightPedidosColors }

/**
 * DarkColorScheme - Paleta Material 3 para tema escuro (Purple/Navy)
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    secondary = Color(0xFFA0AEC0),
    tertiary = SecondaryNavy,
    background = PrimaryNavy,
    surface = SecondaryNavy,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * LightColorScheme - Paleta Material 3 para tema claro (Purple/Violet)
 */
private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryGray,
    tertiary = PrimaryNavy,
    background = PrimaryLightWhite,
    surface = SurfaceWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

/**
 * PedidosTheme - Função composable que aplica o tema à aplicação
 */
@Composable
fun PedidosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    // Material Color Scheme
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme)
                dynamicDarkColorScheme(context)
            else
                dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Suas cores customizadas
    val pedidosColors =
        if (darkTheme) DarkPedidosColors
        else LightPedidosColors

    // Controle da System UI
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Cor da Status Bar
            window.statusBarColor = pedidosColors.background.toArgb()

            // Cor da Navigation Bar
            window.navigationBarColor = pedidosColors.background.toArgb()

            val insetsController =
                WindowCompat.getInsetsController(window, view)

            // Define cor dos ícones:
            // true = ícones escuros
            // false = ícones claros
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // Aplica tema
    CompositionLocalProvider(
        LocalPedidosColors provides pedidosColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
