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

	private static final String QUERY_DEPENDENCIES = "MATCH (artifact:Java)-[:DEPENDS_ON]->(dependency:Java) WHERE artifact.fqn={path} AND dependency.fqn CONTAINS {rootPath} RETURN dependency.fqn as dependencies";

	private static final Logger LOGGER = LoggerFactory.getLogger(JavaArtifactService.class);


	private JavaArtifactService() {
		throw new IllegalStateException("JavaArtifactService class");
	}

	public static List<String> getDependencies(String rootPath, String path, Driver driver) {
		try ( Session session = driver.session() ) {
			Map<String, Object> params = new HashMap<>();
			params.put("rootPath", rootPath);
			params.put("path", path);
			StatementResult result = session.run( QUERY_DEPENDENCIES, params);

			ArrayList<String> dependencies = new ArrayList<>();

			result.stream()
				.parallel()
				.forEach(item -> dependencies.add(item.get("dependencies").asString()));

			return dependencies;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	public static List<Field> getFields(String path, Driver driver, String query) {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( query, Values.parameters( "path", path ) );
        	return convertResultToFields(result);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    	return new ArrayList<>();
    }

	private static ArrayList<Field> convertResultToFields(StatementResult result) {
		ArrayList<Field> fields = new ArrayList<>();
		result.stream()
			.parallel()
			.forEach(item -> {
				if (!item.get( "name" ).isNull()) {
					fields.add(new Field( item ));
		        }
			});
		return fields;
	}
	
	public static List<Method> getMethods(String path, Driver driver, String query) {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( query, Values.parameters( "path", path ) );
    		return convertResultToMethods(result);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    	return new ArrayList<>();
    }
	
	private static ArrayList<Method> convertResultToMethods(StatementResult result) {
		ArrayList<Method> methods = new ArrayList<>();
		result.stream()
			.parallel()
			.forEach(item -> {
				if (!item.get( "name" ).isNull()) {
			        Method newMethod = new Method( item );
			        methods.add(newMethod);
		        }
			});
		return methods;
	}
	
	public static List<Interface> getImplInterfaces(String path, Driver driver, String query, List<Interface> interfaces) {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( query, Values.parameters( "path", path ) );
    		return convertResultToInterface(result, interfaces);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    	return new ArrayList<>();
    }
	
	private static ArrayList<Interface> convertResultToInterface(StatementResult result, List<Interface> interfaces) {		
		ArrayList<Interface> implInterfaces = new ArrayList<>();
		result.stream()
			.parallel()
			.forEach(item -> {
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
	
	public static Class getSuperClass(String path, Driver driver, String query, List<Class> classes) {
		try ( Session session = driver.session() ) {
    		StatementResult result = session.run( query, Values.parameters( "path", path ));
							    		
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
	
	public static List<Annotation> getAnnotations(String path, Driver driver, String queryParent, String queryChild, List<Annotation> annotations) {
		ArrayList<Annotation> result = new ArrayList<>();
		try ( Session session = driver.session() ) {
			getAnnotations(path, result, annotations, session, queryParent);
    		
			getAnnotations(path, result, annotations, session, queryChild);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return result;
	}

	private static void getAnnotations(String path, ArrayList<Annotation> result, List<Annotation> annotations, Session session, String query) {
		session.run( query, Values.parameters( "path", path ) )
			.stream()
			.parallel()
			.forEach(item -> {
				for (Annotation a : annotations) {
					if (a.getPath().contains(item.get("annotation").asString())) {
						result.add(a);
						break;
					}
				}
			});
	}
}
