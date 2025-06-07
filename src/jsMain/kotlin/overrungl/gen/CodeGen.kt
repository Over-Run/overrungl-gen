package overrungl.gen

fun generateGradleKotlinCode(): String = buildString {
    append("""val overrunglVersion = """")
    when (releaseType) {
        ReleaseType.PRE_RELEASE -> append(Version.PRE_RELEASE.versionName)
        ReleaseType.SNAPSHOT -> append(Version.SNAPSHOT.versionName)
    }
    appendLine("\"")

    appendLine("""val overrunglOs = System.getProperty("os.name")!!.let { name ->""")
    appendLine("    when {")
    selectedNatives.map.filterValues { it }.keys.also { natives ->
        if (natives.any { it.osFamily == OsFamily.FREEBSD })
            appendLine("""        "FreeBSD" == name -> "freebsd"""")
        if (natives.any { it.osFamily == OsFamily.LINUX })
            appendLine("""        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } -> "linux"""")
        if (natives.any { it.osFamily == OsFamily.MACOS })
            appendLine("""        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } -> "macos"""")
        if (natives.any { it.osFamily == OsFamily.WINDOWS })
            appendLine("""        arrayOf("Windows").any { name.startsWith(it) } -> "windows"""")
    }
    appendLine("""        else -> throw Error("Unrecognized platform ${'$'}name. Please set \"overrunglOs\" manually")""")
    appendLine("    }")
    appendLine("}")

    appendLine("""val overrunglArch = System.getProperty("os.arch")!!.let { arch ->""")
    appendLine("    when {")
    selectedNatives.map.filterValues { it }.keys.also { natives ->
        val archesForEachOs = mutableMapOf<OsFamily, MutableList<Arch>>()
        natives.forEach {
            archesForEachOs.getOrPut(it.osFamily) { mutableListOf() }.add(it.arch)
        }
        if (natives.any { it.osFamily == OsFamily.FREEBSD })
            appendLine("""        "freebsd" -> "x64"""")
        if (natives.any { it.osFamily == OsFamily.LINUX }) {
            if (archesForEachOs[OsFamily.LINUX]?.size == 1)
                appendLine("""        "linux" -> "${archesForEachOs[OsFamily.LINUX]?.first()?.classifierName}"""")
            else {
                appendLine("""        "linux" -> if (arrayOf("arm", "aarch64").any { arch.startsWith(it) }) {""")
                appendLine("""            if (arch.contains("64") || arch.startsWith("armv8")) "arm64" else "arm32"""")
                appendLine("""        } else if (arch.startsWith("ppc")) "ppc64le"""")
                appendLine("""        else if (arch.startsWith("riscv")) "riscv64"""")
                appendLine("""        else "x64"""")
            }
        }
        if (natives.any { it.osFamily == OsFamily.MACOS })
            if (archesForEachOs[OsFamily.MACOS]?.size == 1)
                appendLine("""        "macos" -> "${archesForEachOs[OsFamily.MACOS]?.first()?.classifierName}"""")
            else
                appendLine("""        "macos" -> if (arch.startsWith("aarch64")) "arm64" else "x64"""")
        if (natives.any { it.osFamily == OsFamily.WINDOWS })
            if (archesForEachOs[OsFamily.WINDOWS]?.size == 1)
                appendLine("""        "windows" -> "${archesForEachOs[OsFamily.WINDOWS]?.first()?.classifierName}"""")
            else
                appendLine("""        "windows" -> if (arch.startsWith("aarch64")) "arm64" else "x64"""")
    }
    appendLine("""        else -> throw Error("Unrecognized architecture ${'$'}arch for platform ${'$'}overrunglOs. Please set \"overrunglArch\" manually")""")
    appendLine("    }")
    appendLine("}")
    appendLine()

    appendLine("""val overrunglNatives = "natives-${'$'}overrunglOs-${'$'}overrunglArch"""")
    if (addonJoml) {
        appendLine("""val jomlVersion = "$JOML_VERSION"""")
    }
    appendLine()

    appendLine("repositories {")
    appendLine("    mavenCentral()")
    if (releaseType == ReleaseType.SNAPSHOT)
        appendLine("""    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")""")
    appendLine("}")
    appendLine()

    appendLine("dependencies {")
    appendLine("""    implementation(platform("io.github.over-run:overrungl-bom:${'$'}overrunglVersion"))""")
    availableModules.filter { selectedModules[it] ?: false }.also { list ->
        list.forEach { m ->
            appendLine("""    implementation("io.github.over-run:${m.artifactName}")""")
        }
        list.forEach { m ->
            if (m.hasNatives)
                appendLine("""    runtimeOnly("io.github.over-run:${m.artifactName}::${'$'}overrunglNatives")""")
        }
    }
    if (addonJoml) {
        appendLine("""    implementation("io.github.over-run:overrungl-joml")""")
        appendLine("""    implementation("org.joml:joml:${'$'}jomlVersion")""")
    }
    appendLine("}")
}

fun generateGradleGroovyCode(): String = buildString {
    append("""project.ext.overrunglVersion = """")
    when (releaseType) {
        ReleaseType.PRE_RELEASE -> append(Version.PRE_RELEASE.versionName)
        ReleaseType.SNAPSHOT -> append(Version.SNAPSHOT.versionName)
    }
    appendLine("\"")
    appendLine()

    appendLine("""static String detectOverrunglOs() {""")
    appendLine("    def name = System.getProperty(\"os.name\")")
    var nativesAppended = false
    selectedNatives.map.filterValues { it }.keys.also { natives ->
        if (natives.any { it.osFamily == OsFamily.FREEBSD }) {
            appendLine("""    if (name == "FreeBSD") "freebsd"""")
            nativesAppended = true
        }
        if (natives.any { it.osFamily == OsFamily.LINUX }) {
            append("    ")
            if (nativesAppended)
                append("else ")
            appendLine("""if (name.startsWithAny("Linux", "SunOS", "Unit")) "linux"""")
            nativesAppended = true
        }
        if (natives.any { it.osFamily == OsFamily.MACOS }) {
            append("    ")
            if (nativesAppended)
                append("else ")
            appendLine("""if (name.startsWithAny("Mac OS X", "Darwin")) "macos"""")
            nativesAppended = true
        }
        if (natives.any { it.osFamily == OsFamily.WINDOWS }) {
            append("    ")
            if (nativesAppended)
                append("else ")
            appendLine("""if (name.startsWith("Windows")) "windows"""")
            nativesAppended = true
        }
    }
    append("    ")
    if (nativesAppended)
        append("else ")
    appendLine("""throw new Error("Unrecognized platform ${'$'}name. Please set \"overrunglOs\" manually")""")
    appendLine("}")
    appendLine()
    appendLine("def overrunglOs = detectOverrunglOs()")
    appendLine()

    appendLine("""static String detectOverrunglArch(String overrunglOs) {""")
    appendLine("    def arch = System.getProperty(\"os.arch\")")
    appendLine("    switch (overrunglOs) {")
    selectedNatives.map.filterValues { it }.keys.also { natives ->
        val archesForEachOs = mutableMapOf<OsFamily, MutableList<Arch>>()
        natives.forEach {
            archesForEachOs.getOrPut(it.osFamily) { mutableListOf() }.add(it.arch)
        }
        if (natives.any { it.osFamily == OsFamily.FREEBSD })
            appendLine("""        case "freebsd": return "x64"""")
        if (natives.any { it.osFamily == OsFamily.LINUX }) {
            if (archesForEachOs[OsFamily.LINUX]?.size == 1)
                appendLine("""        case "linux": return "${archesForEachOs[OsFamily.LINUX]?.first()?.classifierName}"""")
            else {
                appendLine("""        case "linux":""")
                appendLine("""            if (arch.startsWithAny("arm", "aarch64")) {""")
                appendLine("""                return arch.contains("64") || arch.startsWith("armv8") ? "arm64" : "arm32"""")
                appendLine("""            } else if (arch.startsWith("ppc")) return "ppc64le"""")
                appendLine("""            else if (arch.startsWith("riscv")) return "riscv64"""")
                appendLine("""            else return "x64"""")
            }
        }
        if (natives.any { it.osFamily == OsFamily.MACOS })
            if (archesForEachOs[OsFamily.MACOS]?.size == 1)
                appendLine("""        case "macos": return "${archesForEachOs[OsFamily.MACOS]?.first()?.classifierName}"""")
            else
                appendLine("""        case "macos": return arch.startsWith("aarch64") ? "arm64" : "x64"""")
        if (natives.any { it.osFamily == OsFamily.WINDOWS })
            if (archesForEachOs[OsFamily.WINDOWS]?.size == 1)
                appendLine("""        case "windows": return "${archesForEachOs[OsFamily.WINDOWS]?.first()?.classifierName}"""")
            else
                appendLine("""        case "windows": return arch.startsWith("aarch64") ? "arm64" : "x64"""")
    }
    appendLine("""        else -> throw Error("Unrecognized architecture ${'$'}arch for platform ${'$'}overrunglOs. Please set \"overrunglArch\" manually")""")
    appendLine("    }")
    appendLine("}")
    appendLine()
    appendLine("def overrunglArch = detectOverrunglArch(overrunglOs)")
    appendLine()

    appendLine("""def overrunglNatives = "natives-${'$'}overrunglOs-${'$'}overrunglArch"""")
    if (addonJoml) {
        appendLine("""project.ext.jomlVersion = "$JOML_VERSION"""")
    }
    appendLine()

    appendLine("repositories {")
    appendLine("    mavenCentral()")
    if (releaseType == ReleaseType.SNAPSHOT)
        appendLine("""    maven { url "https://s01.oss.sonatype.org/content/repositories/snapshots/" }""")
    appendLine("}")
    appendLine()

    appendLine("dependencies {")
    appendLine("""    implementation(platform("io.github.over-run:overrungl-bom:${'$'}overrunglVersion"))""")
    availableModules.filter { selectedModules[it] ?: false }.also { list ->
        list.forEach { m ->
            appendLine("""    implementation("io.github.over-run:${m.artifactName}")""")
        }
        list.forEach { m ->
            if (m.hasNatives)
                appendLine("""    runtimeOnly("io.github.over-run:${m.artifactName}::${'$'}overrunglNatives")""")
        }
    }
    if (addonJoml) {
        appendLine("""    implementation("io.github.over-run:overrungl-joml")""")
        appendLine("""    implementation("org.joml:joml:${'$'}jomlVersion")""")
    }
    appendLine("}")
}

fun generateGradleVMOptionCode(): String = buildString {
    append("--enable-native-access=")
    append(availableModules.filter { m -> selectedModules[m] ?: false }
        .joinToString(separator = ",") { it.javaModuleName })
}

fun generateGradleManifestCode(): String = buildString {
    append("Enable-Native-Access: ALL-UNNAMED")
}
