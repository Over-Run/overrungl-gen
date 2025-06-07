package overrungl.gen

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.reflect.KProperty

const val PROJECT_LINK = "https://github.com/Over-Run/overrungl"

class LocalStored<T>(
    val name: String,
    defaultValue: T,
    fromStringFun: (String) -> T?,
    val toStringFun: (T) -> String
) {
    var value: T = localStorage[name]?.let { fromStringFun(it) } ?: defaultValue.also {
        localStorage[name] = toStringFun(it)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
        localStorage[name] = toStringFun(value)
    }
}

class LocalStoredMap<K, V>(
    val name: String,
    fromStringFun: (String) -> MutableMap<K, V>?,
    val toStringFun: (MutableMap<K, V>) -> String
) {
    val map: MutableMap<K, V> = localStorage[name]?.let { fromStringFun(it) } ?: mutableMapOf<K, V>().also {
        localStorage[name] = toStringFun(it)
    }

    operator fun get(key: K): V? = map[key]
    operator fun set(key: K, value: V) {
        map[key] = value
        writeToLS()
    }

    fun writeToLS() {
        localStorage[name] = toStringFun(map)
    }
}

var releaseType by LocalStored("releaseType", ReleaseType.PRE_RELEASE, ::releaseTypeFromString, ReleaseType::name)
var langType by LocalStored("langType", LangType.GRADLE_KOTLIN, ::langTypeFromString, LangType::name)
val selectedNatives = LocalStoredMap("selectedNatives", { ls ->
    return@LocalStoredMap mutableMapOf<Natives, Boolean>().also { map ->
        ls.split(',').forEach {
            nativesFromString(it)?.also { n -> map[n] = true }
        }
    }
}, {
    it.filter { (key, value) -> value }.keys.joinToString(separator = ",")
})
val selectedModules = LocalStoredMap("selectedModules", { ls ->
    return@LocalStoredMap mutableMapOf<Modules, Boolean>().also { map ->
        ls.split(',').forEach {
            modulesFromString(it)?.also { m -> map[m] = true }
        }
    }
}, {
    it.filter { (key, value) -> value }.keys.joinToString(separator = ",")
})
var addonJoml by LocalStored("joml", false, String::toBoolean, Boolean::toString)
val availableModules = mutableListOf<Modules>()
var generatedCode = generateCode()

fun generateCode(): String {
    return when (langType) {
        LangType.GRADLE_KOTLIN -> generateGradleKotlinCode()
        LangType.GRADLE_GROOVY -> generateGradleGroovyCode()
        LangType.VM_OPTION -> generateGradleVMOptionCode()
        LangType.MANIFEST -> generateGradleManifestCode()
    }
}

fun updateGeneratedCode() {
    generatedCode = generateCode()
    document.getElementById("generated-code")?.textContent = generatedCode
    document.getElementById("generated-code-notice")?.textContent =
        when (langType) {
            LangType.VM_OPTION -> "Add this VM options to allow OverrunGL to invoke restricted methods. You might need to add the module name of your application."
            LangType.MANIFEST -> "Add this attribute to META-INF/MANIFEST.MF to allow code in executable JAR files to invoke restricted methods."
            else -> ""
        }
}

@HtmlTagMarker
fun DIV.releaseTypeButton(type: ReleaseType) {
    div {
        classes = buildSet {
            add("release-type")
            if (type == releaseType) {
                add("active")
            }
        }
        id = "button-release-type-${type.name.lowercase()}"
        onClickFunction = {
            releaseType = type
            ReleaseType.entries.forEach {
                val element = document.getElementById("button-release-type-${it.name.lowercase()}")
                if (it.name == type.name) {
                    element?.addClass("active")
                } else {
                    element?.removeClass("active")
                }
            }
            updateAvailableModules()
            updateGeneratedCode()
        }
        h2 { +type.titleName }
        p { +type.description }
        p { +type.version.versionName }
    }
}

fun updateAvailableModules() {
    document.getElementById("form-modules")?.remove()
    availableModules.clear()
    when (releaseType) {
        ReleaseType.PRE_RELEASE -> availableModules.addAll(Version.PRE_RELEASE.modules)
        ReleaseType.SNAPSHOT -> availableModules.addAll(Version.SNAPSHOT.modules)
    }
    document.getElementById("container-form-modules")?.append {
        form {
            id = "form-modules"
            availableModules.forEach(::modulesCheckbox)
        }
    }
}

@HtmlTagMarker
fun DIV.langTypeButton(type: LangType) {
    div {
        classes = buildSet {
            add("lang-type")
            if (type == langType) {
                add("active")
            }
        }
        id = "button-lang-type-${type.name.lowercase()}"
        onClickFunction = {
            langType = type
            LangType.entries.forEach {
                val element = document.getElementById("button-lang-type-${it.name.lowercase()}")
                if (it.name == type.name) {
                    element?.addClass("active")
                } else {
                    element?.removeClass("active")
                }
            }
            updateGeneratedCode()
        }
        span { +type.titleName }
    }
}

@HtmlTagMarker
fun FORM.nativesCheckbox(natives: Natives) {
    div(classes = "row") {
        checkBoxInput(classes = "icon") {
            id = "natives-checkbox-${natives.name.lowercase()}"
            checked = selectedNatives[natives] ?: false
            onClickFunction = {
                (selectedNatives[natives] ?: false).also { selectedNatives[natives] = !it }
                updateNativesSelectAll()
                updateGeneratedCode()
            }
        }
        label {
            htmlFor = "natives-checkbox-${natives.name.lowercase()}"
            +"${natives.osFamily.titleName} ${natives.arch.classifierName}"
        }
    }
}

fun updateNativesSelectAll() {
    val element = document.getElementById("natives-checkbox-select-all") as HTMLInputElement?
    if (Natives.entries.all { n -> selectedNatives[n] ?: false }) {
        element?.checked = true
        element?.indeterminate = false
    } else if (Natives.entries.none { n -> selectedNatives[n] ?: false }) {
        element?.checked = false
        element?.indeterminate = false
    } else {
        element?.checked = true
        element?.indeterminate = true
    }
}

@HtmlTagMarker
fun FIELDSET.presetsRadio(presets: Presets) {
    div(classes = "row") {
        radioInput(classes = "icon") {
            id = "presets-radio-${presets.name.lowercase()}"
            name = "presets"
            disabled = presets == Presets.CUSTOM
            checked = presets == Presets.CUSTOM
            onClickFunction = {
                selectedModules.map.clear()
                presets.modules.forEach { m -> selectedModules.map[m] = true }
                selectedModules.writeToLS()
                availableModules.forEach { m ->
                    (document.getElementById("modules-checkbox-${m.name.lowercase()}") as HTMLInputElement?)
                        ?.checked = selectedModules[m] ?: false
                }
                updateGeneratedCode()
            }
        }
        label {
            htmlFor = "presets-radio-${presets.name.lowercase()}"
            +presets.titleName
        }
    }
}

@HtmlTagMarker
fun FORM.modulesCheckbox(modules: Modules) {
    div(classes = "row") {
        checkBoxInput(classes = "icon") {
            id = "modules-checkbox-${modules.name.lowercase()}"
            checked = selectedModules[modules] ?: false
            disabled = modules == Modules.CORE
            onClickFunction = {
                (selectedModules[modules] ?: false).also { selectedModules[modules] = !it }
                (document.getElementById("presets-radio-custom") as HTMLInputElement?)
                    ?.checked = true
                updateGeneratedCode()
            }
        }
        label {
            htmlFor = "modules-checkbox-${modules.name.lowercase()}"
            +modules.titleName
        }
    }
}

fun main() {
    document.body!!.append.div(classes = "container") {
        div(classes = "top-bar") {
            span {
                style = "font-size: 1.2em; margin-left: 10px;"
                +"OverrunGL Modules Customizer"
            }
            span {
                style = "font-size: 0.7em;"
                +"v$GEN_VERSION"
            }
        }
        div(classes = "release-types") {
            ReleaseType.entries.forEach(::releaseTypeButton)
        }
        div(classes = "panel") {
            div(classes = "settings") {
                div {
                    h3 { +"Natives" }
                    form {
                        id = "form-natives"
                        div(classes = "row") {
                            checkBoxInput(classes = "icon") {
                                id = "natives-checkbox-select-all"
                                onClickFunction = {
                                    val inverse =
                                        (document.getElementById("natives-checkbox-select-all") as HTMLInputElement?)
                                            ?.checked ?: false
                                    Natives.entries.forEach { n ->
                                        selectedNatives[n] = inverse
                                        (document.getElementById("natives-checkbox-${n.name.lowercase()}") as HTMLInputElement?)
                                            ?.checked = inverse
                                    }
                                    updateGeneratedCode()
                                }
                            }
                            label {
                                htmlFor = "natives-checkbox-select-all"
                                +"Select/unselect all"
                            }
                        }
                        Natives.entries.forEach(::nativesCheckbox)
                    }
                }
                div {
                    div {
                        form {
                            id = "form-presets"
                            fieldSet {
                                legend {
                                    h3 { +"Presets" }
                                }
                                Presets.entries.forEach(::presetsRadio)
                            }
                        }
                    }
                    div {
                        h3 { +"Addons" }
                        form {
                            id = "form-addons"
                            div(classes = "row") {
                                checkBoxInput(classes = "icon") {
                                    id = "addons-joml"
                                    checked = addonJoml
                                    onClickFunction = {
                                        addonJoml = !addonJoml
                                        updateGeneratedCode()
                                    }
                                }
                                label {
                                    htmlFor = "addons-joml"
                                    +"JOML v$JOML_VERSION"
                                }
                            }
                        }
                    }
                }
                div {
                    id = "container-form-modules"
                    h3 { +"Modules" }
                    form {
                        id = "form-modules"
                    }
                }
            }
            div {
                div(classes = "lang-types") {
                    LangType.entries.forEach(::langTypeButton)
                }
                div {
                    pre {
                        code {
                            id = "generated-code"
                        }
                    }
                }
                div {
                    id = "generated-code-notice"
                }
            }
        }
        div(classes = "bottom-bar") {
            button(type = ButtonType.button, classes = "button icon copy") {
                id = "button-copy"
                onClickFunction = {
                    window.navigator.clipboard.writeText(generatedCode)
                    document.getElementById("button-copy")?.also {
                        it.textContent = "Copied!"
                        it.removeClass("copy")
                        it.addClass("copied")
                    }
                    window.setTimeout(
                        {
                            document.getElementById("button-copy")?.also {
                                it.textContent = "Copy to clipboard"
                                it.removeClass("copied")
                                it.addClass("copy")
                            }
                        },
                        1500
                    )
                }
                +"Copy to clipboard"
            }
            a(classes = "button icon globe", href = PROJECT_LINK, target = "_blank") {
                rel = "noopener noreferrer"
                +"View project"
            }
        }
    }

    updateNativesSelectAll()
    updateAvailableModules()
    updateGeneratedCode()
}
