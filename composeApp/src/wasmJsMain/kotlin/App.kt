import LangType.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
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

const val SNAPSHOT_REPO = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

@Composable
fun App() {
    val initDarkTheme = localStorage.getItem("darkTheme")?.toBoolean() ?: isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(initDarkTheme) }
    MaterialTheme(colors = if (darkTheme) darkColors() else lightColors()) {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text("OverrunGL Modules Customizer")
                Text("v0.3.0", fontSize = 0.8.em)
            },
                actions = {
                    IconToggleButton(checked = darkTheme, onCheckedChange = {
                        darkTheme = it
                        localStorage.setItem("darkTheme", it.toString())
                    }) {
                        if (darkTheme) {
                            Icon(Icons.Filled.LightMode, contentDescription = "Switch to light theme")
                        } else {
                            Icon(Icons.Filled.DarkMode, contentDescription = "Switch to dark theme")
                        }
                    }
                })
        }) {
            val scrollState = rememberScrollState()
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(modifier = Modifier.verticalScroll(scrollState)) {
                    Customizer()
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                )
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
    val (langType, setLangType) = remember {
        mutableStateOf(
            langTypeFromString(localStorage.getItem("langType")) ?: GRADLE_KOTLIN
        )
    }
    val selectedModules = remember {
        mutableStateListOf<Binding>().also {
            val item = localStorage.getItem("selectedModules")
            if (item != null) {
                item.let { s ->
                    s.split(',').mapNotNull { m -> bindingFromString(m) }
                }.let(it::addAll)
            } else {
                it.add(Binding.CORE)
            }
        }
    }
    val release by remember { mutableStateOf(false) }
    var joml by remember { mutableStateOf(localStorage.getItem("joml").toBoolean()) }
    var noVariable by remember { mutableStateOf(localStorage.getItem("noVariable").toBoolean()) }

    fun generateCode(): String = generatedCode(
        langType = langType,
        release = release,
        modules = selectedModules,
        joml = joml,
        noVariable = noVariable,
        version = "$V_LATEST_SNAPSHOT-SNAPSHOT"
    )

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
                    Text("$V_LATEST_SNAPSHOT-SNAPSHOT")
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
                            Text(
                                text,
                                modifier = Modifier.padding(start = 14.dp),
                                fontSize = 1.2.em,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            // options
                            optionTitle("Options")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = noVariable, onCheckedChange = {
                                    noVariable = it
                                    localStorage.setItem("noVariable", it.toString())
                                })
                                Text("Do not use variables")
                            }

                            // natives
                            optionTitle("Natives")
                        }

                        Column(modifier = Modifier.padding(horizontal = 80.dp)) {
                            // addons
                            optionTitle("Addons")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = joml, onCheckedChange = {
                                    joml = it
                                    localStorage.setItem("joml", it.toString())
                                })
                                Text("JOML v$V_JOML")
                            }

                            // version
//                            optionTitle("Version")
                        }

                        Column {
                            // modules
                            optionTitle("Modules")
                            Binding.entries.forEach { module ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = if (module.nonSelectable) true else selectedModules.contains(module),
                                        enabled = !module.nonSelectable,
                                        onCheckedChange = {
                                            if (it) {
                                                selectedModules.add(module)
                                            } else {
                                                selectedModules.remove(module)
                                            }
                                            localStorage.setItem(
                                                "selectedModules",
                                                selectedModules.joinToString(separator = ",") { m -> m.name })
                                        })
                                    Text(module.moduleName)
                                }
                            }
                        }
                    }

                    // generated code
                    Column(modifier = Modifier.padding(all = 16.dp)) {
                        Row {
                            langTypeButton(GRADLE_KOTLIN, setLangType)
                            langTypeButton(GRADLE_GROOVY, setLangType)
                            langTypeButton(GRADLE_CATALOG, setLangType)
                            langTypeButton(MAVEN, setLangType)
                            langTypeButton(VM_OPTION, setLangType)
                            langTypeButton(MANIFEST_ATTRIB, setLangType)
                        }
                        Column {
                            when (langType) {
                                GRADLE_CATALOG -> {
                                    Text(
                                        "The Gradle version catalog does NOT support classifier. You have to add the native libraries by yourself.",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                VM_OPTION -> {
                                    Text("Add this VM option to allow OverrunGL to call restricted methods. You might need to add the module name of your application to it.")
                                }

                                MANIFEST_ATTRIB -> {
                                    Text("Add this attribute to META-INF/MANIFEST.MF to allow code in executable JAR files to call restricted methods.")
                                }

                                else -> {}
                            }


                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                border = ButtonDefaults.outlinedBorder
                            ) {
//                                SelectionContainer {
                                Text(
                                    generateCode(),
                                    modifier = Modifier.padding(2.dp),
                                    fontFamily = JetBrainsMono
                                )
//                                }
                            }
                        }
                        Button(onClick = {
                            window.navigator.clipboard.writeText(generateCode())
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

fun generatedCode(
    langType: LangType,
    release: Boolean,
    modules: List<Binding>,
    joml: Boolean,
    noVariable: Boolean,
    version: String
): String = buildString {
    fun StringBuilder.gradleDependencies() {
        appendLine("dependencies {")
        appendLine("    implementation(platform(\"io.github.over-run:overrungl-bom:${if (noVariable) version else "\$overrunglVersion"}\"))")
        modules.forEach {
            appendLine("    implementation(\"io.github.over-run:${it.artifactName}\")")
        }
        if (joml) {
            appendLine("    implementation(\"io.github.over-run:overrungl-joml\")")
            appendLine("    implementation(\"org.joml:joml:${if (noVariable) V_JOML else "\$jomlVersion"}\")")
        }
        append("}")
    }

    when (langType) {
        GRADLE_GROOVY -> {
            if (!noVariable) {
                appendLine("project.ext.overrunglVersion = \"$version\"")
                if (joml) {
                    appendLine("project.ext.jomlVersion = \"$V_JOML\"")
                }
                appendLine()
            }

            appendLine("repositories {")
            appendLine("    mavenCentral()")
            if (!release) {
                appendLine("    maven { url \"$SNAPSHOT_REPO\" }")
            }
            appendLine("}")
            appendLine()

            gradleDependencies()
        }

        GRADLE_KOTLIN -> {
            if (!noVariable) {
                appendLine("val overrunglVersion = \"$version\"")
                if (joml) {
                    appendLine("val jomlVersion = \"$V_JOML\"")
                }
                appendLine()
            }

            appendLine("repositories {")
            appendLine("    mavenCentral()")
            if (!release) {
                appendLine("    maven(\"$SNAPSHOT_REPO\")")
            }
            appendLine("}")
            appendLine()

            gradleDependencies()
        }

        GRADLE_CATALOG -> {
            appendLine("[versions]")
            appendLine("overrungl = \"$version\"")
            if (joml && !noVariable) {
                appendLine("joml = \"$V_JOML\"")
            }
            appendLine()

            appendLine("[libraries]")
            modules.forEach {
                appendLine("${it.catalogName} = { module = \"io.github.over-run:${it.artifactName}\", version.ref = \"overrungl\" }")
            }
            if (joml) {
                appendLine("overrungl-joml = { module = \"io.github.over-run:overrungl-joml\", version.ref = \"overrungl\" }")
                appendLine("joml = { module = \"org.joml:joml\", version${if (noVariable) " = \"$V_JOML\"" else ".ref = \"joml\""} }")
            }
            appendLine()

            appendLine("[bundles]")
            append(
                "overrungl = [${
                    buildString {
                        append(modules.joinToString { "\"${it.catalogName}\"" })
                        if (joml) {
                            append(", \"overrungl-joml\", \"joml\"")
                        }
                    }
                }]"
            )
        }

        MAVEN -> {
            if (!noVariable) {
                appendLine("<properties>")
                appendLine("    <overrungl.version>$version</overrungl.version>")
                if (joml) {
                    appendLine("    <joml.version>$V_JOML</joml.version>")
                }
                appendLine("</properties>")
                appendLine()
            }

            appendLine("<profiles>")
            appendLine("</profiles>")
            appendLine()

            if (!release) {
                appendLine(
                    """
                    <repositories>
                        <repository>
                            <id>sonatype-snapshots</id>
                            <url>$SNAPSHOT_REPO</url>
                            <releases><enabled>false</enabled></releases>
                            <snapshots><enabled>true</enabled></snapshots>
                        </repository>
                    </repositories>
                    """.trimIndent()
                )
            }

            appendLine(
                """
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.github.over-run</groupId>
                            <artifactId>overrungl-bom</artifactId>
                            <version>${if (noVariable) version else "\${overrungl.version}"}</version>
                            <scope>import</scope>
                            <type>pom</type>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                """.trimIndent()
            )

            appendLine("<dependencies>")
            modules.forEach {
                appendLine(
                    """
                   |    <dependency>
                   |        <groupId>io.github.over-run</groupId>
                   |        <artifactId>${it.artifactName}</artifactId>
                   |    </dependency>
                    """.trimMargin()
                )
            }
            if (joml) {
                appendLine(
                    """
                   |    <dependency>
                   |        <groupId>org.joml</groupId>
                   |        <artifactId>joml</artifactId>
                   |        <version>${if (noVariable) V_JOML else "\${joml.version}"}</version>
                   |    </dependency>
                """.trimMargin()
                )
            }
            append("</dependencies>")
        }

        VM_OPTION -> {
            append("--enable-native-access=io.github.overrun.marshal")
            append(modules.joinToString(separator = ",", prefix = ",") { it.javaModuleName })
        }

        MANIFEST_ATTRIB -> {
            append("Enable-Native-Access: ALL-UNNAMED")
        }
    }
}

@Composable
fun langTypeButton(langType: LangType, setLangType: (LangType) -> Unit) {
    OutlinedButton(onClick = {
        setLangType(langType)
        localStorage.setItem("langType", langType.name)
    }) {
        Text(langType.typeName)
    }
}
