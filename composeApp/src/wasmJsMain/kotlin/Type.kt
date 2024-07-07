import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import overrungl_gen.composeapp.generated.resources.Res
import overrungl_gen.composeapp.generated.resources.jetbrainsmono_regular

val JetBrainsMono: FontFamily
    @Composable
    get() = FontFamily(
        Font(
            resource = Res.font.jetbrainsmono_regular,
            weight = FontWeight.Normal
        )
    )
