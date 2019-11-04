package illumi.code.ddd.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Record;

/**
 * Entity-Class: Interface
 * @author Daniel Kraft
 */
public class Interface extends Artifact {
	
	private ArrayList<Field> fields;
	
	private ArrayList<Method> methods;
	
	private ArrayList<Interface> interfaces;
	
	private ArrayList<Annotation> annotations;
		
	public Interface(Record record) {
		super(record, null);
		
		this.fields = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.interfaces = new ArrayList<>();
		this.annotations = new ArrayList<>();
	}
	
	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = (ArrayList<Field>) fields;
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(List<Method> methods) {
		this.methods = (ArrayList<Method>) methods;
	}

	public List<Interface> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<Interface> interfaces) {
		this.interfaces = (ArrayList<Interface>) interfaces;
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
