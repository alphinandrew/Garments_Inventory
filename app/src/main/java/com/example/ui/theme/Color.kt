package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Minimal Fashion / Textile-Boutique Color Palette

// Light Scheme (Warm Ivory & Fine Linen Canvas)
val AppleSystemBackground = Color(0xFFFAF7F2)      // Warm ivory/bone background
val AppleCardSurface = Color(0xFFFFFFFF)           // Crisp pure bone/linen card surface
val AppleCardSurfaceVariant = Color(0xFFF4EFEA)    // Soft oatmeal/sand container
val AppleCardBorder = Color(0xFFE8E2D9)            // Subtle warm taupe/greige border
val AppleCardBorderSubtle = Color(0xFFF0EBE3)      // Delicate warm border

// Text Colors (Ink & Charcoal)
val AppleLabelPrimary = Color(0xFF23201E)          // Deep warm ink/charcoal (not harsh #000)
val AppleLabelSecondary = Color(0xFF706B65)        // Warm muted taupe
val AppleLabelTertiary = Color(0xFF99938B)         // Soft warm gray
val AppleLabelQuaternary = Color(0xFFC7C1B7)       // Pale warm stone

// Textile-inspired Accents (Terracotta / Rust / Forest / Sage / Rose)
val AppleBlue = Color(0xFFC05638)                  // Warm terracotta / rust for primary actions
val AppleBlueLight = Color(0xFFF9EDE8)             // Soft terracotta tint container
val AppleBlueDark = Color(0xFF9E432A)              // Deep brick terracotta

val AppleIndigo = Color(0xFF8E4A62)                // Muted berry rose / heather
val AppleIndigoLight = Color(0xFFF8ECF0)           // Soft rose tint

val AppleTeal = Color(0xFF3B6B55)                  // Deep forest textile
val AppleGreen = Color(0xFF5A7D5A)                 // Muted sage / olive
val AppleGreenLight = Color(0xFFEFF5EF)            // Soft sage tint

val AppleOrange = Color(0xFFD47A32)                // Warm amber / ochre
val AppleOrangeLight = Color(0xFFFAF0E6)           // Soft ochre tint

val AppleRed = Color(0xFFC24138)                   // Madder / brick red
val AppleRedLight = Color(0xFFFAECEB)              // Soft brick tint

val AppleDarkSlate = Color(0xFF2C2825)             // Warm rich charcoal for primary chips/buttons

// Dark Scheme (Warm Graphite & Deep Charcoal Canvas)
val AppleSystemBackgroundDark = Color(0xFF181716)  // Warm deep charcoal (not pure #000)
val AppleCardSurfaceDark = Color(0xFF242220)       // Warm dark stone / graphite
val AppleCardSurfaceVariantDark = Color(0xFF2E2B28) // Warm graphite container
val AppleCardBorderDark = Color(0xFF3E3A36)        // Soft graphite border

val AppleLabelPrimaryDark = Color(0xFFFAF7F2)      // Warm ivory text
val AppleLabelSecondaryDark = Color(0xFFA39D95)    // Warm secondary text
val AppleLabelTertiaryDark = Color(0xFF757069)
val AppleLabelQuaternaryDark = Color(0xFF4F4B46)

val AppleBlueDarkTheme = Color(0xFFD97051)         // Lighter warm terracotta for dark mode
val AppleIndigoDarkTheme = Color(0xFFA8637B)       // Soft berry rose for dark mode
val AppleGreenDarkTheme = Color(0xFF7AA57A)        // Soft sage green for dark mode
val AppleRedDarkTheme = Color(0xFFE26860)

// Specialized Category & Size Badge Tints (Minimalist & High Legibility)
val TagPantBg = Color(0xFFEFF4F3)                  // Soft spruce / denim linen
val TagPantText = Color(0xFF2C5549)                // Deep spruce
val TagPantBorder = Color(0xFFD3E2DF)

val TagHandwrittenBg = Color(0xFFFBF0EE)           // Soft madder rose
val TagHandwrittenText = Color(0xFFB34233)          // Warm madder
val TagHandwrittenBorder = Color(0xFFF4D5CF)

val TagSubStyleBg = Color(0xFFF4EFEA)              // Soft oatmeal / linen
val TagSubStyleText = Color(0xFF4A443E)
val TagSubStyleBorder = Color(0xFFE5DDD3)

val TagSizeRegularBg = Color(0xFFFAF7F2)           // Warm bone
val TagSizeRegularText = Color(0xFF23201E)
val TagSizeRegularBorder = Color(0xFFE8E2D9)

// Deterministic Fabric Swatch Palette for Categories and Filter Chips
fun getCategorySwatchColor(name: String): Color {
    val swatches = listOf(
        Color(0xFFC05638), // Terracotta
        Color(0xFF3B6B55), // Forest / Sage
        Color(0xFF8E4A62), // Muted Rose
        Color(0xFFD47A32), // Warm Amber
        Color(0xFF4A6882), // Indigo Chambray
        Color(0xFF8C7355), // Linen Camel
        Color(0xFF635670), // Dusty Heather
        Color(0xFF536E53)  // Olive Moss
    )
    val hash = kotlin.math.abs(name.hashCode())
    return swatches[hash % swatches.size]
}
