package org.atlanmod

abstract class Tracer {
    abstract fun before(vararg args: String)
    abstract fun after(vararg args: String)
}

