import csstype.string
import emotion.react.css
import io.github.overrun.application.*
import org.w3c.dom.HTMLOptionElement
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h4
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.select

external interface WelcomeProps : Props {
    var type: Type
    var language: Lang
    var version: Version
    var jomlEnabled: Boolean
}

val Welcome = FC<WelcomeProps> { props ->
    var buildType by useState(props.type)
    var language by useState(props.language)
    var version by useState(props.version)
    var jomlEnabled by useState(props.jomlEnabled)
    val chosenNatives = HashMap<Natives, StateInstance<Boolean>>().also {
        Natives.entries.forEach { n -> it[n] = useState(false) }
    }
    val chosenBindings = HashMap<Binding, StateInstance<Boolean>>().also {
        Binding.entries.forEach { b -> it[b] = useState(b == Binding.CORE) }
    }

    div {
        css {
            fontFamily = string("DejaVu Sans, Bitstream Vera Sans, Luxi Sans, Verdana, Arial, Helvetica, sans-serif")
        }
        p { +"OverrunGL Artifacts Customizer v$VER_CUSTOMIZER" }

        // Release type
        select {
            id = "type"
            option {
                value = Type.SNAPSHOT.itemName
                +Type.SNAPSHOT.itemName
            }
            value = buildType.itemName
            onChange = { event ->
                event.target.let { (it.selectedOptions.item(0) as HTMLOptionElement?)?.value }
                    ?.also { buildType = ofType(it) }
            }
        }

        // Dependencies manager
        div {
            h4 { +"Mode" }
            select {
                id = "mode"
                option {
                    value = "gradle"
                    +"Gradle"
                }
            }
        }

        // Native libraries
        div {
            h4 { +"Natives" }
            fun addNatives(natives: Natives) {
                label {
                    input {
                        type = InputType.checkbox
                        onChange = { event ->
                            chosenNatives[natives]?.also {
                                var n by it
                                n = event.target.checked
                            }
                        }
                    }
                    +natives.itemName
                }
            }
            Natives.entries.forEach(::addNatives)
        }

        // Gradle build script language
        div {
            h4 { +"Language" }
            select {
                fun addLang(lang: Lang) {
                    option {
                        value = lang.itemName
                        +lang.itemName
                    }
                }
                id = "language"
                Lang.entries.forEach(::addLang)
                value = language.itemName
                onChange = { event ->
                    event.target.let { (it.selectedOptions.item(0) as HTMLOptionElement?)?.value }
                        ?.also { language = ofLang(it) }
                }
            }
        }

        // Addons
        div {
            h4 { +"Addons" }
            label {
                input {
                    type = InputType.checkbox
                    onChange = { event ->
                        jomlEnabled = event.target.checked
                    }
                }
                +"JOML v$VER_JOML"
            }
        }

        // OverrunGL version
        div {
            h4 { +"Version" }
            select {
                fun addVer(ver: Version) {
                    option {
                        value = ver.num
                        +ver.num
                    }
                }
                id = "version"
                addVer(VER_0_1_0)
                value = version.num
                onChange = { event ->
                    event.target.let { (it.selectedOptions.item(0) as HTMLOptionElement?)?.value }
                        ?.also { version = VER_MAPPING[it]!! }
                }
            }
        }

        // Bindings
        div {
            h4 { +"Contents" }
            fun addBinding(binding: Binding) {
                if (binding in version.bindings) {
                    label {
                        input {
                            type = InputType.checkbox
                            onChange = { event ->
                                chosenBindings[binding]?.also {
                                    var b by it
                                    b = event.target.checked
                                }
                            }
                        }
                        +binding.itemName
                    }
                }
            }
            if (Binding.CORE in version.bindings)
                label {
                    input {
                        type = InputType.checkbox
                        checked = true
                        disabled = true
                    }
                    +Binding.CORE.itemName
                }
            Binding.entries.filterNot { it == Binding.CORE }.forEach(::addBinding)
        }

        fun PropsWithClassName.preCodeCss() {
            css {
                fontFamily =
                    string("DejaVu Sans Mono, JetBrains Mono, Bitstream Vera Sans Mono, Luxi Mono, Courier New, monospace")
            }
        }

        // Generated code
        pre {
            code {
                preCodeCss()

                // Gradle
                // import
                if (language == Lang.GROOVY) +"import org.gradle.internal.os.OperatingSystem\n\n"
                +"${if (language == Lang.GROOVY) "project.ext." else "val "}overrunglVersion = \"$version${if (buildType == Type.SNAPSHOT) "-SNAPSHOT" else ""}\"\n"
                if (jomlEnabled) +"${if (language == Lang.GROOVY) "project.ext." else "val "}jomlVersion = \"$VER_JOML\"\n"
                // natives
                Natives.entries.filter { n ->
                    chosenNatives[n]?.let {
                        val r by it
                        r
                    } == true
                }.also {
                    if (it.size == 1) {
                        when (language) {
                            Lang.GROOVY -> +"project.ext.overrunglNatives = \"${it[0]}\"\n"
                            Lang.KOTLIN -> +"val overrunglNatives = \"${it[0]}\"\n"
                        }
                    } else if (it.size > 1) {
                        // collect os
                        val linux = NATIVES_LINUX.filter { n -> n in it }
                        val macos = NATIVES_MACOS.filter { n -> n in it }
                        val windows = NATIVES_WIN.filter { n -> n in it }
                        when (language) {
                            Lang.GROOVY -> {
                                +"\nswitch (OperatingSystem.current()) {\n"
                                if (linux.isNotEmpty()) {
                                    +"    case OperatingSystem.LINUX:\n"
                                    if (linux.size == 1) {
                                        +"        project.ext.overrunglNatives = ${linux[0]}\n        break\n"
                                    } else if (linux.size > 1) {
                                        +"        def osArch = System.getProperty(\"os.arch\")\n        project.ext.overrunglNatives = osArch.startsWith(\"arm\") || osArch.startsWith(\"aarch64\")\n            ? \"${Natives.LINUX_64}-\${osArch.contains(\"64\") || osArch.startsWith(\"armv8\") ? \"arm64\" : \"arm32\"}\"\n            : \"${Natives.LINUX_64}\"\n        break\n"
                                    }
                                }
                                if (macos.isNotEmpty()) {
                                    +"    case OperatingSystem.MAC_OS:\n"
                                    if (macos.size == 1) {
                                        +"        project.ext.overrunglNatives = ${macos[0]}\n        break\n"
                                    } else if (macos.size > 1) {
                                        +"        project.ext.overrunglNatives = System.getProperty(\"os.arch\").startsWith(\"aarch64\") ? \"${Natives.MACOS_ARM64}\" : \"${Natives.MACOS}\"\n        break\n"
                                    }
                                }
                                if (windows.isNotEmpty()) {
                                    +"    case OperatingSystem.WINDOWS:\n"
                                    if (windows.size == 1) {
                                        +"        project.ext.overrunglNatives = ${windows[0]}\n        break\n"
                                    } else if (windows.size > 1) {
                                        +"        def osArch = System.getProperty(\"os.arch\")\n        if (osArch.contains(\"64\"))\n            project.ext.overrunglNatives = \"${Natives.WIN_64}\${sArch.startsWith(\"aarch64\") ? \"-arm64\" : \"\"}\"\n        break\n"
                                    }
                                }
                                +"}\n"
                            }

                            Lang.KOTLIN -> {
                                +"""

                                    val overrunglNatives = Pair(
                                        System.getProperty("os.name")!!,
                                        System.getProperty("os.arch")!!
                                    ).let { (name, arch) ->
                                        when {

                                """.trimIndent()
                                if (linux.isNotEmpty()) {
                                    +"        arrayOf(\"Linux\", \"FreeBSD\", \"SunOS\", \"Unit\").any { name.startsWith(it) } ->\n"
                                    if (linux.size == 1) {
                                        +"            \"${linux[0]}\"\n"
                                    } else if (linux.size > 1) {
                                        +"            if (arrayOf(\"arm\", \"aarch64\").any { arch.startsWith(it) })\n                \"${Natives.LINUX_64}\${if (arch.contains(\"64\") || arch.startsWith(\"armv8\")) \"-arm64\" else \"-arm32\"}\"\n            else \"${Natives.LINUX_64}\"\n"
                                    }
                                }
                                if (macos.isNotEmpty()) {
                                    +"        arrayOf(\"Mac OS X\", \"Darwin\").any { name.startsWith(it) } ->\n"
                                    if (macos.size == 1) {
                                        +"            \"${macos[0]}\"\n"
                                    } else if (macos.size > 1) {
                                        +"            \"${Natives.MACOS}\${if (arch.startsWith(\"aarch64\")) \"-arm64\" else \"\"}\"\n"
                                    }
                                }
                                if (windows.isNotEmpty()) {
                                    +"        arrayOf(\"Windows\").any { name.startsWith(it) } ->\n"
                                    if (windows.size == 1) {
                                        +"            \"${windows[0]}\"\n"
                                    } else if (windows.size > 1) {
                                        +"            if (arch.contains(\"64\"))\n                \"${Natives.WIN_64}\${if (arch.startsWith(\"aarch64\")) \"-arm64\" else \"\"}\"\n            else throw Error(\"Unrecognized or unsupported architecture. Please set \\\"overrunglNatives\\\" manually\")\n"
                                    }
                                }
                                +"""
                                            else -> throw Error("Unrecognized or unsupported platform. Please set \"overrunglNatives\" manually")
                                        }
                                    }

                                """.trimIndent()
                            }
                        }
                    }
                }
                // repositories
                +"""

                    repositories {
                        mavenCentral()

                """.trimIndent()
                if (buildType == Type.SNAPSHOT) {
                    when (language) {
                        Lang.GROOVY -> +"    maven { url \"https://s01.oss.sonatype.org/content/repositories/snapshots/\" }"
                        Lang.KOTLIN -> +"    maven { url = uri(\"https://s01.oss.sonatype.org/content/repositories/snapshots/\") }"
                    }
                }
                // dependencies
                +"""

                    }

                    dependencies {
                        implementation(platform("io.github.over-run:overrungl-bom:${"$"}overrunglVersion"))

                """.trimIndent()
                Binding.entries.filter { b ->
                    chosenBindings[b]?.let {
                        val r by it
                        r
                    } == true
                }.forEach {
                    +"    implementation(\"io.github.over-run:${it.artifactName}\")\n"
                    if (it.natives.isNotEmpty()) {
                        +"    runtimeOnly(\"io.github.over-run:${it.artifactName}::\$overrunglNatives\")\n"
                    }
                }
                if (jomlEnabled) {
                    +"    implementation(\"io.github.over-run:overrungl-joml\")\n"
                    +"    implementation(\"org.joml:joml:\$jomlVersion\")\n"
                }
                +"}"
            }
        }

        // VM options
        div {
            h4 { +"VM Options" }
            p { +"Add this content to your VM options. You might need to add the module name of your application to it." }
            pre {
                code {
                    preCodeCss()

                    +buildString {
                        append("--enable-native-access=")
                        Binding.entries.filter { b ->
                            chosenBindings[b]?.let {
                                val r by it
                                r
                            } == true
                        }.forEachIndexed { index, binding ->
                            if (index != 0) {
                                append(',')
                            }
                            append(binding.moduleName)
                        }
                    }
                }
            }
        }
    }
}
