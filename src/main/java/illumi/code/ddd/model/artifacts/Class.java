package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import illumi.code.ddd.service.analyse.ClassAnalyseService;
import illumi.code.ddd.service.fitness.ClassFitnessService;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;
import illumi.code.ddd.service.StructureService;

/**
 * Entity-Class: Class
 * @author Daniel Kraft
 */
public class Class extends Artifact {
	
	private ArrayList<Field> fields;
	private ArrayList<Method> methods;
	
	private ArrayList<Interface> implInterfaces;
	private Class superClass;
	
	private ArrayList<Annotation> annotations;

	private ArrayList<String> dependencies;
	private ArrayList<String> used;
		
	public Class(Record record) {
		super(record, null);
		init();
	}
	
	public Class(String name, String path) {
		super(name, path, null);
		init();
	}
	
	private void init() {
		if (getName().toUpperCase().contains("FACTORY")) 		setType(DDDType.FACTORY);
		if (getName().toUpperCase().contains("REPOSITORY")) 	setType(DDDType.REPOSITORY);
		if (getName().toUpperCase().contains("SERVICE")) 		setType(DDDType.SERVICE);
		if (getName().toUpperCase().contains("APPLICATION"))	setType(DDDType.APPLICATION_SERVICE);
		if (getName().toUpperCase().contains("CONTROLLER"))		setType(DDDType.CONTROLLER);
		
		this.fields = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.implInterfaces = new ArrayList<>();
		this.annotations = new ArrayList<>();
		this.dependencies = new ArrayList<>();
		this.used = new ArrayList<>();
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
	
	public void addImplInterface(Interface implInterface) {
		this.implInterfaces.add(implInterface);
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
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) new JavaArtifactService(driver, getPath()).getAnnotations(annotations);
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

	public void setType(StructureService structureService) {
		new ClassAnalyseService(this, structureService).setType();
	}

	public void setDomainEvent() {
		new ClassAnalyseService(this).setDomainEvent();
	}
	
	public void evaluate(StructureService structureService) {
		setFitness(new ClassFitnessService(this, structureService).evaluate());
	}
}
