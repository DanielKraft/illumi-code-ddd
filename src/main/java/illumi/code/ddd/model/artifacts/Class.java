package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import illumi.code.ddd.service.analyse.impl.ClassAnalyseService;
import illumi.code.ddd.service.fitness.impl.ClassFitnessService;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;
import illumi.code.ddd.model.Structure;

/**
 * Entity-Class: Class
 * @author Daniel Kraft
 */
public class Class extends File {

	private Class superClass;

	private ArrayList<String> dependencies;
	private ArrayList<String> used;
		
	public Class(Record record) {
		super(record, null);
		initClass();
	}
	
	public Class(String name, String path) {
		super(name, path, null);
		initClass();
	}
	
	private void initClass() {
		if (getName().toUpperCase().contains("FACTORY")) 		setType(DDDType.FACTORY);
		if (getName().toUpperCase().contains("REPOSITORY")) 	setType(DDDType.REPOSITORY);
		if (getName().toUpperCase().contains("SERVICE")) 		setType(DDDType.SERVICE);
		if (getName().toUpperCase().contains("APPLICATION"))	setType(DDDType.APPLICATION_SERVICE);
		if (getName().toUpperCase().contains("CONTROLLER"))		setType(DDDType.CONTROLLER);
		
		this.dependencies = new ArrayList<>();
		this.used = new ArrayList<>();
	}

	public Class getSuperClass() {
		return superClass;
	}

	public void setSuperClass(Driver driver, List<Class> classes) {
		this.superClass = new JavaArtifactService(driver, getPath()).getSuperClass(classes);
	}
	
	public void addSuperClass(Class superClass) {
		this.superClass = superClass;
	}

	public List<String> getDependencies() {
		return dependencies;
	}

	public void setDependencies(Driver driver, String path) {
		this.dependencies = (ArrayList<String>) new JavaArtifactService(driver, getPath()).getDependencies(path);
	}

	public void addDependencies(String path) {
		this.dependencies.add(path);
	}

	public List<String> getUsed() {
		return used;
	}

	public void addUsed(String path) {
		this.used.add(path);
	}

	public void setType(Structure structure) {
		new ClassAnalyseService(this, structure).setType();
	}

	public void setDomainEvent() {
		new ClassAnalyseService(this).setDomainEvent();
	}
	
	public void evaluate(Structure structure) {
		setFitness(new ClassFitnessService(this, structure).evaluate());
	}
}
