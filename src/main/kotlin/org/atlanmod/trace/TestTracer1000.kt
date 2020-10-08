package org.atlanmod.trace

class TestTracer1000 : Tracer<Any>() {
    override fun setUp(): Double {
        return 0.0
    }

    override fun tearDown(): Double {
        return 0.0
    }

    override fun closure(vararg args: String): Boolean {
        if (args[1].contains("TestClass999#test")) {
            return true
        }
        return false
    }
}