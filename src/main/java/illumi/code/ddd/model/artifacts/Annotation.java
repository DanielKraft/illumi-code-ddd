package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import illumi.code.ddd.service.fitness.AnnotationFitnessService;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;

public class Annotation extends Artifact {
		
	private static final String QUERY_FIELDS 				= "MATCH (a:Annotation)-[:DECLARES]->(f:Field) WHERE a.fqn={path} RETURN DISTINCT f.name as name, f.signature as type, f.visibility as visibility";
	private static final String QUERY_METHODS 				= "MATCH (a:Annotation)-[:DECLARES]->(m:Method) WHERE a.fqn = {path} RETURN DISTINCT m.visibility as visibility, m.name as name, m.signature as signature";
	private static final String QUERY_PARENT_ANNOTATIONS 	= "MATCH (parent:Annotation)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} RETURN DISTINCT type.fqn as annotation";
	private static final String QUERY_CHILD_ANNOTATIONS		= "MATCH (parent:Annotation)-[:DECLARES]->(child:Java)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} AND (child:Field OR child:Method) RETURN DISTINCT type.fqn as annotation";
	
	private ArrayList<Field> fields;
	
	private ArrayList<Method> methods;
	
	private ArrayList<Annotation> annotations;

	public Annotation(Record record) {
		super(record, DDDType.INFRASTRUCTUR);
		init();
	}
	
	public Annotation(String name, String path) {
		super(name, path, DDDType.INFRASTRUCTUR);
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
		this.fields = (ArrayList<Field>) JavaArtifactService.getFields(getPath(), driver, QUERY_FIELDS);
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(Driver driver) {
		this.methods = (ArrayList<Method>) JavaArtifactService.getMethods(getPath(), driver, QUERY_METHODS);
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) JavaArtifactService.getAnnotations(getPath(), driver, QUERY_PARENT_ANNOTATIONS, QUERY_CHILD_ANNOTATIONS, annotations);
	}
	
	public void evaluate() {
		new AnnotationFitnessService(this).evaluate();
	}
}
