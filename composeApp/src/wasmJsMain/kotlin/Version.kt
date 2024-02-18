/**
 * @author squid233
 * @since 0.3.0
 */
class Version(private val versionName: String, vararg modules: Binding) {
    val modules: List<Binding> = modules.toList()
    override fun toString(): String = versionName
}

val V0_1_0 = Version("0.1.0", Binding.CORE, Binding.GLFW, Binding.NFD, Binding.OPENGL, Binding.STB)

val V_LATEST_SNAPSHOT = Version("0.1.0-SNAPSHOT", *V0_1_0.modules.toTypedArray())

const val V_JOML = "1.10.5"
