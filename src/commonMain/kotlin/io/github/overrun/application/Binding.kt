package io.github.overrun.application

enum class Natives(val itemName: String, val classifier: String) {
    LINUX_64("Linux x64", "natives-linux"),
    LINUX_ARM64("Linux arm64", "natives-linux-arm64"),
    LINUX_ARM32("Linux arm32", "natives-linux-arm32"),
    MACOS("macOS x64", "natives-macos"),
    MACOS_ARM64("macOS arm64", "natives-macos-arm64"),
    WIN_64("Windows x64", "natives-windows"),
    WIN_ARM64("Windows arm64", "natives-windows-arm64");

    override fun toString(): String = classifier
}

val ALL_NATIVES = Natives.entries.toTypedArray()
val NATIVES_LINUX = arrayOf(Natives.LINUX_64, Natives.LINUX_ARM64, Natives.LINUX_ARM32)
val NATIVES_MACOS = arrayOf(Natives.MACOS, Natives.MACOS_ARM64)
val NATIVES_WIN = arrayOf(Natives.WIN_64, Natives.WIN_ARM64)

enum class Binding(
    val itemName: String,
    val artifactName: String,
    val moduleName: String,
    vararg val natives: Natives
) {
    CORE("OverrunGL Core", "overrungl", "overrungl.core"),
    GLFW(
        "GLFW",
        "overrungl-glfw",
        "overrungl.glfw",
        Natives.LINUX_64,
        Natives.LINUX_ARM64,
        Natives.MACOS,
        Natives.MACOS_ARM64,
        Natives.WIN_64
    ),
    NFD("Native File Dialog", "overrungl-nfd", "overrungl.nfd", *ALL_NATIVES),
    OPENGL("OpenGL", "overrungl-opengl", "overrungl.opengl"),
    STB("stb", "overrungl-stb", "overrungl.stb", *ALL_NATIVES)
}

enum class Type(val itemName: String) {
    RELEASE("Release"),
    SNAPSHOT("Snapshot");

    override fun toString(): String = itemName
}

fun ofType(name: String) = Type.valueOf(name.uppercase())

enum class Lang(val itemName: String) {
    GROOVY("Groovy"),
    KOTLIN("Kotlin")
}

fun ofLang(name: String) = Lang.valueOf(name.uppercase())
