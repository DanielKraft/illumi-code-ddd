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

	private static final String QUERY_CLASS_FIELDS 				= "MATCH (c:Class)-[:DECLARES]->(f:Field) WHERE c.fqn={path} RETURN DISTINCT f.name as name, f.signature as type, f.visibility as visibility";
	private static final String QUERY_CLASS_METHODS 			= "MATCH (c:Class)-[:DECLARES]->(m:Method) WHERE c.fqn = {path} RETURN DISTINCT m.visibility as visibility, m.name as name, m.signature as signature";
	private static final String QUERY_CLASS_SUPER 				= "MATCH (c1:Class)-[:EXTENDS]->(c2:Class) WHERE c1.fqn={path} RETURN DISTINCT c2.fqn as superClass";
	private static final String QUERY_CLASS_IMPL 				= "MATCH (c:Class)-[:IMPLEMENTS]->(i:Interface) WHERE c.fqn={path} RETURN DISTINCT i.fqn as interface";
	private static final String QUERY_CLASS_PARENT_ANNOTATIONS 	= "MATCH (parent:Class)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} RETURN DISTINCT type.fqn as annotation";
	private static final String QUERY_CLASS_CHILD_ANNOTATIONS	= "MATCH (parent:Class)-[:DECLARES]->(child:Java)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} AND (child:Field OR child:Method) RETURN DISTINCT type.fqn as annotation";
	
	
	private ArrayList<Field> fields;
	
	private ArrayList<Method> methods;
	
	private ArrayList<Interface> implInterfaces;
	private Class superClass;
	
	private ArrayList<Annotation> annotations;
		
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
	}

	public List<Field> getFields() {
		return fields;
	}
	
	public void setFields(Driver driver) {
		this.fields = (ArrayList<Field>) JavaArtifactService.getFields(getPath(), driver, QUERY_CLASS_FIELDS);
    }
	
	public void addField(Field field) {
		this.fields.add(field);
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(Driver driver) {
		this.methods = (ArrayList<Method>) JavaArtifactService.getMethods(getPath(), driver, QUERY_CLASS_METHODS);
    }
	
	public void addMethod(Method method) {
		this.methods.add(method);
	}
	
	public List<Interface> getInterfaces() {
		return implInterfaces;
	}
	
	public void setImplInterfaces(Driver driver, List<Interface> interfaces) {
		this.implInterfaces =  (ArrayList<Interface>) JavaArtifactService.getImplInterfaces(getPath(), driver, QUERY_CLASS_IMPL, interfaces);
    }
	
	public void addImplInterface(Interface implInterface) {
		this.implInterfaces.add(implInterface);
	}

	public Class getSuperClass() {
		return superClass;
	}

	public void setSuperClass(Driver driver, List<Class> classes) {
		this.superClass = JavaArtifactService.getSuperClass(getPath(), driver, QUERY_CLASS_SUPER, classes);
	}
	
	public void addSuperClass(Class superClass) {
		this.superClass = superClass;
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) JavaArtifactService.getAnnotations(getPath(), driver, QUERY_CLASS_PARENT_ANNOTATIONS, QUERY_CLASS_CHILD_ANNOTATIONS, annotations);
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
