package illumi.code.ddd.model.artifacts;

import illumi.code.ddd.service.analyse.impl.InterfaceAnalyseService;
import illumi.code.ddd.service.fitness.impl.InterfaceFitnessService;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;

/**
 * Entity-Class: Interface
 * @author Daniel Kraft
 */
public class Interface extends File {

	public Interface(Record record) {
		super(record, null);
		initInterface();
	}
	
	public Interface(String name, String path) {
		super(name, path, null);
		initInterface();
	}
	
	private void initInterface() {
		if (getName().toUpperCase().contains("FACTORY")) {
			setType(DDDType.FACTORY);
		}
		else if (getName().toUpperCase().contains("REPOSITORY")) {
			setType(DDDType.REPOSITORY);
		}
		else {
			setType(DDDType.SERVICE);
		}
	}
	
	public void setType() {
		new InterfaceAnalyseService(this).setType();
	}
	
	public void evaluate() {
		setFitness(new InterfaceFitnessService(this).evaluate());
	}
}
