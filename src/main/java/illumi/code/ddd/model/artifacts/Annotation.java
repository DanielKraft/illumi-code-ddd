package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import illumi.code.ddd.service.fitness.AnnotationFitnessService;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;

public class Annotation extends Artifact {

	private ArrayList<Field> fields;
	private ArrayList<Method> methods;
	private ArrayList<Annotation> annotations;

	public Annotation(Record record) {
		super(record, DDDType.INFRASTRUCTURE);
		init();
	}
	
	public Annotation(String name, String path) {
		super(name, path, DDDType.INFRASTRUCTURE);
		init();
	}
	
	private void init() {
		this.fields = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.annotations = new ArrayList<>();
	}
	
	public List<Field> getFields() {
		return fields;
	}

	public void setFields(Driver driver) {
		this.fields = (ArrayList<Field>) new JavaArtifactService(driver, getPath()).getFields();
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(Driver driver) {
		this.methods = (ArrayList<Method>) new JavaArtifactService(driver, getPath()).getMethods();
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) new JavaArtifactService(driver, getPath()).getAnnotations(annotations);
	}
	
	public void evaluate() {
		setFitness(new AnnotationFitnessService(this).evaluate());
	}
}
