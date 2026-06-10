package dev.fslab.pedidos.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PALETA DE CORES DA APLICAÇÃO (RanGo)
 *
 * Identidade: Verde Vibrante e Azul Marinho Profundo
 */

// Cores Primárias - Marca
val PrimaryGreen = Color(0xFF14B822)       // Verde - botões principais
val PrimaryNavy = Color(0xFF0A0E1A)        // Navy primário - fundos tema escuro
val SecondaryNavy = Color(0xFF161B2E)      // Navy secundário - cards e superfícies tema escuro
val PrimaryLightWhite = Color(0xFFFFFFFF)  // Branco - fundos e backgrounds tema claro

// Cores de Texto
val TextPrimary = Color(0xFF1A1A1A)        // Texto principal - títulos e labels
val TextSecondary = Color(0xFF6B7280)      // Texto secundário - descrições
val TextTertiary = Color(0xFF4B5563)       // Texto terciário - links e detalhes
val TextOnPrimary = Color(0xFFFFFFFF)      // Texto sobre fundo primário
val TextBlack = Color.Black                // Texto preto - campos de input

// Cores Secundárias - Cinzas (Textos e bordas)
val SecondaryGray = Color(0xFF4B5563)      // Cinza para textos secundários
val LightGray = Color(0xFFF3F4F6)          // Cinza muito claro - fundos
val MediumGray = Color(0xFF9CA3AF)         // Cinza médio - placeholder/desabilitado
val DarkGray = Color(0xFF6B7280)           // Cinza escuro - descrições
val BorderGray = Color(0xFFE5E7EB)         // Cinza para bordas
val InputBorderGray = Color(0xFFE5E7EB)    // Cinza neutro para bordas de inputs (substituindo o antigo arroxeado)
val IconGray = Color(0xFF374151)           // Cinza para ícones

// Cores de Superfície
val SurfaceWhite = Color.White             // Fundo de cards
val SurfaceLight = Color(0xFFF9FAFB)       // Fundo cinza muito claro
val BackgroundGradientStart = PrimaryLightWhite
val BackgroundGradientEnd = SurfaceLight

// Cores de Estado - Erro
val ErrorBackground = Color(0xFFFFEBEE)    // Fundo vermelho claro
val ErrorText = Color(0xFFC62828)          // Texto vermelho
val ErrorButton = Color(0xFFEF4444)        // Botão vermelho

// Cores de Estado - Sucesso
val SuccessBackground = Color(0xFFE8F5E9)  // Fundo verde claro
val SuccessText = Color(0xFF2E7D32)        // Texto verde

// Cores para Destaques e Features
val FeatureBlue = Color(0xFF3B82F6)        // Azul suave (não roxo)
val FeatureOrange = Color(0xFFF97316)      // Laranja vibrante
val FeatureGreen = Color(0xFF22C55E)       // Verde sucesso
val FeatureCyan = Color(0xFF06B6D4)        // Ciano
val FeatureRed = Color(0xFFEF4444)         // Vermelho
