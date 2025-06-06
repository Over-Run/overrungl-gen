package overrungl.gen

enum class ReleaseType(
    val titleName: String,
    val description: String,
    val version: String
) {
    PRE_RELEASE("Pre-release", "Build prepared for release", PRE_RELEASE_VER),
    SNAPSHOT("Snapshot", "Unstable build", SNAPSHOT_VER);

}

fun releaseTypeFromString(string: String): ReleaseType? = when (string) {
    "PRE_RELEASE" -> ReleaseType.PRE_RELEASE
    "SNAPSHOT" -> ReleaseType.SNAPSHOT
    else -> null
}

enum class Modules {
    CORE,
    GLFW,
    NFD,
    OPENAL,
    OPENGL,
    STB,
    VULKAN
}

const val GEN_VERSION = "0.9.0"

const val PRE_RELEASE_VER = "0.1.0-alpha.3"
const val SNAPSHOT_VER = "0.1.0-SNAPSHOT"

const val JOML_VERSION = "1.10.8"
