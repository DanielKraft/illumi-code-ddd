package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;

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
