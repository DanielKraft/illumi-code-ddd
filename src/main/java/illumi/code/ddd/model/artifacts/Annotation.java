package illumi.code.ddd.model.artifacts;

import illumi.code.ddd.service.fitness.impl.AnnotationFitnessService;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;

public class Annotation extends File {

	public Annotation(Record record) {
		super(record, DDDType.INFRASTRUCTURE);
	}
	
	public Annotation(String name, String path) {
		super(name, path, DDDType.INFRASTRUCTURE);
	}
	
	public void evaluate() {
		setFitness(new AnnotationFitnessService(this).evaluate());
	}
}
