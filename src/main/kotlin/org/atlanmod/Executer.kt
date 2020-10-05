package org.atlanmod

import org.apache.maven.shared.invoker.DefaultInvocationRequest
import org.apache.maven.shared.invoker.DefaultInvoker
import org.apache.maven.shared.invoker.InvocationRequest
import org.apache.maven.shared.invoker.Invoker
import java.io.File
import java.util.*


class Executer {

    private fun runWithGoals(directory: File, vararg args: String) {

        val pom = File(directory, "pom.xml")
        val request: InvocationRequest = DefaultInvocationRequest()
        request.pomFile = pom
        request.goals = args.toList()

        val invoker: Invoker = DefaultInvoker()
        if (System.getProperty("maven.home") == null)
            invoker.mavenHome = File("/usr/share/maven")


        invoker.execute(request)
    }

    fun runTests(directory: File) {
        runWithGoals(directory, "clean", "compile", "test")
    }

    fun runExec(directory: File) {
        runWithGoals(directory, "clean", "compile", "exec:java")
    }
}