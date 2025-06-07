package overrungl.gen

import overrungl.gen.Modules.*

enum class Version(val versionName: String, val modules: List<Modules>) {
    PRE_RELEASE("0.1.0-alpha.3", listOf(CORE, GLFW, NFD, OPENAL, OPENGL, STB, VMA, VULKAN)),
    SNAPSHOT("0.1.0-SNAPSHOT", listOf(CORE, GLFW, NFD, OPENAL, OPENGL, STB))
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

const val GEN_VERSION = "0.9.0"

const val JOML_VERSION = "1.10.8"
