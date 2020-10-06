package org.atlanmod.merge

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.gmt.modisco.java.*
import org.eclipse.gmt.modisco.java.emf.JavaPackage
import org.eclipse.jdt.core.dom.Name
import org.eclipse.jdt.core.dom.PackageDeclaration
import org.eclipse.modisco.java.composition.javaapplication.Java2File
import org.eclipse.modisco.java.composition.javaapplication.JavaapplicationPackage
import org.omg.smm.Measurement
import org.omg.smm.SmmModel
import org.omg.smm.SmmPackage
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.full.isSubclassOf

class MoDiscoMerge {
    private val java2files = ArrayList<Java2File>()

    fun merge(smm: File, modiscoModel: File, fragments: File) {
        JavaPackage.eINSTANCE.eClass()
        JavaapplicationPackage.eINSTANCE.eClass()
        SmmPackage.eINSTANCE.eClass()

        val reg = Resource.Factory.Registry.INSTANCE
        reg.extensionToFactoryMap["*"] = XMIResourceFactoryImpl()
        val rs = ResourceSetImpl()

        val smm : SmmModel = rs.getResource(URI.createURI(smm.toURI().toString()), true).contents[0] as SmmModel //Loading the SMM model
        rs.getResource(URI.createURI(modiscoModel.toURI().toString()), true).contents[0] //Loading Modisco Java model

        val java2kdms = fragments.list()
        java2kdms!!.forEach { rs.getResource(URI.createURI(File(fragments, it).toURI().toString()), true) }

        rs.resources.forEach { it.contents.forEach { eObject -> if (eObject is Java2File) java2files.add(eObject)}}

        smm.observations[0].observedMeasures[0].measurements.forEach{
            val reference = it.description.split(";")

            if (it.description?.matches(Regex(".*;[0-9]+;[0-9]+"))!!) {// is statement measurement
                // bad performances
                val st: Statement? = lookForStatement(reference[0], Integer.parseInt(reference[1]), Integer.parseInt(reference[2]))
                it.measurand = st // attaching MoDisco's statement to SMM measurement
            } else {// method measurement
                val meth: MethodDeclaration? = lookForMethod(reference[0], reference[1], reference[2])
                it.measurand = meth
            }
        }

        smm.eResource().save(Collections.EMPTY_MAP)
        displayTrace(smm)
    }

    private fun lookForStatement(unit: String, start: Int, end: Int) : Statement? {
        val file = java2files.find { it.javaUnit.name == unit }
        // MoDisco's end column is smaller than spoon's by 1.
        return file?.children?.filter { it.node is Statement }?.find { it.startPosition == start && it.endPosition == end+1 }?.node as Statement
    }

    private fun lookForMethod(unit: String, qualifiedName: String, signature: String): MethodDeclaration? {
        val file = java2files.find { it.javaUnit.name == unit }
        // wont work with lambda exp.
        // wont work with multiple methods with the same name in a compilation unit. This should also consider the class / inner class as well as the signature
        return file?.children?.filter { it.node is MethodDeclaration }?.map { it.node as MethodDeclaration }?.first{ qualifiedName(it) == qualifiedName}
    }

    /**
     * get qualified name of EObject: iterate over the object and its parents recursively until the parent has no name
     */
    private fun qualifiedName(element: EObject) : String {
        return if (element::class.isSubclassOf(NamedElement::class))
            when  {
                element::class.isSubclassOf(MethodDeclaration::class) -> "${qualifiedName(element.eContainer())}#${(element as NamedElement).name}"
                element::class.isSubclassOf(ClassDeclaration::class) -> "${qualifiedName(element.eContainer())}.${(element as NamedElement).name}"
                element::class.isSubclassOf(Package::class) -> if (!element.eContainer()::class.isSubclassOf(Package::class)) (element as NamedElement).name else "${qualifiedName(element.eContainer())}.${(element as NamedElement).name}"
                else -> ""
            }
        else
            ""
    }

    private fun displayTrace(model: SmmModel) {
        var m: Measurement = model.observations[0].observedMeasures[0].measurements[0]
        while (m.measurementRelationships.size > 0) {
            println("${if (m.measurand != null) m.measurand else ""} --> ")
            m = m.measurementRelationships[0].to as Measurement
        }
        println(m.measurand)

    }
}