package org.atlanmod.trace

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.omg.smm.*
import java.util.*

/**
 * Abstract tracer.
 * Create SMM element for each entity executed in the analysed program
 * Extend this tracer by overriding setUp() and tearDown() for custom analysis
 * T is the returned type provided by the analysis
 */
abstract class Tracer<T> {

    companion object {
        var model = SmmFactory.eINSTANCE.createSmmModel()
        var observedMeasure: ObservedMeasure
        var previous: Measurement? = SmmFactory.eINSTANCE.createDirectMeasurement()

        /**
         * Static initialization.
         */
        init {

            val observation = SmmFactory.eINSTANCE.createObservation()
            val measure: Measure = SmmFactory.eINSTANCE.createDirectMeasure()
            val lib = SmmFactory.eINSTANCE.createMeasureLibrary()

            observedMeasure = SmmFactory.eINSTANCE.createObservedMeasure()
            observedMeasure.measurements.add(previous)

            previous?.description = "entry point;_;_"
            observation.observedMeasures.add(observedMeasure)
            lib.measureElements.add(measure)
            model.libraries.add(lib)
            model.observations.add(observation)
        }

        /**
         * Static: closes the model and write it down in XMI format
         */
        fun close() {
            val reg = Resource.Factory.Registry.INSTANCE
            reg.extensionToFactoryMap["*"] = XMIResourceFactoryImpl()
            val rs = ResourceSetImpl()

            val resource = rs.createResource(URI.createURI("smmModel.xmi"))
            resource.contents.add(model)
            resource.save(Collections.EMPTY_MAP)

            println("Wrote dynamic analysis model in ${URI.createURI("smmModel.xmi").toFileString()}")
        }
    }

    /**
     * Method called before statements of method by the instrumenter
     */
    fun before(vararg args: String) {
        val measurement = createMeasurement(*args)
        (measurement as DirectMeasurement).value = setUp() as Double

        var rs = SmmFactory.eINSTANCE.createBase1MeasurementRelationship()
        rs.name = "trace"
        rs.from = previous
        rs.to = measurement

        previous?.measurementRelationships?.add(rs)
        previous = measurement

        (previous as DirectMeasurement).value = setUp() as Double // need some flexibility: generic measurement with value of type T

        observedMeasure.measurements.add(measurement)
        //observedMeasure.inRelationships.add(rs)
    }

    /**
     * Method called after statements or methods by the instrumenter.
     */
    fun after(vararg args: String) {
        if (previous?.description == args.joinToString(";")) // there is a before step that targeted the same element
            (previous as DirectMeasurement).value = tearDown() as Double
        else {
            val measurement = createMeasurement(*args)
            (measurement as DirectMeasurement).value = tearDown() as Double
        }

        if (closure(*args))
            close()
    }

    /**
     * Defines an SMM Measurement.
     * Current version provides a @GradeMeasurement, a smarter version would create a measurement depending to <T>
     */
    private fun createMeasurement(vararg args: String) : Measurement {
        val measurement = SmmFactory.eINSTANCE.createDirectMeasurement()
        measurement.name = args[0]
        measurement.description = args.joinToString(";")
        return measurement

    }

    /**
     * defines additional analysis to call in a before() method
     *
     */
    abstract fun setUp(): T

    /**
     * defines additional analysis to call in an after() method
     */
    abstract fun tearDown(): T

    /**
     * condition defining when to stop and write down the SMM model
     */
    abstract fun closure(vararg args: String): Boolean
}