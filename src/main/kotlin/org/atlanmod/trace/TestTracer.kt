package org.atlanmod.trace

class TestTracer : Tracer<Any>() {
    override fun setUp(): Double {
        return 0.0
    }

    override fun tearDown(): Double {
        return 0.0
    }

    override fun closure(vararg args: String): Boolean {
        if (args[1] == "org.example.App#main") {
            return true
        }
        return false
    }
}