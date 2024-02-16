/**
 * @author squid233
 * @since 0.3.0
 */
enum class LangType(val typeName: String) {
    GRADLE_GROOVY("Gradle (Groovy)"),
    GRADLE_KOTLIN("Gradle (Kotlin)"),
}

fun langTypeFromString(name: String?): LangType? = when (name) {
    "GRADLE_GROOVY" -> LangType.GRADLE_GROOVY
    "GRADLE_KOTLIN" -> LangType.GRADLE_KOTLIN
    else -> null
}
