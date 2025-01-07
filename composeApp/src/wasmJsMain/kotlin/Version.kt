/**
 * @author squid233
 * @since 0.3.0
 */
class Version(private val versionName: String, val modules: List<Binding>) {
    constructor(versionName: String, vararg modules: Binding) : this(versionName, modules.toList())

    override fun toString(): String = versionName
}

val V0_1_0 = Version("0.1.0", Binding.CORE, Binding.GLFW, Binding.NFD, Binding.OPENAL, Binding.OPENGL, Binding.STB)

val V_LATEST = V0_1_0
val V_LATEST_PRERELEASE = Version("0.1.0-alpha.0", V0_1_0.modules)
val V_LATEST_SNAPSHOT = Version("0.1.0-SNAPSHOT", V0_1_0.modules)

const val V_JOML = "1.10.8"

const val V_APP = "0.6.0"

const val V_TYPE_RELEASE = "release"
const val V_TYPE_PRERELEASE = "prerelease"
const val V_TYPE_SNAPSHOT = "snapshot"
