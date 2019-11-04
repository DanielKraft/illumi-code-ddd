package illumi.code.ddd.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity-Class: Interface
 * @author Daniel Kraft
 */
public class Interface extends Artifact {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Class.class);
	
	public static final String QUERY_FIELDS =	"MATCH (i:Interface)-[:DECLARES]->(f:Field) WHERE i.fqn= {path} RETURN DISTINCT f.name as name, f.signature as signature, f.visibility as visibility";
	public static final String QUERY_METHODS =	"MATCH (i:Interface)-[:DECLARES]->(m:Method) WHERE i.fqn = {path} RETURN DISTINCT m.visibility as visibility, m.name as name, m.signature as signature";

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
	
	public void setMethods(Driver driver) {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_METHODS, Values.parameters( "path", getPath() ) );
    		convertResultToMethods(result, driver);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    }
	
	private void convertResultToMethods(StatementResult result, Driver driver) {
		result.stream()
			.parallel()
			.forEach(item -> {
				if (!item.get( "name" ).isNull()) {
			        Method newMethod = new Method( item );
			        methods.add(newMethod);
		        }
			});
	}
	
	public List<Field> getFields() {
		return fields;
	}

	public List<Method> getMethods() {
		return methods;
	}

	public List<Interface> getInterfaces() {
		return interfaces;
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
}
