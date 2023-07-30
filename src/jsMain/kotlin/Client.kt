
import io.github.overrun.application.Lang
import io.github.overrun.application.Type
import io.github.overrun.application.VER_LATEST
import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    val welcome = Welcome.create {
        type = Type.SNAPSHOT
        language = Lang.KOTLIN
        version = VER_LATEST
    }
    createRoot(container).render(welcome)
}
