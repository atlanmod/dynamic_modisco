package org.atlanmod

import java.io.File

class InstrumenterBuilder {
    private var instrumenter : Instrumenter = Instrumenter()
    private lateinit var directory: File
    private lateinit var target: File
    var beforeMethodProcessors : ArrayList<Tracer> = ArrayList()
    var beforeStatementProcessors : ArrayList<Tracer> = ArrayList()
    var afterMethodProcessors : ArrayList<Tracer> = ArrayList()
    var afterStatementProcessors : ArrayList<Tracer> = ArrayList()
    private val dependencies : ArrayList<File> = ArrayList()


    fun onProject(directory: File) : InstrumenterBuilder {
        this.directory = directory
        return this
    }

    fun toProject(directory: File) : InstrumenterBuilder {
        this.target = directory
        return this
    }

    fun beforeMethods(tracer: Tracer) : InstrumenterBuilder {
        beforeMethodProcessors.add(tracer)
        return this
    }

    fun afterMethods(tracer: Tracer) : InstrumenterBuilder {
        afterMethodProcessors.add(tracer)
        return this
    }

    fun beforeStatements(tracer: Tracer) : InstrumenterBuilder {
        beforeStatementProcessors.add(tracer)
        return this
    }

    fun afterStatements(tracer: Tracer) : InstrumenterBuilder {
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
