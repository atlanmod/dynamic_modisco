package org.atlanmod

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.omg.smm.*
import java.util.*

class SimpleTraceTracer : Tracer() {

    companion object {
        var model = SmmFactory.eINSTANCE.createSmmModel()
        var observedMeasure: ObservedMeasure
        var previous: Measurement? = SmmFactory.eINSTANCE.createGradeMeasurement()

        init {
            val observation = SmmFactory.eINSTANCE.createObservation()
            val measure: Measure = SmmFactory.eINSTANCE.createGradeMeasure()
            val lib = SmmFactory.eINSTANCE.createMeasureLibrary()

            observedMeasure = SmmFactory.eINSTANCE.createObservedMeasure()
            observedMeasure.measurements.add(previous)


            observation.observedMeasures.add(observedMeasure)
            lib.measureElements.add(measure)
            model.libraries.add(lib)
            model.observations.add(observation)
        }

        fun close() {
            val reg = Resource.Factory.Registry.INSTANCE
            reg.extensionToFactoryMap.put("*", XMIResourceFactoryImpl())
            val rs = ResourceSetImpl()

            val resource = rs.createResource(URI.createURI("smmModel.xmi"))
            resource.contents.add(model)
            resource.allContents.forEach { println(it) }
            resource.save(Collections.EMPTY_MAP)
        }
    }

    override fun before(vararg args: String) {
        val measurement = SmmFactory.eINSTANCE.createGradeMeasurement()
        measurement.name = args[0]
        measurement.value = args.joinToString(";")

        var rs = SmmFactory.eINSTANCE.createBase1MeasurementRelationship()
        rs.name = "trace"
        rs.from = previous
        rs.to = measurement
        previous = measurement

        observedMeasure.measurements.add(measurement)
        observedMeasure.inRelationships.add(rs)
        measurement.measurementRelationships.add(rs)
    }

    override fun after(vararg args: String) {
        args.forEach { println(it) }
        observedMeasure.measurements.forEach{ println((it as GradeMeasurement).value)}
        close()
    }



}