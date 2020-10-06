# Dynamic Modisco
Inject SMM-modeled dynamic analysis into MoDisco models

## Requirements
- Maven
- Java 8+

## How to

### Static analysis
- Use MoDisco to generate a static source code model of the project you want to analyse.
 
### Instrumentation
- We use Spoon to instrument the source code of the program to analyse.
- The program is -not- immediately compiled. The instrumented source code is cloned, and is executed later.
- We propose a simple API to specify additional analysis:
```
InstrumenterBuilder()
    .onProject(File("src/test/resources/projectToAnalyse"))
    .toProject(File("src/test/resources/instrumentedClone"))   
    .beforeStatements(TracerImpl()) // Custom analysis class analysing the statements
    .afterMethods(TracerImpl()) // Custom analysis class analysing the methods
    .withMavenDependency(File("pom.xml")) // Reference to the project defining TracerImpl()
    .build() // Build the Instrumenter
    .instrument() // Runs it.
```  

- To define an analysis behavior, create a Java class overriding the `org.atlanmod.Tracer` class.
- A `Closure` has to be defined in one of the Tracers: This is necessary to stop the analysis, and write the SMM model
- Example:

```
class TestTracer : Tracer<Any>() {
    /**
     * Definition of analysis behaviour to execute -before- the statements/methods
     */  
    override fun setUp(): Double {
        return 0.0 
    }

    /**
     * Definition of analysis behaviour to execute -after- the statements/methods
     */   
    override fun tearDown(): Double {        
        return 0.0  
    }

    /**
     * Custom methods: return true when you need to stop the analysis
     * e.g., after the execution of the main method.
     */      
    override fun closure(vararg args: String): Boolean {
        if (args[1] == "org.example.App#main") {
            return true
        }
        return false
    }
}
```
- Use as many ``Tracers`` as necessary, eventually at various granularities (methods, statements)

### Execution

- We rely on maven for the execution.
- The executer class defines basic maven goals : ``exec:java`` or `test`
- E.g., `Executer().runExec(projectFileToRun)` 

### Post-analysis

- The ``MoDiscoMerge`` class merge together a MoDisco model and a SMM model produced by our dynamic analysis.
- An execution trace is created as a chain of smm::Measurements linked together with smm::MeasurementRelationships labelled "trace".
- The values produced by the behavior on the Tracers is defined in the smm::Measurement.value fields.


 