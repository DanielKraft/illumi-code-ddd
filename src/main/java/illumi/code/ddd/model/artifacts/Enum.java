package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;

public class Enum extends Artifact {

	private ArrayList<Field> fields;
	private ArrayList<Annotation> annotations;
	
	public Enum(Record record) {
		super(record, DDDType.VALUE_OBJECT);
		setFitness(new DDDFitness());
		
		this.fields = new ArrayList<>();
		this.annotations = new ArrayList<>();
	}

	public Enum(String name, String path) {
		super(name, path, DDDType.VALUE_OBJECT);
		setFitness(new DDDFitness());
		
		this.fields = new ArrayList<>();
		this.annotations = new ArrayList<>();
	}

	public List<Field> getFields() {
		return fields;
	}
	
	public void setFields(Driver driver) {
		this.fields = (ArrayList<Field>) new JavaArtifactService(driver, getPath()).getFields();
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) new JavaArtifactService(driver, getPath()).getAnnotations(annotations);
	}
}
