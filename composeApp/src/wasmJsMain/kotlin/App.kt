import LangType.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.browser.localStorage
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

const val SNAPSHOT_REPO = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
const val WIKI_ARCH_LINK = "https://github.com/Over-Run/overrungl/wiki/Installing-Natives#supported-architectures"
const val PROJECT_TEMPLATE_LINK = "https://github.com/Over-Run/project-template"

@Composable
fun App() {
    val initDarkTheme = localStorage.getItem("darkTheme")?.toBoolean() ?: isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(initDarkTheme) }
    MaterialTheme(colors = if (darkTheme) darkColors() else lightColors()) {
        val (generatedCode, setGeneratedCode) = remember { mutableStateOf("") }

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
        }, bottomBar = {
            BottomAppBar {
                val clipboardManager = LocalClipboardManager.current
                val uriHandler = LocalUriHandler.current
                Button(onClick = {
                    clipboardManager.setText(AnnotatedString(generatedCode))
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy to clipboard")
                    Text("Copy to clipboard")
                }
                Button(onClick = {
                    uriHandler.openUri(PROJECT_TEMPLATE_LINK)
                }) {
                    Icon(Icons.Filled.OpenInBrowser, contentDescription = "Project template")
                    Text("Project template")
                }
            }
        }) {
            val scrollState = rememberScrollState()
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(modifier = Modifier.verticalScroll(scrollState)) {
                    Customizer(setGeneratedCode)
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
fun Customizer(setGeneratedCode: (String) -> Unit) {
    val JetBrainsMono = FontFamily(
        Font(FontResource("jetbrainsmono_regular.ttf"), weight = FontWeight.Normal)
    )

    var langType by remember {
        mutableStateOf(
            langTypeFromString(localStorage.getItem("langType")) ?: GRADLE_KOTLIN
        )
    }
    var selectedVersion by remember { mutableStateOf(V_LATEST_SNAPSHOT) }
    val selectedModules = remember {
        mutableStateMapOf<Binding, Boolean>().also {
            val item = localStorage.getItem("selectedModules")
            val initModules =
                item?.let { s ->
                    s.split(',').mapNotNull { m -> bindingFromString(m) }
                } ?: emptyList()
            selectedVersion.modules.forEach { m -> it[m] = initModules.contains(m) }
            it[Binding.CORE] = true
        }
    }
    val selectedNatives = remember {
        mutableStateListOf<Native>().also {
            localStorage.getItem("selectedNatives")?.let { s ->
                s.split(',').mapNotNull { n -> nativeFromString(n) }
            }?.let(it::addAll)
        }
    }
    val release by remember { mutableStateOf(false) }
    var joml by remember { mutableStateOf(localStorage.getItem("joml").toBoolean()) }
    var noVariable by remember { mutableStateOf(localStorage.getItem("noVariable").toBoolean()) }
    var selectedPreset by remember { mutableStateOf(Preset.CUSTOM) }

    fun storeSelectedNatives() {
        localStorage.setItem(
            "selectedNatives",
            selectedNatives.joinToString(separator = ",") { m -> m.name })
    }

    fun generateCode(): String = generatedCode(
        langType = langType,
        release = release,
        modules = selectedModules,
        joml = joml,
        noVariable = noVariable,
        version = selectedVersion,
        natives = selectedNatives
    )

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        // disclaimer
        /*Text(
            "Disclaimer: this customizer uses experimental features and might occur errors when any key is pressed",
            fontWeight = FontWeight.Bold
        )*/

        //region select the build type
        Row {
            Button(
                onClick = { selectedVersion = V_LATEST_SNAPSHOT },
                modifier = Modifier.padding(all = 32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Snapshot", fontSize = 2.em)
                    Text("Unstable build", fontStyle = FontStyle.Italic)
                    Text("$V_LATEST_SNAPSHOT")
                }
            }
        }
        //endregion

        Surface(
            modifier = Modifier.fillMaxSize().padding(all = 16.dp),
            border = BorderStroke(width = 2.dp, color = MaterialTheme.colors.onSurface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(all = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    @Composable
                    inline fun optionTitle(text: String) {
                        Text(
                            text,
                            fontSize = 1.2.em,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    //region Options
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TriStateCheckbox(
                                state = if (selectedNatives.isEmpty()) ToggleableState.Off
                                else if (selectedNatives.size == Native.entries.size) ToggleableState.On
                                else ToggleableState.Indeterminate,
                                onClick = {
                                    val size = selectedNatives.size
                                    selectedNatives.clear()
                                    if (size != Native.entries.size) {
                                        selectedNatives.addAll(Native.entries)
                                    }
                                    storeSelectedNatives()
                                })
                            Text("Select/unselect all")
                        }
                        Native.entries.forEach { native ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedNatives.contains(native),
                                    onCheckedChange = {
                                        if (it) {
                                            selectedNatives.add(native)
                                        } else {
                                            selectedNatives.remove(native)
                                        }
                                        storeSelectedNatives()
                                    }
                                )
                                if (native.macos) {
                                    Icon(Icons.Filled.Apple, contentDescription = null)
                                } else if (native.linux) {
                                    Icon(Icons.Filled.Linux, contentDescription = null)
                                } else if (native.windows) {
                                    Icon(Icons.Filled.Microsoft, contentDescription = null)
                                }
                                Text(native.description)
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 80.dp)) {
                        // presets
                        optionTitle("Presets")
                        Preset.entries.forEach {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = it == selectedPreset, onClick = {
                                    selectedPreset = it
                                    if (it.modules != null) {
                                        selectedVersion.modules.forEach { m ->
                                            selectedModules[m] = it.modules.contains(m)
                                        }
                                    }
                                }, enabled = it != Preset.CUSTOM)
                                Text(it.text)
                            }
                        }

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
                        selectedVersion.modules.forEach { module ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = if (module.nonSelectable) true else selectedModules[module]
                                        ?: false,
                                    enabled = !module.nonSelectable,
                                    onCheckedChange = {
                                        selectedPreset = Preset.CUSTOM
                                        selectedModules[module] = it
                                        localStorage.setItem(
                                            "selectedModules",
                                            selectedModules.filterValues { b -> b }.keys.joinToString(
                                                separator = ","
                                            ) { m -> m.name })
                                    })
                                Text(module.moduleName)
                            }
                        }
                    }
                    //endregion
                }

                val uriHandler = LocalUriHandler.current
                TextButton(onClick = { uriHandler.openUri(WIKI_ARCH_LINK) }) {
                    Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                    Text(
                        "Check supported architecture for each module here",
                        textDecoration = TextDecoration.Underline
                    )
                }

                //region generated code
                Column(modifier = Modifier.padding(bottom = 100.dp)) {
                    Row {
                        @Composable
                        fun langTypeButton(buttonLangType: LangType) {
                            OutlinedButton(
                                onClick = {
                                    langType = buttonLangType
                                    localStorage.setItem("langType", buttonLangType.name)
                                },
                                modifier = if (buttonLangType == langType) Modifier.offset(y = 8.dp) else Modifier
                            ) {
                                Text(buttonLangType.typeName)
                            }
                        }

                        langTypeButton(GRADLE_KOTLIN)
                        langTypeButton(GRADLE_GROOVY)
                        langTypeButton(GRADLE_CATALOG)
                        langTypeButton(MAVEN)
                        langTypeButton(VM_OPTION)
                        langTypeButton(MANIFEST_ATTRIB)
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        border = ButtonDefaults.outlinedBorder
                    ) {
//                                SelectionContainer {
                        val code = generateCode()
                        setGeneratedCode(code)
                        Text(
                            code,
                            modifier = Modifier.padding(all = 4.dp),
                            fontFamily = JetBrainsMono
                        )
//                                }
                    }
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
                }
                //endregion
            }
        }
    }
}

fun generatedCode(
    langType: LangType,
    release: Boolean,
    modules: Map<Binding, Boolean>,
    joml: Boolean,
    noVariable: Boolean,
    version: Version,
    natives: List<Native>,
): String = buildString {
    val selectedModules = version.modules.filter { modules[it] ?: false }
    val linuxList = natives.filter { it.linux }
    val macosList = natives.filter { it.macos }
    val windowsList = natives.filter { it.windows }

    fun StringBuilder.gradleDependencies() {
        appendLine("dependencies {")
        appendLine("""    implementation(platform("io.github.over-run:overrungl-bom:${if (noVariable) "$version" else "\$overrunglVersion"}"))""")
        selectedModules.forEach {
            appendLine("""    implementation("io.github.over-run:${it.artifactName}")""")
        }
        selectedModules.filter { it.requireNative }.forEach {
            appendLine(
                """    runtimeOnly("io.github.over-run:${it.artifactName}::${
                    if (noVariable && natives.size == 1) natives[0].classifierName
                    else "\$overrunglNatives"
                }")"""
            )
        }
        if (joml) {
            appendLine("""    implementation("io.github.over-run:overrungl-joml")""")
            appendLine("""    implementation("org.joml:joml:${if (noVariable) V_JOML else "\$jomlVersion"}")""")
        }
        append("}")
    }

    when (langType) {
        GRADLE_GROOVY -> {
            if (natives.size > 1) {
                appendLine("import org.gradle.internal.os.OperatingSystem")
                appendLine()
            }

            if (!noVariable) {
                appendLine("""project.ext.overrunglVersion = "$version"""")
            }

            if (!noVariable && natives.size == 1) {
                appendLine("""project.ext.overrunglNatives = "${natives[0].classifierName}"""")
            } else if (natives.size > 1) {
                appendLine("switch (OperatingSystem.current()) {")
                if (linuxList.isNotEmpty()) {
                    appendLine("    case OperatingSystem.LINUX:")
                    if (linuxList.size == 1) {
                        appendLine("""        project.ext.overrunglNatives = "${linuxList[0].classifierName}"""")
                    } else {
                        appendLine(
                            """
                               |        def osArch = System.getProperty("os.arch")
                               |        project.ext.overrunglNatives = osArch.startsWith("arm") || osArch.startsWith("aarch64")
                               |            ? ((osArch.contains("64") || osArch.startsWith("armv8"))
                               |                ? "${Native.LINUX_ARM64.classifierName}"
                               |                : "${Native.LINUX_ARM32.classifierName}")
                               |            : "${Native.LINUX_X64.classifierName}"
                            """.trimMargin()
                        )
                    }
                    appendLine("        break")
                }
                if (macosList.isNotEmpty()) {
                    appendLine("    case OperatingSystem.MAC_OS:")
                    if (macosList.size == 1) {
                        appendLine("""        project.ext.overrunglNatives = "${macosList[0].classifierName}"""")
                    } else {
                        appendLine(
                            """
                               |        project.ext.overrunglNatives = System.getProperty("os.arch").startsWith("aarch64") ? "${Native.MACOS_ARM64.classifierName}" : "${Native.MACOS_X64.classifierName}"
                            """.trimMargin()
                        )
                    }
                    appendLine("        break")
                }
                if (windowsList.isNotEmpty()) {
                    appendLine("    case OperatingSystem.WINDOWS:")
                    if (windowsList.size == 1) {
                        appendLine("""        project.ext.overrunglNatives = "${windowsList[0].classifierName}"""")
                    } else {
                        appendLine(
                            """
                               |        def osArch = System.getProperty("os.arch")
                               |        if (osArch.contains("64"))
                               |            project.ext.overrunglNatives = osArch.startsWith("aarch64") ? "${Native.WINDOWS_ARM64.classifierName}" : "${Native.WINDOWS_X64.classifierName}"
                            """.trimMargin()
                        )
                    }
                    appendLine("        break")
                }
                appendLine("}")
            }

            if (!noVariable && joml) {
                appendLine("""project.ext.jomlVersion = "$V_JOML"""")
            }
            if (!noVariable || natives.size > 1) {
                appendLine()
            }

            appendLine("repositories {")
            appendLine("    mavenCentral()")
            if (!release) {
                appendLine("""    maven { url "$SNAPSHOT_REPO" }""")
            }
            appendLine("}")
            appendLine()

            gradleDependencies()
        }

        GRADLE_KOTLIN -> {
            if (!noVariable) {
                appendLine("""val overrunglVersion = "$version"""")
            }

            if (!noVariable && natives.size == 1) {
                appendLine("""val overrunglNatives = "${natives[0].classifierName}"""")
            } else if (natives.size > 1) {
                appendLine(
                    """
                    val overrunglNatives = Pair(
                        System.getProperty("os.name")!!,
                        System.getProperty("os.arch")!!
                    ).let { (name, arch) ->
                        when {
                """.trimIndent()
                )
                if (linuxList.isNotEmpty()) {
                    appendLine("""        arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->""")
                    if (linuxList.size == 1) {
                        appendLine("""            "${linuxList[0].classifierName}"""")
                    } else {
                        appendLine(
                            """
                               |            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                               |                if (arch.contains("64") || arch.startsWith("armv8")) "${Native.LINUX_ARM64.classifierName}" else "${Native.LINUX_ARM32.classifierName}"
                               |            else "${Native.LINUX_X64.classifierName}"
                            """.trimMargin()
                        )
                    }
                }
                if (macosList.isNotEmpty()) {
                    appendLine("""        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->""")
                    if (macosList.size == 1) {
                        appendLine("""            "${macosList[0].classifierName}"""")
                    } else {
                        appendLine("""            if (arch.startsWith("aarch64")) "${Native.MACOS_ARM64.classifierName}" else "${Native.MACOS_X64.classifierName}"""")
                    }
                }
                if (windowsList.isNotEmpty()) {
                    appendLine("""        arrayOf("Windows").any { name.startsWith(it) } ->""")
                    if (windowsList.size == 1) {
                        appendLine("""            "${windowsList[0].classifierName}"""")
                    } else {
                        appendLine(
                            """
                               |            if (arch.contains("64"))
                               |                if (arch.startsWith("aarch64")) "${Native.WINDOWS_ARM64.classifierName}" else "${Native.WINDOWS_X64.classifierName}"
                               |            else throw Error("Unrecognized or unsupported architecture. Please set \"overrunglNatives\" manually")
                            """.trimMargin()
                        )
                    }
                }
                appendLine(
                    """
                       |        else -> throw Error("Unrecognized or unsupported platform. Please set \"overrunglNatives\" manually")
                       |    }
                       |}
                    """.trimMargin()
                )
            }

            if (!noVariable && joml) {
                appendLine("""val jomlVersion = "$V_JOML"""")
            }
            if (!noVariable || natives.size > 1) {
                appendLine()
            }

            appendLine("repositories {")
            appendLine("    mavenCentral()")
            if (!release) {
                appendLine("""    maven("$SNAPSHOT_REPO")""")
            }
            appendLine("}")
            appendLine()

            gradleDependencies()
        }

        GRADLE_CATALOG -> {
            appendLine("[versions]")
            appendLine("""overrungl = "$version"""")
            if (joml && !noVariable) {
                appendLine("""joml = "$V_JOML"""")
            }
            appendLine()

            appendLine("[libraries]")
            selectedModules.forEach {
                appendLine("""${it.catalogName} = { module = "io.github.over-run:${it.artifactName}", version.ref = "overrungl" }""")
            }
            if (joml) {
                appendLine("""overrungl-joml = { module = "io.github.over-run:overrungl-joml", version.ref = "overrungl" }""")
                appendLine("""joml = { module = "org.joml:joml", version${if (noVariable) """ = "$V_JOML"""" else """.ref = "joml""""} }""")
            }
            appendLine()

            appendLine("[bundles]")
            append(
                "overrungl = [${
                    buildString {
                        append(selectedModules.joinToString { """"${it.catalogName}"""" })
                        if (joml) {
                            append(""", "overrungl-joml", "joml"""")
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

            if (!noVariable || natives.size > 1) {
                appendLine("<profiles>")
                natives.map { it to it.os }.forEach { (native, osArr) ->
                    osArr.forEach {
                        appendLine(
                            """
                               |    <profile>
                               |        <id>${it.mavenId}</id>
                               |        <activation>
                               |            <os>
                               |                <family>${it.family}</family>
                            """.trimMargin()
                        )
                        if (it.name != null) {
                            appendLine("                <name>${it.name}</name>")
                        }
                        appendLine(
                            """
                               |                <arch>${it.arch}</arch>
                               |            </os>
                               |        </activation>
                               |        <properties>
                               |            <overrungl.natives>${native.classifierName}</overrungl.natives>
                               |        </properties>
                               |    </profile>
                            """.trimMargin()
                        )
                    }
                }
                appendLine("</profiles>")
                appendLine()
            }

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
                appendLine()
            }

            appendLine(
                """
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.github.over-run</groupId>
                            <artifactId>overrungl-bom</artifactId>
                            <version>${if (noVariable) "$version" else "\${overrungl.version}"}</version>
                            <scope>import</scope>
                            <type>pom</type>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                """.trimIndent()
            )
            appendLine()

            appendLine("<dependencies>")
            selectedModules.forEach {
                appendLine(
                    """
                       |    <dependency>
                       |        <groupId>io.github.over-run</groupId>
                       |        <artifactId>${it.artifactName}</artifactId>
                       |    </dependency>
                    """.trimMargin()
                )
            }
            selectedModules.filter { it.requireNative }.forEach {
                appendLine(
                    """
                       |    <dependency>
                       |        <groupId>io.github.over-run</groupId>
                       |        <artifactId>${it.artifactName}</artifactId>
                       |        <classifier>${if (noVariable && natives.size == 1) natives[0].classifierName else "\${overrungl.natives}"}</classifier>
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
            append(selectedModules.joinToString(separator = ",", prefix = ",") { it.javaModuleName })
        }

        MANIFEST_ATTRIB -> {
            append("Enable-Native-Access: ALL-UNNAMED")
        }
    }
}
