package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import illumi.code.ddd.service.analyse.InterfaceAnalyseService;
import illumi.code.ddd.service.fitness.InterfaceFitnessService;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;

/**
 * Entity-Class: Interface
 * @author Daniel Kraft
 */
public class Interface extends Artifact {

	private ArrayList<Field> fields;
	
	private ArrayList<Method> methods;
	
	private ArrayList<Interface> implInterfaces;
	
	private ArrayList<Annotation> annotations;
		
	public Interface(Record record) {
		super(record, null);
		init();
	}
	
	public Interface(String name, String path) {
		super(name, path, null);
		init();
	}
	
	private void init() {
		if (getName().toUpperCase().contains("FACTORY")) {
			setType(DDDType.FACTORY);
		}
		else if (getName().toUpperCase().contains("REPOSITORY")) {
			setType(DDDType.REPOSITORY);
		}
		else {
			setType(DDDType.SERVICE);
		}
		
		this.fields = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.implInterfaces = new ArrayList<>();
		this.annotations = new ArrayList<>();
	}

	public List<Field> getFields() {
		return fields;
	}
	
	public void setFields(Driver driver) {
		this.fields = (ArrayList<Field>) new JavaArtifactService(driver, getPath()).getFields();
    }
	
	public void addField(Field field) {
		this.fields.add(field);
	}
	
	public List<Method> getMethods() {
		return methods;
	}
	
	public void setMethods(Driver driver) {
		this.methods = (ArrayList<Method>) new JavaArtifactService(driver, getPath()).getMethods();
    }
	
	public void addMethod(Method method) {
		this.methods.add(method);
	}

	public List<Interface> getInterfaces() {
		return implInterfaces;
	}
	
	public void setImplInterfaces(Driver driver, List<Interface> interfaces) {
		this.implInterfaces =  (ArrayList<Interface>) new JavaArtifactService(driver, getPath()).getImplInterfaces(interfaces);
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) new JavaArtifactService(driver, getPath()).getAnnotations(annotations);
	}
	
	public void setType() {
		new InterfaceAnalyseService(this).setType();
	}
	
	public void evaluate() {
		setFitness(new InterfaceFitnessService(this).evaluate());
	}
}
