package io.github.overrun.application

const val VER_JOML = "1.10.5"

val VER_MAPPING = HashMap<String, Version>()

val VER_0_1_0 = Version("0.1.0", arrayOf(Binding.CORE, Binding.GLFW, Binding.NFD, Binding.OPENGL, Binding.STB))

val VER_LATEST = VER_0_1_0

data class Version(val num: String, val bindings: Array<Binding>) {
    init {
        VER_MAPPING[num] = this
    }

    override fun toString(): String = num

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Version) return false

        if (num != other.num) return false
        if (!bindings.contentEquals(other.bindings)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = num.hashCode()
        result = 31 * result + bindings.contentHashCode()
        return result
    }
}
