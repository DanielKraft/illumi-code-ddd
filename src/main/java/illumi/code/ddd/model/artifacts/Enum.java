package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;

public class Enum extends Artifact {

	private static final Logger LOGGER = LoggerFactory.getLogger(Class.class);
	
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
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_FIELDS, Values.parameters( "path", getPath() ) );
        	convertResultToFields(result);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    }

	private void convertResultToFields(StatementResult result) {
		result.stream()
			.parallel()
			.forEach(item -> {
				if (!item.get( "name" ).isNull()) {
					fields.add(new Field( item ));
		        }
			});
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, ArrayList<Annotation> annotations) {
		try ( Session session = driver.session() ) {
			setAnnotations(annotations, session, QUERY_PARENT_ANNOTATIONS);
    		
			setAnnotations(annotations, session, QUERY_CHILD_ANNOTATIONS);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void setAnnotations(ArrayList<Annotation> annotations, Session session, String Query) {
		session.run( Query, Values.parameters( "path", getPath()) )
			.stream()
			.parallel()
			.forEach(item -> {
				for (Annotation a : annotations) {
					if (a.getPath().contains(item.get("annotation").asString())) {
						this.annotations.add(a);
						break;
					}
				}
			});
	}
}
