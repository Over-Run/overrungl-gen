import LangType.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.browser.localStorage
import org.jetbrains.compose.resources.painterResource
import overrungl_gen.composeapp.generated.resources.*
import kotlin.reflect.KProperty

const val SNAPSHOT_REPO = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
const val WIKI_ARCH_LINK = "https://github.com/Over-Run/overrungl/wiki/Installing-Natives#supported-architectures"
const val PROJECT_LINK = "https://github.com/Over-Run/overrungl"

value class StorageItem(val name: String) {
    operator fun getValue(thisRef: Nothing?, property: KProperty<*>): String? =
        localStorage.getItem(name)

    operator fun setValue(thisRef: Nothing?, property: KProperty<*>, value: String?) {
        if (value != null) {
            localStorage.setItem(name, value)
        } else {
            localStorage.removeItem(name)
        }
    }
}

@Composable
fun App() {
    MaterialTheme(colors = lightColors()) {
        val (generatedCode, setGeneratedCode) = remember { mutableStateOf("") }

        Scaffold(topBar = {
            TopAppBar(title = {
                Text("OverrunGL Modules Customizer")
                Text("v$V_APP", fontSize = 0.8.em)
            })
        }, bottomBar = {
            BottomAppBar {
                val clipboardManager = LocalClipboardManager.current
                val uriHandler = LocalUriHandler.current
                Button(onClick = {
                    clipboardManager.setText(AnnotatedString(generatedCode))
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null)
                    Text("Copy to clipboard")
                }
                Button(onClick = {
                    uriHandler.openUri(PROJECT_LINK)
                }) {
                    Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                    Text("View project")
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

@Composable
fun Customizer(setGeneratedCode: (String) -> Unit) {
    var langTypeStorage by StorageItem("langType")
    var selectedModulesStorage by StorageItem("selectedModules")
    var selectedNativesStorage by StorageItem("selectedNatives")
    var jomlStorage by StorageItem("joml")

    var langType by remember {
        mutableStateOf(
            langTypeFromString(langTypeStorage) ?: GRADLE_KOTLIN
        )
    }
    var buildTypeStorage by StorageItem("buildType")
    var rememberBuildType by remember { mutableStateOf(buildTypeStorage) }
    var selectedVersion by remember { mutableStateOf(V_LATEST_PRERELEASE) }
    val selectedModules = remember {
        mutableStateMapOf<Binding, Boolean>().also {
            val initModules =
                selectedModulesStorage?.let { s ->
                    s.split(',').mapNotNull { m -> bindingFromString(m) }
                } ?: emptyList()
            selectedVersion.modules.forEach { m -> it[m] = initModules.contains(m) }
            it[Binding.CORE] = true
        }
    }
    val selectedNatives = remember {
        mutableStateListOf<Native>().also {
            selectedNativesStorage?.let { s ->
                s.split(',').mapNotNull { n -> nativeFromString(n) }
            }?.let(it::addAll)
        }
    }
    var snapshot by remember { mutableStateOf(false) }
    var joml by remember { mutableStateOf(jomlStorage.toBoolean()) }
    var selectedPreset by remember { mutableStateOf(Preset.CUSTOM) }

    fun storeSelectedNatives() {
        selectedNativesStorage = selectedNatives.joinToString(separator = ",") { m -> m.name }
    }

    fun generateCode(): String = generatedCode(
        langType = langType,
        snapshot = snapshot,
        modules = selectedModules,
        joml = joml,
        version = selectedVersion,
        natives = selectedNatives
    )

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        //region select the build type
        Row {
            @Composable
            fun buildType(
                buildType: String,
                selectVersion: Version,
                title: String,
                description: String,
                versionName: Boolean
            ) {
                Button(
                    onClick = {
                        rememberBuildType = buildType
                        buildTypeStorage = buildType
                        selectedVersion = selectVersion
                        snapshot = buildType == V_TYPE_SNAPSHOT
                    },
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 16.dp, bottom = 16.dp)
                        .offset(y = if (rememberBuildType == buildType) 16.dp else 0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(title, fontSize = 2.em)
                        Text(description, fontStyle = FontStyle.Italic)
                        if (versionName) {
                            Text("$selectVersion")
                        }
                    }
                }
            }
            buildType(V_TYPE_PRERELEASE, V_LATEST_PRERELEASE, "Pre-release", "Build prepared for release", true)
            buildType(V_TYPE_SNAPSHOT, V_LATEST_SNAPSHOT, "Snapshot", "Unstable build", true)
        }
        //endregion

        Surface(
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            border = BorderStroke(width = 2.dp, color = MaterialTheme.colors.onSurface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(all = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    @Composable
                    fun optionTitle(text: String) {
                        Text(
                            text,
                            fontSize = 1.2.em,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    //region Options
                    Column {
                        // options
//                        optionTitle("Options")

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
                                Icon(
                                    painterResource(
                                        when (native.nativeOs) {
                                            NativeOs.FREEBSD -> Res.drawable.freebsd
                                            NativeOs.LINUX -> Res.drawable.linux
                                            NativeOs.MACOS -> Res.drawable.apple
                                            NativeOs.WINDOWS -> Res.drawable.microsoft
                                        }
                                    ),
                                    contentDescription = null
                                )
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
                                        selectedModulesStorage = selectedModules
                                            .filterValues { b -> b }.keys.joinToString(
                                                separator = ","
                                            ) { m -> m.name }
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
                                jomlStorage = it.toString()
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
                                        selectedModulesStorage = selectedModules
                                            .filterValues { b -> b }.keys.joinToString(
                                                separator = ","
                                            ) { m -> m.name }
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
                        "Check supported architecture for each module",
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
                                    langTypeStorage = buttonLangType.name
                                },
                                modifier = if (buttonLangType == langType) Modifier.offset(y = 8.dp) else Modifier
                            ) {
                                Text(buttonLangType.typeName)
                            }
                        }

                        langTypeButton(GRADLE_KOTLIN)
                        langTypeButton(GRADLE_GROOVY)
                        langTypeButton(VM_OPTION)
                        langTypeButton(MANIFEST_ATTRIB)
                    }
                    SelectionContainer(
                        modifier = Modifier.fillMaxWidth().border(ButtonDefaults.outlinedBorder)
                    ) {
                        val code = generateCode()
                        setGeneratedCode(code)
                        Text(
                            code,
                            modifier = Modifier.padding(all = 4.dp),
                            fontFamily = JetBrainsMono
                        )
                    }
                    when (langType) {
                        VM_OPTION -> {
                            Text("Add this VM option to allow OverrunGL to invoke restricted methods. You might need to add the module name of your application to it.")
                        }

                        MANIFEST_ATTRIB -> {
                            Text("Add this attribute to META-INF/MANIFEST.MF to allow code in executable JAR files to invoke restricted methods.")
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
    snapshot: Boolean,
    modules: Map<Binding, Boolean>,
    joml: Boolean,
    version: Version,
    natives: List<Native>,
): String = buildString {
    val selectedModules = version.modules.filter { modules[it] ?: false }
    val nativeMap = natives.groupBy { it.nativeOs }
    val selectedArchCount = natives.distinctBy { it.nativeArch }.size

    fun StringBuilder.gradleDependencies() {
        appendLine("dependencies {")
        appendLine("""    implementation(platform("io.github.over-run:overrungl-bom:${'$'}overrunglVersion"))""")
        selectedModules.forEach {
            appendLine("""    implementation("io.github.over-run:${it.artifactName}")""")
        }
        selectedModules.forEach {
            if (it.requireNative) {
                appendLine("""    runtimeOnly("io.github.over-run:${it.artifactName}::${'$'}overrunglNatives")""")
            }
        }
        if (joml) {
            appendLine("""    implementation("io.github.over-run:overrungl-joml")""")
            appendLine("""    implementation("org.joml:joml:${'$'}jomlVersion")""")
        }
        append("}")
    }

    when (langType) {
        GRADLE_GROOVY -> {
            appendLine("""project.ext.overrunglVersion = "$version"""")

            if (nativeMap.size == 1) {
                appendLine("""def overrunglOs = "${natives[0].nativeOs.attribValue}"""")
            } else if (nativeMap.size > 1) {
                appendLine(
                    """
                    static String detectOverrunglOs() {
                        def name = System.getProperty("os.name")
                """.trimIndent()
                )
                if (NativeOs.FREEBSD in nativeMap) {
                    appendLine("""    if (name == "FreeBSD") "freebsd"""")
                }
                if (NativeOs.LINUX in nativeMap) {
                    appendLine("""    else if (name.startsWithAny("Linux", "SunOS", "Unit")) "linux"""")
                }
                if (NativeOs.MACOS in nativeMap) {
                    appendLine("""    else if (name.startsWithAny("Mac OS X", "Darwin")) "macos"""")
                }
                if (NativeOs.WINDOWS in nativeMap) {
                    appendLine("""    else if (name.startsWith("Windows")) "windows"""")
                }
                appendLine(
                    """
                        else throw new Error("Unrecognized platform ${'$'}name. Please set \"overrunglOs\" manually")
                    }
                """.trimIndent()
                )
                appendLine()

                appendLine("def overrunglOs = detectOverrunglOs()")
                appendLine()
            }

            if (selectedArchCount == 1) {
                appendLine("""def overrunglArch = "${natives[0].nativeArch.attribValue}"""")
            } else if (selectedArchCount > 1) {
                appendLine(
                    """
                    static String detectOverrunglArch(String overrunglOs) {
                        def arch = System.getProperty("os.arch")
                        switch (overrunglOs) {
                """.trimIndent()
                )
                if (NativeOs.FREEBSD in nativeMap) {
                    appendLine("""        case "freebsd": return "x64"""")
                }
                nativeMap[NativeOs.LINUX]?.also {
                    if (it.size == 1) {
                        appendLine("""        case "linux": return "${it[0].nativeArch.attribValue}"""")
                    } else {
                        appendLine(
                            """
                                |        case "linux":
                                |            if (arch.startsWithAny("arm", "aarch64")) {
                                |                return arch.contains("64") || arch.startsWith("armv8") ? "arm64" : "arm32"
                                |            } else if (arch.startsWith("ppc")) return "ppc64le"
                                |            else if (arch.startsWith("riscv")) return "riscv64"
                                |            else return "x64"
                            """.trimMargin()
                        )
                    }
                }
                nativeMap[NativeOs.MACOS]?.also {
                    if (it.size == 1) {
                        appendLine("""        case "macos": return "${it[0].nativeArch.attribValue}"""")
                    } else {
                        appendLine("""        case "macos": return arch.startsWith("aarch64") ? "arm64" : "x64"""")
                    }
                }
                nativeMap[NativeOs.WINDOWS]?.also {
                    if (it.size == 1) {
                        appendLine("""        case "windows": return "${it[0].nativeArch.attribValue}"""")
                    } else {
                        appendLine("""        case "windows": return arch.startsWith("aarch64") ? "arm64" : "x64"""")
                    }
                }
                appendLine(
                    """
                            default: throw new Error("Unrecognized platform ${'$'}overrunglOs. Please set \"overrunglArch\" manually")
                        }
                    }
                """.trimIndent()
                )
                appendLine()

                appendLine("def overrunglArch = detectOverrunglArch(overrunglOs)")
                appendLine()
            }

            appendLine("""def overrunglNatives = "natives-${'$'}overrunglOs-${'$'}overrunglArch"""")

            if (joml) {
                appendLine("""project.ext.jomlVersion = "$V_JOML"""")
            }
            appendLine()

            appendLine("repositories {")
            appendLine("    mavenCentral()")
            if (snapshot) {
                appendLine("""    maven { url "$SNAPSHOT_REPO" }""")
            }
            appendLine("}")
            appendLine()

            gradleDependencies()
        }

        GRADLE_KOTLIN -> {
            appendLine("""val overrunglVersion = "$version"""")

            if (nativeMap.size == 1) {
                appendLine("""val overrunglOs = "${natives[0].nativeOs.attribValue}"""")
            } else if (nativeMap.size > 1) {
                appendLine(
                    """
                    val overrunglOs = System.getProperty("os.name")!!.let { name ->
                        when {
                """.trimIndent()
                )
                if (NativeOs.FREEBSD in nativeMap) {
                    appendLine("""        "FreeBSD" == name -> "freebsd"""")
                }
                if (NativeOs.LINUX in nativeMap) {
                    appendLine("""        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } -> "linux"""")
                }
                if (NativeOs.MACOS in nativeMap) {
                    appendLine("""        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } -> "macos"""")
                }
                if (NativeOs.WINDOWS in nativeMap) {
                    appendLine("""        arrayOf("Windows").any { name.startsWith(it) } -> "windows"""")
                }
                appendLine(
                    """
                            else -> throw Error("Unrecognized platform ${'$'}name. Please set \"overrunglOs\" manually")
                        }
                    }
                """.trimIndent()
                )
            }

            if (selectedArchCount == 1) {
                appendLine("""val overrunglArch = "${natives[0].nativeArch.attribValue}"""")
            } else if (selectedArchCount > 1) {
                appendLine(
                    """
                    val overrunglArch = System.getProperty("os.arch")!!.let { arch ->
                        when (overrunglOs) {
                """.trimIndent()
                )
                if (NativeOs.FREEBSD in nativeMap) {
                    appendLine("""        "freebsd" -> "x64"""")
                }
                nativeMap[NativeOs.LINUX]?.also {
                    if (it.size == 1) {
                        appendLine("""        "linux" -> "${it[0].nativeArch.attribValue}"""")
                    } else {
                        appendLine(
                            """
                            |        "linux" -> if (arrayOf("arm", "aarch64").any { arch.startsWith(it) }) {
                            |            if (arch.contains("64") || arch.startsWith("armv8")) "arm64" else "arm32"
                            |        } else if (arch.startsWith("ppc")) "ppc64le"
                            |        else if (arch.startsWith("riscv")) "riscv64"
                            |        else "x64"
                        """.trimMargin()
                        )
                    }
                }
                nativeMap[NativeOs.MACOS]?.also {
                    if (it.size == 1) {
                        appendLine("""        "macos" -> "${it[0].nativeArch.attribValue}"""")
                    } else {
                        appendLine("""        "macos" -> if (arch.startsWith("aarch64")) "arm64" else "x64"""")
                    }
                }
                nativeMap[NativeOs.WINDOWS]?.also {
                    if (it.size == 1) {
                        appendLine("""        "windows" -> "${it[0].nativeArch.attribValue}"""")
                    } else {
                        appendLine("""        "windows" -> if (arch.startsWith("aarch64")) "arm64" else "x64"""")
                    }
                }
                appendLine(
                    """
                            else -> throw Error("Unrecognized platform ${'$'}overrunglOs. Please set \"overrunglArch\" manually")
                        }
                    }
                """.trimIndent()
                )

                appendLine()
            }

            appendLine("""val overrunglNatives = "natives-${'$'}overrunglOs-${'$'}overrunglArch"""")

            if (joml) {
                appendLine("""val jomlVersion = "$V_JOML"""")
                appendLine()
            }

            appendLine("repositories {")
            appendLine("    mavenCentral()")
            if (snapshot) {
                appendLine("""    maven("$SNAPSHOT_REPO")""")
            }
            appendLine("}")
            appendLine()

            gradleDependencies()
        }

        VM_OPTION -> {
            append("--enable-native-access=")
            append(selectedModules.joinToString(separator = ",") { it.javaModuleName })
        }

        MANIFEST_ATTRIB -> {
            append("Enable-Native-Access: ALL-UNNAMED")
        }
    }
}
