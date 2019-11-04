package illumi.code.ddd.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Record;

public class Enum extends Artifact {

	private ArrayList<String> fields;
	private ArrayList<Annotation> annotations;
	
	public Enum(Record record) {
		super(record, DDDType.VALUE_OBJECT);
		
		this.fields = new ArrayList<>();
		this.annotations = new ArrayList<>();
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = (ArrayList<String>) fields;
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void addAnnotations(Annotation annotation) {
		if (!this.annotations.contains(annotation)) {
			this.annotations.add(annotation);
		}
	}
}
