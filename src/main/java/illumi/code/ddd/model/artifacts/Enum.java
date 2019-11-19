package illumi.code.ddd.model.artifacts;

import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;

public class Enum extends File {

	public Enum(Record record) {
		super(record, DDDType.VALUE_OBJECT);
		setFitness(new DDDFitness());
	}

	public Enum(String name, String path) {
		super(name, path, DDDType.VALUE_OBJECT);
		setFitness(new DDDFitness());
	}
}
