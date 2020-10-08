package org.atlanmod.instrument

import org.atlanmod.trace.Tracer
import java.io.File

class InstrumenterBuilder {
    private var instrumenter : Instrumenter = Instrumenter()
    private lateinit var directory: File
    private lateinit var target: File
    private var beforeMethodProcessors : ArrayList<Tracer<Any>> = ArrayList()
    private var beforeStatementProcessors : ArrayList<Tracer<Any>> = ArrayList()
    private var afterMethodProcessors : ArrayList<Tracer<Any>> = ArrayList()
    private var afterStatementProcessors : ArrayList<Tracer<Any>> = ArrayList()
    private val dependencies : ArrayList<File> = ArrayList()

    fun onProject(directory: File) : InstrumenterBuilder {
        this.directory = directory
        return this
    }

    fun toProject(directory: File) : InstrumenterBuilder {
        this.target = directory
        return this
    }

    fun beforeMethods(tracer: Tracer<Any>) : InstrumenterBuilder {
        beforeMethodProcessors.add(tracer)
        return this
    }

    fun afterMethods(tracer: Tracer<Any>) : InstrumenterBuilder {
        afterMethodProcessors.add(tracer)
        return this
    }

    fun beforeStatements(tracer: Tracer<Any>) : InstrumenterBuilder {
        beforeStatementProcessors.add(tracer)
        return this
    }

    fun afterStatements(tracer: Tracer<Any>) : InstrumenterBuilder {
        afterStatementProcessors.add(tracer)
        return this
    }

    fun withMavenDependency(pom: File) : InstrumenterBuilder {
        dependencies.add(pom)
        return this
    }

    fun build(): Instrumenter {
        instrumenter.target = this.target
        instrumenter.dependencies = this.dependencies
        instrumenter.directory = this.directory
        instrumenter.beforeStatementProcessors = this.beforeStatementProcessors
        instrumenter.beforeMethodProcessors = this.beforeMethodProcessors
        instrumenter.afterStatementProcessors = this.afterStatementProcessors
        instrumenter.afterMethodProcessors = this.afterMethodProcessors

        return instrumenter
    }
}
