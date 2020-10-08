package org.atlanmod.scalability

import org.atlanmod.Executer
import org.atlanmod.instrument.InstrumenterBuilder
import org.atlanmod.merge.MoDiscoMerge
import org.atlanmod.trace.*
import org.junit.Test
import java.io.File

class TestGenerator {
    fun generate(number: Int): File {
        val repo = File("src/test/resources/bench")
        val mainRepo = File(repo, "src/main/java")
        if (mainRepo.exists())
            mainRepo.deleteRecursively()
        mainRepo.mkdirs()

        val testRepo = File(repo, "src/test/java")
        if (testRepo.exists())
            testRepo.deleteRecursively()
        testRepo.mkdirs()

        for (i in 0 until number) {

            val f = File(mainRepo, "Class${i}.java")
            f.createNewFile()

           f.writeText("" +
                    "public class Class${i} { public static int main(int x) { " +
                        "int fact = 1;" +
                        "for(int i = 2; i <= x; i++) {" +
                            "fact = fact * i;" +
                        "}" +
                        "return fact; " +
                    "}}")


            val t = File(testRepo, "TestClass${i}.java")
            t.createNewFile()
            t.writeText("" +
                    "public class TestClass${i} { " +
                    "@org.junit.Test " +
                    "public void test() { " +
                        "assert new Class${i}().main(5) == 120;"+
                    "}}")

        }
        return repo
    }

    @Test
    fun generate10AndBuildSMMModel() {
        val repo = generate(10)
        val instrumentedRepo = File("src/test/resources/instrumentedRepo")
        if (instrumentedRepo.exists())
            instrumentedRepo.deleteRecursively()
        instrumentedRepo.mkdirs()

        var before = System.currentTimeMillis()
        InstrumenterBuilder()
                .onProject(repo)
                .toProject(instrumentedRepo)
                .beforeStatements(TestTracer())
                .afterMethods(TestTracer10())
                .withMavenDependency(File("pom.xml"))
                .build()
                .instrument()

        println("instrumentation lasted: ${System.currentTimeMillis() - before}")

        before = System.currentTimeMillis()
        Executer().runTests(instrumentedRepo)
        println("execution lasted: ${System.currentTimeMillis() - before}")

        before = System.currentTimeMillis()
        MoDiscoMerge().merge(File(instrumentedRepo, "smmModel.xmi"), File(instrumentedRepo, "bench_java.xmi"), File(instrumentedRepo, "java2kdmFragments"))
        println("merging lasted: ${System.currentTimeMillis() - before}")
    }

    @Test
    fun generate100AndBuildSMMModel() {
        val repo = generate(100)
        val instrumentedRepo = File("src/test/resources/instrumentedRepo")
        if (instrumentedRepo.exists())
            instrumentedRepo.deleteRecursively()
        instrumentedRepo.mkdirs()

        var before = System.currentTimeMillis()
        InstrumenterBuilder()
                .onProject(repo)
                .toProject(instrumentedRepo)
                .beforeStatements(TestTracer())
                .afterMethods(TestTracer100())
                .withMavenDependency(File("pom.xml"))
                .build()
                .instrument()

        println("instrumentation lasted: ${System.currentTimeMillis() - before}")

        before = System.currentTimeMillis()
        Executer().runTests(instrumentedRepo)
        println("execution lasted: ${System.currentTimeMillis() - before}")

        before = System.currentTimeMillis()
        MoDiscoMerge().merge(File(instrumentedRepo, "smmModel.xmi"), File(instrumentedRepo, "bench_java.xmi"), File(instrumentedRepo, "java2kdmFragments"))
        println("merging lasted: ${System.currentTimeMillis() - before}")
    }

    @Test
    fun generate1000AndBuildSMMModel() {
        val repo = generate(1000)
        val instrumentedRepo = File("src/test/resources/instrumentedRepo")
        if (instrumentedRepo.exists())
            instrumentedRepo.deleteRecursively()
        instrumentedRepo.mkdirs()

        var before = System.currentTimeMillis()
        InstrumenterBuilder()
                .onProject(repo)
                .toProject(instrumentedRepo)
                .beforeStatements(TestTracer())
                .afterMethods(TestTracer1000())
                .withMavenDependency(File("pom.xml"))
                .build()
                .instrument()

        println("instrumentation lasted: ${System.currentTimeMillis() - before}")

        before = System.currentTimeMillis()
        Executer().runTests(instrumentedRepo)
        println("execution lasted: ${System.currentTimeMillis() - before}")

        before = System.currentTimeMillis()
        MoDiscoMerge().merge(File(instrumentedRepo, "smmModel.xmi"), File(instrumentedRepo, "bench_java.xmi"), File(instrumentedRepo, "java2kdmFragments"))
        println("merging lasted: ${System.currentTimeMillis() - before}")
    }

    @Test
    fun generate10000AndBuildSMMModel() {
        val repo = generate(10000)
        val instrumentedRepo = File("src/test/resources/instrumentedRepo")
        if (instrumentedRepo.exists())
            instrumentedRepo.deleteRecursively()
        instrumentedRepo.mkdirs()

        var before = System.currentTimeMillis()
        InstrumenterBuilder()
                .onProject(repo)
                .toProject(instrumentedRepo)
                .beforeStatements(TestTracer())
                .afterMethods(TestTracer10000())
                .withMavenDependency(File("pom.xml"))
                .build()
                .instrument()

        println("instrumentation lasted: ${System.currentTimeMillis() - before}")

        before = System.currentTimeMillis()
        Executer().runTests(instrumentedRepo)
        println("execution lasted: ${System.currentTimeMillis() - before}")

        before = System.currentTimeMillis()
        MoDiscoMerge().merge(File(instrumentedRepo, "smmModel.xmi"), File(instrumentedRepo, "bench_java.xmi"), File(instrumentedRepo, "java2kdmFragments"))
        println("merging lasted: ${System.currentTimeMillis() - before}")
    }


    @Test
    fun testmerge() {
        val instrumentedRepo = File("src/test/resources/instrumentedRepo")

        val before = System.currentTimeMillis()
        MoDiscoMerge().merge(File(instrumentedRepo, "smmModel.xmi"), File(instrumentedRepo, "bench_java.xmi"), File(instrumentedRepo, "java2kdmFragments"))
        println("merging lasted: ${System.currentTimeMillis() - before}")
    }


}