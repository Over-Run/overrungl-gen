/**
 * @author squid233
 * @since 0.3.0
 */
class Os(val family: String, val name: String? = null, val arch: String, mavenId: String = name ?: family) {
    val mavenId: String = "overrungl-natives-$mavenId-$arch"
}

/**
 * @author squid233
 * @since 0.3.0
 */
enum class Native(
    val description: String,
    val classifierName: String,
    vararg val os: Os,
    val linux: Boolean = false,
    val macos: Boolean = false,
    val windows: Boolean = false
) {
    LINUX_X64(
        "Linux x64",
        "natives-linux",
        Os(family = "unix", name = "linux", arch = "amd64"),
        linux = true
    ),
    LINUX_ARM64(
        "Linux arm64",
        "natives-linux-arm64",
        Os(family = "unix", name = "linux", arch = "aarch64"),
        linux = true
    ),
    LINUX_ARM32(
        "Linux arm32",
        "natives-linux-arm32",
        Os(family = "unix", name = "linux", arch = "arm"),
        Os(family = "unix", name = "linux", arch = "arm32"),
        linux = true
    ),
    MACOS_X64(
        "macOS x64",
        "natives-macos",
        Os(family = "mac", arch = "x86_64", mavenId = "macos"),
        macos = true
    ),
    MACOS_ARM64(
        "macOS arm64",
        "natives-macos-arm64",
        Os(family = "mac", arch = "aarch64", mavenId = "macos"),
        macos = true
    ),
    WINDOWS_X64(
        "Windows x64",
        "natives-windows",
        Os(family = "windows", arch = "amd64"),
        windows = true
    ),
    WINDOWS_ARM64(
        "Windows arm64",
        "natives-windows-arm64",
        Os(family = "windows", arch = "aarch64"),
        windows = true
    ),
}

fun nativeFromString(name: String?): Native? = name?.let {
    try {
        Native.valueOf(it)
    } catch (e: Exception) {
        null
    }
}
