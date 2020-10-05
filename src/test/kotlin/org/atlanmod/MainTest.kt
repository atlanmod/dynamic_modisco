package org.atlanmod

import org.junit.Test
import spoon.Launcher
import java.io.File
import java.nio.file.Files

class MainTest {

    @Test
    fun test() {
        val launcher = Launcher()
        val toParse = File("src/test/resources/TestClass1.java")

        val ctClass1 = Launcher.parseClass(Files.readString(toParse.toPath()))
        //ctClass1.methods.forEach { it.body.statements.forEach { st -> println(st) } }

        val codeFactory = launcher.factory.Code()


        ctClass1.methods.forEach {
            for (i in 0 until it.body.statements.size) {
                val st = it.body.statements.get(i)
                st.allMetadata.forEach { (t, u) ->  println("${t} => ${u}")}
                val statement = codeFactory.createCodeSnippetStatement("System.out.println(\\\"${st}\\\")")
                st.insertBefore<spoon.reflect.code.CtStatement>(statement)
            }
        }

        println(ctClass1.toString())


    }

    @Test
    fun testStInstrumentation() {
        val f = File("src/test/resources/output")
        if (f.exists())
            f.deleteRecursively()

        f.mkdir()

        InstrumenterBuilder()
                .onProject(File("src/test/resources/dummy"))
                .toProject(f)
                .withMavenDependency(File("pom.xml")) // dependency to this project
                .beforeStatements(SimpleTraceTracer())
                .build()
                .instrument()
    }

    @Test
    fun testMethInstrumentation() {
        val f = File("src/test/resources/output")
        if (f.exists())
            f.deleteRecursively()
        f.mkdir()

        InstrumenterBuilder()
                .onProject(File("src/test/resources/dummy"))
                .toProject(f)
                .withMavenDependency(File("pom.xml")) // dependency to this project
                .beforeMethods(SimpleTraceTracer())
                .build()
                .instrument()

    }

    @Test
    fun testMethInstrumentationAndRunTests() {
        val f = File("src/test/resources/output")
        if (f.exists())
            f.deleteRecursively()

        f.mkdir()

        InstrumenterBuilder()
                .onProject(File("src/test/resources/dummy"))
                .toProject(f)
                .withMavenDependency(File("pom.xml")) // dependency to this project
                .beforeMethods(SimpleTraceTracer())
                .build()
                .instrument()

        Executer().runExec(f)
    }

    @Test
    fun testStInstrumentationAndRunTests() {
        val f = File("src/test/resources/output")
        if (f.exists())
            f.deleteRecursively()

        f.mkdir()

        InstrumenterBuilder()
                .onProject(File("src/test/resources/dummy"))
                .toProject(f)
                .withMavenDependency(File("pom.xml")) // dependency to this project
                .beforeStatements(SimpleTraceTracer())
                .build()
                .instrument()

        Executer().runExec(f)
    }

}
