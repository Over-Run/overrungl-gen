/**
 * @author squid233
 * @since 0.3.0
 */
enum class LangType(val typeName: String) {
    GRADLE_GROOVY("Gradle (Groovy)"),
    GRADLE_KOTLIN("Gradle (Kotlin)"),
    GRADLE_CATALOG("Gradle (Catalog)"),
    MAVEN("Maven"),
    VM_OPTION("VM Option"),
    MANIFEST_ATTRIB("JAR-file manifest attribute")
}

fun langTypeFromString(name: String?): LangType? = name?.let {
    try {
        LangType.valueOf(it)
    } catch (e: Exception) {
        null
    }
}
