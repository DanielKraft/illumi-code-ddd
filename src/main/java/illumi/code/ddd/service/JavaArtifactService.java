package illumi.code.ddd.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaArtifactService {


	private static final String QUERY_FIELDS 				= "MATCH (a:Java)-[:DECLARES]->(f:Field) WHERE a.fqn = {path} RETURN DISTINCT f.name as name, f.signature as type, f.visibility as visibility";
	private static final String QUERY_METHODS 				= "MATCH (a:Java)-[:DECLARES]->(m:Method) WHERE a.fqn = {path} RETURN DISTINCT m.visibility as visibility, m.name as name, m.signature as signature";

	private static final String QUERY_SUPER 				= "MATCH (a:Java)-[:EXTENDS]->(super) WHERE a.fqn = {path} RETURN DISTINCT super.fqn as superClass";
	private static final String QUERY_IMPL 					= "MATCH (a:Java)-[:IMPLEMENTS]->(i:Interface) WHERE a.fqn={path} RETURN DISTINCT i.fqn as interface";

	private static final String QUERY_PARENT_ANNOTATIONS 	= "MATCH (parent:Java)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} RETURN DISTINCT type.fqn as annotation";
	private static final String QUERY_CHILD_ANNOTATIONS		= "MATCH (parent:Java)-[:DECLARES]->(child:Java)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} AND (child:Field OR child:Method) RETURN DISTINCT type.fqn as annotation";

	private static final String QUERY_DEPENDENCIES 			= "MATCH (artifact:Java)-[:DEPENDS_ON]->(dependency:Java) WHERE artifact.fqn={path} AND dependency.fqn CONTAINS {rootPath} RETURN DISTINCT dependency.name as dependencies";

	private static final Logger LOGGER = LoggerFactory.getLogger(JavaArtifactService.class);

	private Driver driver;
	private String path;


	public JavaArtifactService(Driver driver, String path) {
		this.driver = driver;
		this.path = path;
	}

	public List<String> getDependencies(String rootPath) {
		try ( Session session = driver.session() ) {
			Map<String, Object> params = new HashMap<>();
			params.put("rootPath", rootPath);
			params.put("path", path);
			StatementResult result = session.run( QUERY_DEPENDENCIES, params);

			ArrayList<String> dependencies = new ArrayList<>();

			result.stream()
				.parallel()
				.forEachOrdered(item -> dependencies.add(item.get("dependencies").asString()));

			return dependencies;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	public List<Field> getFields() {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_FIELDS, Values.parameters( "path", path ) );
        	return convertResultToFields(result);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    	return new ArrayList<>();
    }

	private ArrayList<Field> convertResultToFields(StatementResult result) {
		ArrayList<Field> fields = new ArrayList<>();
		result.stream()
			.parallel()
			.forEachOrdered(item -> {
				if (!item.get( "name" ).isNull()) {
					fields.add(new Field( item ));
		        }
			});
		return fields;
	}
	
	public List<Method> getMethods() {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_METHODS, Values.parameters( "path", path ) );
    		return convertResultToMethods(result);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    	return new ArrayList<>();
    }
	
	private ArrayList<Method> convertResultToMethods(StatementResult result) {
		ArrayList<Method> methods = new ArrayList<>();
		result.stream()
			.parallel()
			.forEachOrdered(item -> {
				if (!item.get( "name" ).isNull()) {
			        Method newMethod = new Method( item );
			        methods.add(newMethod);
		        }
			});
		return methods;
	}
	
	public List<Interface> getImplInterfaces(List<Interface> interfaces) {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_IMPL, Values.parameters( "path", path ) );
    		return convertResultToInterface(result, interfaces);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    	return new ArrayList<>();
    }
	
	private ArrayList<Interface> convertResultToInterface(StatementResult result, List<Interface> interfaces) {
		ArrayList<Interface> implInterfaces = new ArrayList<>();
		result.stream()
			.parallel()
			.forEachOrdered(item -> {
				for (Interface i : interfaces) {
					if (i.getPath().contains(item.get( "interface" ).asString())) {
						implInterfaces.add(i);
						break;
					}
				}
				item.get( "interface" ).asString();
			});
		return implInterfaces;
	}
	
	public Class getSuperClass(List<Class> classes) {
		try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_SUPER, Values.parameters( "path", path ));
							    		
		    String superPath = result.hasNext() ? result.next().get("superClass").asString() : null;
		    
		    if (superPath != null) {
		    	for (Class c : classes) {
					if (c.getPath().equals(superPath)) {
						return c;
					}
				}
		    }
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}
	
	public List<Annotation> getAnnotations(List<Annotation> annotations) {
		ArrayList<Annotation> result = new ArrayList<>();
		try ( Session session = driver.session() ) {
			getAnnotations(path, result, annotations, session, QUERY_PARENT_ANNOTATIONS);
    		
			getAnnotations(path, result, annotations, session, QUERY_CHILD_ANNOTATIONS);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return result;
	}

	private void getAnnotations(String path, ArrayList<Annotation> result, List<Annotation> annotations, Session session, String query) {
		session.run( query, Values.parameters( "path", path ) )
			.stream()
			.parallel()
			.forEachOrdered(item -> {
				for (Annotation a : annotations) {
					if (a.getPath().contains(item.get("annotation").asString())) {
						result.add(a);
						break;
					}
				}
			});
	}
}
