package overrungl.gen

enum class Presets(val titleName: String, val modules: List<Modules>) {
    NONE("None", listOf(Modules.CORE)),
    CUSTOM("Custom", listOf()),
    EVERYTHING("Everything", Modules.entries),
    MINIMAL_OPENGL("Minimal OpenGL", listOf(Modules.CORE, Modules.GLFW, Modules.OPENAL, Modules.OPENGL, Modules.STB)),
    MINIMAL_VULKAN("Minimal Vulkan", listOf(Modules.CORE, Modules.GLFW, Modules.OPENAL, Modules.STB, Modules.VULKAN)),
}
