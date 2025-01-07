/**
 * @author squid233
 * @since 0.3.0
 */
enum class Binding(
    val moduleName: String,
    val artifactName: String,
    val javaModuleName: String,
    val nonSelectable: Boolean = false,
    val requireNative: Boolean = false
) {
    CORE("OverrunGL Core", "overrungl", "overrungl.core", nonSelectable = true),
    GLFW("GLFW", "overrungl-glfw", "overrungl.glfw", requireNative = true),
    NFD("Native File Dialog Extended", "overrungl-nfd", "overrungl.nfd", requireNative = true),
    OPENAL("OpenAL", "overrungl-openal", "overrungl.openal", requireNative = true),
    OPENGL("OpenGL", "overrungl-opengl", "overrungl.opengl"),
    STB("stb", "overrungl-stb", "overrungl.stb", requireNative = true),
}

fun bindingFromString(name: String?): Binding? = name?.let {
    try {
        Binding.valueOf(it)
    } catch (e: Exception) {
        null
    }
}
