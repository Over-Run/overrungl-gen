import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

val primaryLight = Color(0xff03a9f4)
val primaryLightVariant = Color(0xff039be5)
val secondaryLight = Color(0xffff80ab)
val secondaryLightVariant = Color(0xffff4081)
val backgroundLight = Color.White
val surfaceLight = Color.White
val errorLight = Color(0xffd32f2f)
val onPrimaryLight = Color.White
val onSecondaryLight = Color.Black
val onBackgroundLight = Color.Black
val onSurfaceLight = Color.Black
val onErrorLight = Color.White

fun lightColors() = Colors(
    primaryLight,
    primaryLightVariant,
    secondaryLight,
    secondaryLightVariant,
    backgroundLight,
    surfaceLight,
    errorLight,
    onPrimaryLight,
    onSecondaryLight,
    onBackgroundLight,
    onSurfaceLight,
    onErrorLight,
    true
)
