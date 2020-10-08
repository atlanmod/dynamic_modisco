package org.atlanmod.instrument

import org.apache.commons.io.FileUtils
import org.apache.maven.model.Dependency
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.io.xpp3.MavenXpp3Writer
import org.atlanmod.trace.Tracer
import spoon.Launcher
import spoon.MavenLauncher
import spoon.processing.AbstractProcessor
import spoon.reflect.code.CtReturn
import spoon.reflect.code.CtStatement
import spoon.reflect.code.CtStatementList
import spoon.reflect.declaration.CtClass
import spoon.reflect.declaration.CtMethod
import spoon.reflect.declaration.CtNamedElement
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.full.isSubclassOf

/**
 * TODO: Separate the method calls when it is a statement and a method instrumentation.
 *          using a different method signature would work
 */
class Instrumenter {
    val logger = Logger.getLogger(Instrumenter::class.qualifiedName)

    lateinit var directory: File
    lateinit var target: File
    var beforeMethodProcessors : ArrayList<Tracer<Any>> = ArrayList()
    var beforeStatementProcessors : ArrayList<Tracer<Any>> = ArrayList()
    var afterMethodProcessors : ArrayList<Tracer<Any>> = ArrayList()
    var afterStatementProcessors : ArrayList<Tracer<Any>> = ArrayList()
    var dependencies : ArrayList<File> = ArrayList()

    fun instrument() {
        FileUtils.copyDirectory(directory, target)
        var srcDestination = File(target.absolutePath, "src/")
        val pom = File(target.absolutePath, "pom.xml")

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

        configureLauncher(MavenLauncher.SOURCE_TYPE.APP_SOURCE, File(target.absolutePath, "/src/main/java").absolutePath).run()
        configureLauncher(MavenLauncher.SOURCE_TYPE.TEST_SOURCE, File(target.absolutePath, "/src/test/java").absolutePath).run()

    }


    /**
     * Recursive: get qualified name of method of class
     */
    private fun qualifiedName(elem: CtNamedElement): String {
        return when {
            elem::class.isSubclassOf(CtMethod::class) -> "${qualifiedName((elem as CtMethod<*>).parent as CtNamedElement)}#${(elem as CtMethod<*>).simpleName}"
            elem::class.isSubclassOf(CtClass::class) -> (elem as CtClass<*>).qualifiedName
            else -> ""
        }
    }

    private fun configureLauncher(type: MavenLauncher.SOURCE_TYPE, outputDirectory: String): Launcher {
        val launcher: Launcher = MavenLauncher(directory.absolutePath, type)

        launcher.environment.setShouldCompile(false) // we compile later with maven
        launcher.environment.isAutoImports = true
        launcher.environment.isCopyResources = true
        launcher.setSourceOutputDirectory(outputDirectory)

        beforeMethodProcessors.forEach {
            launcher.addProcessor(object: AbstractProcessor<CtMethod<Any>>() {
                override fun process(method: CtMethod<Any>?) {
                    try {
                        val codeFactory = Launcher().factory.Code()
                        val instrumentedStatement: CtStatement = codeFactory.createCodeSnippetStatement("new ${it::class.qualifiedName}().before(\"${method?.position?.compilationUnit?.file?.name}\",\"${qualifiedName(method!!)}\",\"${method.signature}\")")
                        method.body?.insertBegin<CtStatementList>(instrumentedStatement)
                    } catch (e: Exception) {
                        logger.log(Level.FINE, "Error instrumenting method $method", e)
                    }
                }
            })
        }

        beforeStatementProcessors.forEach {
            launcher.addProcessor(object: AbstractProcessor<CtStatement>() {
                override fun process(st: CtStatement?) {
                    try {
                        val codeFactory = Launcher().factory.Code()
                        val instrumentedStatement = codeFactory.createCodeSnippetStatement("new ${it::class.qualifiedName}().before(\"${st?.position?.compilationUnit?.file?.name}\",\"${st?.position?.sourceStart}\", \"${st?.position?.sourceEnd}\")")
                        st?.insertBefore<CtStatement>(instrumentedStatement)
                    } catch (e: Exception) {
                        logger.log(Level.FINE, "Error instrumenting statement $st", e)
                    }
                }
            })
        }

        afterMethodProcessors.forEach {
            launcher.addProcessor(object: AbstractProcessor<CtMethod<Any>>() {
                override fun process(method: CtMethod<Any>?) {
                    try {
                        val codeFactory = Launcher().factory.Code()
                        val instrumentedStatement = codeFactory.createCodeSnippetStatement("new ${it::class.qualifiedName}().after(\"${method?.position?.compilationUnit?.file?.name}\",\"${qualifiedName(method!!)}\",\"${method.signature}\")")
                        if (method.body?.statements?.last() is CtReturn<*>) { // If the method finishes with a return, inserting a statement after would make it unreachable
                            method.body?.statements?.last()?.insertBefore<CtStatement>(instrumentedStatement)
                        } else
                            method.body?.insertEnd<CtStatementList>(instrumentedStatement)
                    } catch (e: Exception) {
                        logger.log(Level.FINE, "Error instrumenting method $method", e)
                    }
                }
            })
        }

        afterStatementProcessors.forEach {
            launcher.addProcessor(object: AbstractProcessor<CtStatement>() {
                override fun process(st: CtStatement?) {
                    try {
                        val codeFactory = Launcher().factory.Code()
                        val instrumentedStatement = codeFactory.createCodeSnippetStatement("new ${it::class.qualifiedName}().after(\"${st?.position?.compilationUnit?.file?.name}\",\"${st?.position?.sourceStart}\", \"${st?.position?.sourceEnd}\")")
                        st?.insertAfter<CtStatement>(instrumentedStatement)
                    } catch (e: Exception) {
                        logger.log(Level.FINE, "Error instrumenting statement $st", e)
                    }
                }
            })
        }

        return launcher
    }
}