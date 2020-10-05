package org.atlanmod.trace

class TracerImpl: Tracer<Any>() {
    override fun setUp(): Float {
        return 0.0f
    }

    override fun tearDown(): Float {
        return 0.0f
    }

    override fun closure(vararg args: String): Boolean {
        if (args[0] == "main") {
            return true
        }
        return false
    }
}