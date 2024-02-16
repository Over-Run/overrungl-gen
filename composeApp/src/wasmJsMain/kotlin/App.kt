import LangType.GRADLE_GROOVY
import LangType.GRADLE_KOTLIN
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

@Composable
fun App() {
    MaterialTheme {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text("OverrunGL Modules Customizer")
                Text("v0.3.0", fontSize = 0.8.em)
            })
        }) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Customizer()
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun Customizer() {
    val JetBrainsMono = FontFamily(
        Font(FontResource("jetbrainsmono_regular.ttf"), weight = FontWeight.Normal)
    )
    var langType by remember {
        mutableStateOf(
            langTypeFromString(localStorage.getItem("langType")) ?: GRADLE_KOTLIN
        )
    }
    val generatedCode = remember {
        when (langType) {
            GRADLE_GROOVY -> generateGroovy()
            GRADLE_KOTLIN -> generateKotlin()
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        // disclaimer
        Text(
            "Disclaimer: this customizer uses experimental features and might occur errors when any key is pressed",
            fontWeight = FontWeight.Bold
        )

        // select the build type
        Row {
            Button(onClick = {}, modifier = Modifier.padding(all = 32.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Snapshot", fontSize = 2.em)
                    Text("Unstable build", fontStyle = FontStyle.Italic)
                    Text("$V_LATEST-SNAPSHOT")
                }
            }
        }

        Surface(modifier = Modifier.fillMaxSize().padding(all = 16.dp)) {
            // inside
            Surface(
                modifier = Modifier.fillMaxSize(),
                border = BorderStroke(width = 2.dp, color = MaterialTheme.colors.onSurface)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(all = 16.dp)) {
                        @Composable
                        inline fun optionTitle(text: String) {
                            Text(text, fontSize = 1.2.em, fontWeight = FontWeight.Bold)
                        }

                        Column {
                            // natives
                            optionTitle("Natives")
                        }

                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                            // addons
                            optionTitle("Addons")

                            // version
                            optionTitle("Version")
                        }

                        Column {
                            // modules
                            optionTitle("Modules")
                        }
                    }

                    // generated code
                    Column(modifier = Modifier.padding(all = 16.dp)) {
                        Row {
                            langTypeButton(GRADLE_GROOVY) { langType = it }
                            langTypeButton(GRADLE_KOTLIN) { langType = it }
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            border = ButtonDefaults.outlinedBorder
                        ) {
//                        SelectionContainer {
                            Text(
                                generatedCode, modifier = Modifier.padding(2.dp), fontFamily = JetBrainsMono
                            )
//                        }
                        }
                        Button(onClick = {
                            window.navigator.clipboard.writeText(generatedCode)
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Text("Copy to clipboard")
                        }
                    }
                }

            }
        }
    }
}

fun generateGroovy(): String = buildString {
    appendLine("dependencies {")
    append("}")
}

fun generateKotlin(): String = buildString {
    appendLine("dependencies {")
    append("}")
}

@Composable
fun langTypeButton(langType: LangType, onClick: (LangType) -> Unit) {
    OutlinedButton(onClick = {
        onClick(langType)
        localStorage.setItem("langType", langType.name)
    }) {
        Text(langType.typeName)
    }
}
