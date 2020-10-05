package org.atlanmod

import org.apache.maven.model.Dependency
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.io.xpp3.MavenXpp3Writer
import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.Invoker
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.util.*


class Executer {

    private fun addDependencyToNeodisco(pom: File) {
        println(pom)
        val model = MavenXpp3Reader().read(FileInputStream(pom))
        val thisModel = MavenXpp3Reader().read(FileInputStream("pom.xml"))
        val dependency = Dependency()

        // Wont run from JAR

        dependency.artifactId = thisModel.artifactId
        dependency.groupId = thisModel.groupId
        dependency.version = thisModel.version

        model.dependencies.add(dependency)

        MavenXpp3Writer().write(FileWriter(pom), model)
    }

    fun runTests(directory: File) {
        val pom = File(directory, "pom.xml")
        addDependencyToNeodisco(pom)
        val request: InvocationRequest = DefaultInvocationRequest()
        request.pomFile = pom
        request.goals = Arrays.asList("clean", "compile", "test")

        val invoker: Invoker = DefaultInvoker()
        invoker.execute(request)
    }

    fun runExec(directory: File) {
        val pom = File(directory, "pom.xml")
        addDependencyToNeodisco(pom)
        val request: InvocationRequest = DefaultInvocationRequest()
        request.pomFile = pom
        request.goals = Arrays.asList("clean", "compile", "exec:java")

        val invoker: Invoker = DefaultInvoker()
        invoker.execute(request)
    }
}