import Binding.*

/**
 * @author squid233
 * @since 0.3.0
 */
enum class Preset(val text: String, val modules: List<Binding>?) {
    NONE("None", listOf(CORE)),
    CUSTOM("Custom", null),
    EVERYTHING("Everything", Binding.entries),
    MINIMAL_OPENGL("Minimal OpenGL", listOf(CORE, GLFW, OPENGL, STB))
}
