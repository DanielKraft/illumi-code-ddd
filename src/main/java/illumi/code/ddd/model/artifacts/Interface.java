package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;

/**
 * Entity-Class: Interface
 * @author Daniel Kraft
 */
public class Interface extends Artifact {
		
	private static final String QUERY_FIELDS 				= "MATCH (i:Interface)-[:DECLARES]->(f:Field) WHERE i.fqn= {path} RETURN DISTINCT f.name as name, f.signature as type, f.visibility as visibility";
	private static final String QUERY_METHODS				= "MATCH (i:Interface)-[:DECLARES]->(m:Method) WHERE i.fqn = {path} RETURN DISTINCT m.visibility as visibility, m.name as name, m.signature as signature";
	private static final String QUERY_IMPL 					= "MATCH (i1:Interface)-[:IMPLEMENTS]->(i2:Interface) WHERE i1.fqn= {path} RETURN i2.fqn as interface";
	private static final String QUERY_PARENT_ANNOTATIONS	= "MATCH (parent:Interface)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} RETURN DISTINCT type.fqn as annotation";
	private static final String QUERY_CHILD_ANNOTATIONS 	= "MATCH (parent:Interface)-[:DECLARES]->(child:Java)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} AND (child:Field OR child:Method) RETURN DISTINCT type.fqn as annotation";
	
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
		this.fields = (ArrayList<Field>) JavaArtifactService.getFields(getPath(), driver, QUERY_FIELDS);
    }
	
	public List<Method> getMethods() {
		return methods;
	}
	
	public void setMethods(Driver driver) {
		this.methods = (ArrayList<Method>) JavaArtifactService.getMethods(getPath(), driver, QUERY_METHODS);
    }

	public List<Interface> getInterfaces() {
		return implInterfaces;
	}
	
	public void setImplInterfaces(Driver driver, List<Interface> interfaces) {
		this.implInterfaces =  (ArrayList<Interface>) JavaArtifactService.getImplInterfaces(getPath(), driver, QUERY_IMPL, interfaces);
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) JavaArtifactService.getAnnotations(getPath(), driver, QUERY_PARENT_ANNOTATIONS, QUERY_CHILD_ANNOTATIONS, annotations);
	}
}
