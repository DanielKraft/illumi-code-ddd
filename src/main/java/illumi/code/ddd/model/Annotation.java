package illumi.code.ddd.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Record;

public class Annotation extends Artifact {
	
	private ArrayList<Field> fields;
	
	private ArrayList<Method> methods;
	
	private ArrayList<Annotation> annotations;

	public Annotation(Record record) {
		super(record, null);

		this.fields = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.annotations = new ArrayList<>();
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void addAnnotations(Annotation annotation) {
		if (!this.annotations.contains(annotation)) {
			this.annotations.add(annotation);
		}
	}
	
	public List<Field> getFields() {
		return fields;
	}

	public void addFields(Field field) {
		this.fields.add(field);
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void addMethods(Method method) {
		this.methods.add(method);
	}
}
