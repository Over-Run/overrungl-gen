package overrungl.gen

enum class LangType(val titleName: String) {
    GRADLE_KOTLIN("Gradle (Kotlin)"),
    GRADLE_GROOVY("Gradle (Groovy)"),
    VM_OPTION("VM Option"),
    MANIFEST("JAR-file manifest attribute")
}

fun langTypeFromString(string: String): LangType? {
    return try {
        LangType.valueOf(string)
    } catch (_: Exception) {
        null
    }
}
