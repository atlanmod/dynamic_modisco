package org.atlanmod

class SimpleTraceTracer : Tracer() {

    override fun before(vararg args: String) {
        args.forEach { println(it) }    }

    override fun after(vararg args: String) {
        args.forEach { println(it) }    }
}