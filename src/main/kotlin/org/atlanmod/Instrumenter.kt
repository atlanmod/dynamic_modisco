package org.atlanmod

import org.apache.commons.io.FileUtils
import org.apache.maven.model.Dependency
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.io.xpp3.MavenXpp3Writer
import spoon.Launcher
import spoon.MavenLauncher
import spoon.OutputType
import spoon.processing.AbstractProcessor
import spoon.reflect.code.CtStatement
import spoon.reflect.code.CtStatementList
import spoon.reflect.declaration.CtMethod
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

class Instrumenter {
    lateinit var directory: File
    lateinit var target: File
    var beforeMethodProcessors : ArrayList<Tracer> = ArrayList()
    var beforeStatementProcessors : ArrayList<Tracer> = ArrayList()
    var afterMethodProcessors : ArrayList<Tracer> = ArrayList()
    var afterStatementProcessors : ArrayList<Tracer> = ArrayList()
    var dependencies : ArrayList<File> = ArrayList()

    fun instrument() {
        FileUtils.copyDirectory(directory, target)
        val srcDestination = File(target.absolutePath, "src/main/java")
        val pom = File(target.absolutePath, "pom.xml")
        val launcher: Launcher = MavenLauncher(directory.absolutePath, MavenLauncher.SOURCE_TYPE.ALL_SOURCE)

        launcher.environment.setShouldCompile(false) // we compile later with maven
        launcher.environment.isAutoImports = true
        launcher.environment.isCopyResources = true
        launcher.setSourceOutputDirectory(srcDestination.absolutePath)

        beforeMethodProcessors.forEach {
            launcher.addProcessor(object: AbstractProcessor<CtMethod<Any>>() {
                override fun process(method: CtMethod<Any>?) {
                    try {
                        val codeFactory = Launcher().factory.Code()
                        val instrumentedStatement: CtStatement = codeFactory.createCodeSnippetStatement("new ${it::class.qualifiedName}().before(\"${method?.signature}\")")
                        method?.body?.insertBegin<CtStatementList>(instrumentedStatement)
                    } catch (e: Exception) {
                        println(e.message)
                        println("Could not instrument ${method?.simpleName}")
                    }
                }
            })
        }

        beforeStatementProcessors.forEach {
            launcher.addProcessor(object: AbstractProcessor<CtStatement>() {
                override fun process(st: CtStatement?) {
                    try {
                        val codeFactory = Launcher().factory.Code()
                        val instrumentedStatement = codeFactory.createCodeSnippetStatement("new ${it::class.qualifiedName}().before(\"${st?.position?.compilationUnit?.file?.name}\",\"${st?.position?.column}\", \"${st?.position?.endColumn}\")")
                        st?.insertBefore<CtStatement>(instrumentedStatement)
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            })
        }

        afterMethodProcessors.forEach {
            launcher.addProcessor(object: AbstractProcessor<CtMethod<Any>>() {
                override fun process(method: CtMethod<Any>?) {
                    try {
                        val codeFactory = Launcher().factory.Code()
                        val instrumentedStatement = codeFactory.createCodeSnippetStatement("new ${it::class.qualifiedName}().after(\"${method?.signature}\")")
                        method?.body?.insertEnd<CtStatementList>(instrumentedStatement)
                    } catch (e: Exception) {
                        println(e.message)
                        println("Could not instrument ${method?.simpleName}")
                    }
                }
            })
        }

        afterStatementProcessors.forEach {
            launcher.addProcessor(object: AbstractProcessor<CtStatement>() {
                override fun process(st: CtStatement?) {
                    try {
                        val codeFactory = Launcher().factory.Code()
                        val instrumentedStatement = codeFactory.createCodeSnippetStatement("new ${it::class.qualifiedName}().after(\"${st?.position?.compilationUnit?.file?.name}\",\"${st?.position?.column}\", \"${st?.position?.endColumn}\")")
                        st?.insertAfter<CtStatement>(instrumentedStatement)
                    } catch (e: Exception) {
                        println(e.message)
                    }
                }
            })
        }

        dependencies.forEach {
            val model = MavenXpp3Reader().read(FileInputStream(pom))
            val dependencyToAdd = MavenXpp3Reader().read(FileInputStream(it))
            val dependency = Dependency()

            dependency.artifactId = dependencyToAdd.artifactId
            dependency.groupId = dependencyToAdd.groupId
            dependency.version = dependencyToAdd.version

            model.dependencies.add(dependency)

            MavenXpp3Writer().write(FileWriter(pom), model)
        }

        launcher.run()
    }
}