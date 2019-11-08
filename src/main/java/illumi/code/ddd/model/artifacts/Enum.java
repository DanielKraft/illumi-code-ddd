package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;

public class Enum extends Artifact {
	
	private static final String QUERY_FIELDS 				= "MATCH (e:Enum)-[:DECLARES]->(f:Field) WHERE e.fqn={path} RETURN DISTINCT f.name as name, f.signature as type, f.visibility as visibility";
	private static final String QUERY_PARENT_ANNOTATIONS 	= "MATCH (parent:Enum)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} RETURN DISTINCT type.fqn as annotation";
	private static final String QUERY_CHILD_ANNOTATIONS		= "MATCH (parent:Enum)-[:DECLARES]->(child:Java)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} AND (child:Field OR child:Method) RETURN DISTINCT type.fqn as annotation";
	
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
		this.fields = JavaArtifactService.getFields(getPath(), driver, QUERY_FIELDS);
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = JavaArtifactService.getAnnotations(getPath(), driver, QUERY_PARENT_ANNOTATIONS, QUERY_CHILD_ANNOTATIONS, annotations);
	}
}
