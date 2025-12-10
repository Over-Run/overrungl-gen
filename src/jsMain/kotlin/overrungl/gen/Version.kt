package overrungl.gen

import overrungl.gen.Modules.*

enum class Version(val versionName: String, val modules: List<Modules>, val release: Boolean = true) {
    V0_2_0("0.2.0", listOf(CORE, GLFW, NFD, OPENAL, OPENGL, SHADERC, STB, TINYFD, VMA, VULKAN)),
    V0_1_0("0.1.0", listOf(CORE, GLFW, NFD, OPENAL, OPENGL, STB, VMA, VULKAN)),
    PRE_RELEASE("0.1.0-beta.1", listOf(CORE, GLFW, NFD, OPENAL, OPENGL, STB, VMA, VULKAN), release = false),
    SNAPSHOT("0.2.0-SNAPSHOT", listOf(CORE, GLFW, NFD, OPENAL, OPENGL, SHADERC, STB, VMA, VULKAN), release = false)
}

fun versionFromString(string: String): Version? {
    return try {
        Version.valueOf(string)
    } catch (_: Exception) {
        null
    }
}

enum class ReleaseType(
    val titleName: String,
    val description: String,
    val version: Version
) {
    RELEASE("Release", "Stable versions", Version.V0_2_0),
    PRE_RELEASE("Pre-release", "Build prepared for release", Version.PRE_RELEASE),
    SNAPSHOT("Snapshot", "Unstable build", Version.SNAPSHOT)
}

fun releaseTypeFromString(string: String): ReleaseType? {
    return try {
        ReleaseType.valueOf(string)
    } catch (_: Exception) {
        null
    }
}

const val GEN_VERSION = "0.16.0"

const val JOML_VERSION = "1.10.8"
