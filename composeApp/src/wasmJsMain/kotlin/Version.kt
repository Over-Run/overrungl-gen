/**
 * @author squid233
 * @since 0.3.0
 */
class Version(val versionName: String) {
    override fun toString(): String = versionName
}

val V0_1_0 = Version("0.1.0")

val V_LATEST_SNAPSHOT = V0_1_0

const val V_JOML = "1.10.5"
