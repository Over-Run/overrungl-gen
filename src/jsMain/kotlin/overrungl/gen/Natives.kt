package overrungl.gen

enum class OsFamily(val titleName: String) {
    FREEBSD("FreeBSD"),
    LINUX("Linux"),
    MACOS("macOS"),
    WINDOWS("Windows"),
}

enum class Arch(val classifierName: String) {
    X64("x64"),
    ARM64("arm64"),
    ARM32("arm32"),
    PPC64LE("ppc64le"),
    RISCV64("riscv64"),
}

enum class Natives(val osFamily: OsFamily, val arch: Arch) {
    FREEBSD_X64(OsFamily.FREEBSD, Arch.X64),
    LINUX_X64(OsFamily.LINUX, Arch.X64),
    LINUX_ARM64(OsFamily.LINUX, Arch.ARM64),
    LINUX_ARM32(OsFamily.LINUX, Arch.ARM32),
    LINUX_PPC64LE(OsFamily.LINUX, Arch.PPC64LE),
    LINUX_RISCV64(OsFamily.LINUX, Arch.RISCV64),
    MACOS_X64(OsFamily.MACOS, Arch.X64),
    MACOS_ARM64(OsFamily.MACOS, Arch.ARM64),
    WINDOWS_X64(OsFamily.WINDOWS, Arch.X64),
    WINDOWS_ARM64(OsFamily.WINDOWS, Arch.ARM64),
}

fun nativesFromString(string: String): Natives? {
    return try {
        Natives.valueOf(string)
    } catch (_: Exception) {
        null
    }
}
