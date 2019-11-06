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

import illumi.code.ddd.model.DDDType;

/**
 * Entity-Class: Class
 * @author Daniel Kraft
 */
public class Class extends Artifact {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Class.class);
	
	private static final String QUERY_FIELDS 				= "MATCH (c:Class)-[:DECLARES]->(f:Field) WHERE c.fqn={path} RETURN DISTINCT f.name as name, f.signature as type, f.visibility as visibility";
	private static final String QUERY_METHODS 				= "MATCH (c:Class)-[:DECLARES]->(m:Method) WHERE c.fqn = {path} RETURN DISTINCT m.visibility as visibility, m.name as name, m.signature as signature";
	private static final String QUERY_SUPER 				= "MATCH (c1:Class)-[:EXTENDS]->(c2:Class) WHERE c1.fqn={path} RETURN DISTINCT c2.fqn as superClass";
	private static final String QUERY_IMPL 					= "MATCH (c:Class)-[:IMPLEMENTS]->(i:Interface) WHERE c.fqn={path} RETURN DISTINCT i.fqn as interface";
	private static final String QUERY_PARENT_ANNOTATIONS 	= "MATCH (parent:Class)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} RETURN DISTINCT type.fqn as annotation";
	private static final String QUERY_CHILD_ANNOTATIONS		= "MATCH (parent:Class)-[:DECLARES]->(child:Java)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} AND (child:Field OR child:Method) RETURN DISTINCT type.fqn as annotation";
	
	
	private ArrayList<Field> fields;
	
	private ArrayList<Method> methods;
	
	private ArrayList<Interface> implInterfaces;
	private Class superClass;
	
	private ArrayList<Annotation> annotations;
		
	public Class(Record record) {
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
	
	public Class(String name, String path, DDDType type, ArrayList<Field> fields, ArrayList<Method> methods,
			ArrayList<Interface> implInterfaces, Class superClass, ArrayList<Annotation> annotations) {
		super(name, path, type);
		
		this.fields = fields;
		this.methods = methods;
		this.implInterfaces = implInterfaces;
		this.superClass = superClass;
		this.annotations = annotations;
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

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(Driver driver) {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_METHODS, Values.parameters( "path", getPath() ) );
    		convertResultToMethods(result);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    }
	
	private void convertResultToMethods(StatementResult result) {
		result.stream()
			.parallel()
			.forEach(item -> {
				if (!item.get( "name" ).isNull()) {
			        Method newMethod = new Method( item );
			        methods.add(newMethod);
		        }
			});
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

	public Class getSuperClass() {
		return superClass;
	}

	public void setSuperClass(Driver driver, ArrayList<Class> classes) {
		try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_SUPER, Values.parameters( "path", getPath() ));
							    		
		    String superPath = result.hasNext() ? result.next().get("superClass").asString() : null;
		    
		    if (superPath != null) {
		    	for (Class c : classes) {
					if (c.getPath().contains(superPath)) {
						this.superClass = c;
						
						break;
					}
				}
		    }
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
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
