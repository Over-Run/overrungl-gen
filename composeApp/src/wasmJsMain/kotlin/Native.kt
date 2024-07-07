enum class NativeOs(val attribValue: String, val description: String) {
    FREEBSD("freebsd", "FreeBSD"),
    LINUX("linux", "Linux"),
    MACOS("macos", "macOS"),
    WINDOWS("windows", "Windows")
}

enum class NativeArch(val attribValue: String) {
    ARM32("arm32"),
    ARM64("arm64"),
    PPC64LE("ppc64le"),
    RISCV64("riscv64"),
    X64("x64")
}

/**
 * @author squid233
 * @since 0.3.0
 */
enum class Native(
    val nativeOs: NativeOs,
    val nativeArch: NativeArch
) {
    FREEBSD_X64(
        NativeOs.FREEBSD,
        NativeArch.X64
    ),
    LINUX_X64(
        NativeOs.LINUX,
        NativeArch.X64
    ),
    LINUX_ARM64(
        NativeOs.LINUX,
        NativeArch.ARM64
    ),
    LINUX_ARM32(
        NativeOs.LINUX,
        NativeArch.ARM32
    ),
    LINUX_PPC64LE(
        NativeOs.LINUX,
        NativeArch.PPC64LE
    ),
    LINUX_RISCV64(
        NativeOs.LINUX,
        NativeArch.RISCV64
    ),
    MACOS_X64(
        NativeOs.MACOS,
        NativeArch.X64
    ),
    MACOS_ARM64(
        NativeOs.MACOS,
        NativeArch.ARM64
    ),
    WINDOWS_X64(
        NativeOs.WINDOWS,
        NativeArch.X64
    ),
    WINDOWS_ARM64(
        NativeOs.WINDOWS,
        NativeArch.ARM64
    ),
    ;

    val description = "${nativeOs.description} ${nativeArch.attribValue}"
}

fun nativeFromString(name: String?): Native? = name?.let {
    try {
        Native.valueOf(it)
    } catch (e: Exception) {
        null
    }
}
