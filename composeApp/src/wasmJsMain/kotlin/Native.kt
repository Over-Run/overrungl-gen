/**
 * @author squid233
 * @since 0.3.0
 */
enum class Native(
    val description: String,
    val classifierName: String,
    val linux: Boolean = false,
    val macos: Boolean = false,
    val windows: Boolean = false
) {
    LINUX_X64("Linux x64", "natives-linux", linux = true),
    LINUX_ARM64("Linux arm64", "natives-linux-arm64", linux = true),
    LINUX_ARM32("Linux arm32", "natives-linux-arm32", linux = true),
    MACOS_X64("macOS x64", "natives-macos", macos = true),
    MACOS_ARM64("macOS arm64", "natives-macos-arm64", macos = true),
    WINDOWS_X64("Windows x64", "natives-windows", windows = true),
    WINDOWS_ARM64("Windows arm64", "natives-windows-arm64", windows = true),
}

fun nativeFromString(name: String?): Native? = name?.let {
    try {
        Native.valueOf(it)
    } catch (e: Exception) {
        null
    }
}
