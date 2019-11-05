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
	
	private static final String QUERY_FIELDS 				= "MATCH (i:Interface)-[:DECLARES]->(f:Field) WHERE i.fqn= {path} RETURN DISTINCT f.name as name, f.signature as signature, f.visibility as visibility";
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
		return implInterfaces;
	}
	
	public void setImplInterfaces(Driver driver, ArrayList<Interface> interfaces) {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_IMPL, Values.parameters( "path", getPath() ) );
    		convertResultToInterface(result, interfaces);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    }
	
	private void convertResultToInterface(StatementResult result, ArrayList<Interface> interfaces) {		
		result.stream()
			.parallel()
			.forEach(item -> {
				for (Interface i : interfaces) {
					if (i.getPath().contains(item.get( "interface" ).asString())) {
						this.implInterfaces.add(i);
						
						break;
					}
				}
				item.get( "interface" ).asString();
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
