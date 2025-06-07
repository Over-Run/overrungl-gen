package overrungl.gen

enum class Modules(
    val titleName: String,
    val javaModuleName: String,
    val artifactName: String,
    val hasNatives: Boolean = true
) {
    CORE("OverrunGL Core", "overrungl.core", "overrungl", hasNatives = false),
    GLFW("GLFW", "overrungl.glfw", "overrungl-glfw"),
    NFD("Native File Dialog", "overrungl.nfd", "overrungl-nfd"),
    OPENAL("OpenAL", "overrungl.openal", "overrungl-openal"),
    OPENGL("OpenGL", "overrungl.opengl", "overrungl-opengl", hasNatives = false),
    STB("stb", "overrungl.stb", "overrungl-stb"),
    VMA("Vulkan Memory Allocator", "overrungl.vma", "overrungl-vma"),
    VULKAN("Vulkan", "overrungl.vulkan", "overrungl-vulkan", hasNatives = false),
}

fun modulesFromString(string: String): Modules? {
    return try {
        Modules.valueOf(string)
    } catch (_: Exception) {
        null
    }
}
