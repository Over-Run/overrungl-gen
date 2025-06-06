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

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
        localStorage[name] = toStringFun(value)
    }
}

var releaseType by LocalStored("releaseType", ReleaseType.PRE_RELEASE, ::releaseTypeFromString, ReleaseType::name)
var langType by LocalStored("langType", LangType.GRADLE_KOTLIN, ::langTypeFromString, LangType::name)
var generatedCode = generateCode()

fun generateCode(): String {
    val sb = StringBuilder()
    sb.append(
        """
            $langType
            void main() { IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); }
            void main() { IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); }
            void main() { IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); }
            void main() { IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); }
            void main() { IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); }
            void main() { IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); }
            void main() { IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); }
            void main() { IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); IO.println("Hello world"); }
        """.trimIndent()
    )
    return sb.toString()
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
        }
        h2 { +type.titleName }
        p { +type.description }
        p { +type.version }
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
            generatedCode = generateCode()
            document.getElementById("generated-code")?.textContent = generatedCode
        }
        span { +type.titleName }
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
            releaseTypeButton(ReleaseType.PRE_RELEASE)
            releaseTypeButton(ReleaseType.SNAPSHOT)
        }
        div(classes = "panel") {
            div(classes = "settings") {
                div {
                    h3 { +"Natives" }
                    form {
                        id = "form-natives"
                        checkBoxInput {
                            id = "natives-select-all"
                        }
                        label {
                            htmlFor = "natives-select-all"
                            +"Select/unselect all"
                        }
                    }
                }
                div {
                    div {
                        h3 { +"Presets" }
                        form {
                            id = "form-presets"
                            radioInput {
                                id = "presets-none"
                            }
                            label {
                                htmlFor = "presets-none"
                                +"None"
                            }
                        }
                    }
                    div {
                        h3 { +"Addons" }
                        form {
                            id = "form-addons"
                            checkBoxInput {
                                id = "addons-joml"
                            }
                            label {
                                htmlFor = "addons-joml"
                                +"JOML v$JOML_VERSION"
                            }
                        }
                    }
                }
                div {
                    h3 { +"Modules" }
                    form {
                        id = "form-modules"
                        checkBoxInput {
                            id = "modules-core"
                            checked = true
                            disabled = true
                        }
                        label {
                            htmlFor = "modules-core"
                            +"OverrunGL Core"
                        }
                    }
                }
            }
            div {
                div(classes = "lang-types") {
                    langTypeButton(LangType.GRADLE_KOTLIN)
                    langTypeButton(LangType.GRADLE_GROOVY)
                    langTypeButton(LangType.VM_OPTION)
                    langTypeButton(LangType.MANIFEST)
                }
                div {
                    pre {
                        code {
                            id = "generated-code"
                            +generatedCode
                        }
                    }
                }
            }
        }
        div(classes = "bottom-bar") {
            button(type = ButtonType.button, classes = "button copy") {
                id = "button-copy"
                onClickFunction = {
                    window.navigator.clipboard.writeText(generatedCode)
                    document.getElementById("button-copy")?.textContent = "Copied!"
                    window.setTimeout({ document.getElementById("button-copy")?.textContent = "Copy to clipboard" }, 1500)
                }
                +"Copy to clipboard"
            }
            a(classes = "button globe", href = PROJECT_LINK, target = "_blank") {
                rel = "noopener noreferrer"
                +"View project"
            }
        }
    }
}
