package overrungl.gen

enum class LangType(val titleName: String) {
    GRADLE_KOTLIN("Gradle (Kotlin)"),
    GRADLE_GROOVY("Gradle (Groovy)"),
    VM_OPTION("VM Option"),
    MANIFEST("JAR-file manifest attribute")
}

fun langTypeFromString(string: String): LangType? = when (string) {
    "GRADLE_KOTLIN" -> LangType.GRADLE_KOTLIN
    "GRADLE_GROOVY" -> LangType.GRADLE_GROOVY
    "VM_OPTION" -> LangType.VM_OPTION
    "MANIFEST" -> LangType.MANIFEST
    else -> null
}
