package org.atlanmod.trace

class TestTracer10 : Tracer<Any>() {
    override fun setUp(): Double {
        return 0.0
    }

    override fun tearDown(): Double {
        return 0.0
    }

    override fun closure(vararg args: String): Boolean {
        if (args[1].contains("TestClass9#test")) {
            return true
        }
        return false
    }
}