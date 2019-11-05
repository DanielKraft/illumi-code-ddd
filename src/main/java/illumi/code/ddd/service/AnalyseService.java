package illumi.code.ddd.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import illumi.code.ddd.model.Artifact;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.Package;
import illumi.code.ddd.model.Class;
import illumi.code.ddd.model.Interface;
import illumi.code.ddd.model.Enum;
import illumi.code.ddd.model.Annotation;

public class AnalyseService {
	private static final String QUERY_ARTIFACT = "MATCH (root:Package)-[:CONTAINS]->(artifact) "
			 									+ "WHERE root.fqn={path} AND (artifact:Package OR artifact:Class OR artifact:Interface OR artifact:Enum OR artifact:Annotation) "
			 									+ "RETURN DISTINCT artifact.name as name, artifact.fqn as path, labels(artifact) as types";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyseService.class);
	
    private Driver driver;
    
    private ArrayList<Class> classes;
    private ArrayList<Interface> interfaces;
    private ArrayList<Enum> enums;
    private ArrayList<Annotation> annotations;
    
    
    public @Inject AnalyseService(Driver driver) { 
    	this.driver = driver;
    	this.classes = new ArrayList<>();
    	this.interfaces = new ArrayList<>();
    	this.enums = new ArrayList<>();
    	this.annotations = new ArrayList<>();
    }
	
    public ArrayList<Artifact> analyzeStructure(String path) {
    	ArrayList<Artifact> artifacts = getArtifacts(path);
    	analyzeClasses();
    	analyzeInterfaces();
		return artifacts;
    }
    
    private ArrayList<Artifact> getArtifacts(String path) {
    	try ( Session session = driver.session() ) {
    		StatementResult result = session.run( QUERY_ARTIFACT, Values.parameters( "path", path ));
        	return convertResultToArtifacts(result);
    	} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    	return new ArrayList<>();
	}

	private ArrayList<Artifact> convertResultToArtifacts(StatementResult result) {
		ArrayList<Artifact> artifacts = new ArrayList<>();
		result.stream()
			.parallel()
			.forEach(item -> {
				List<Object> types = item.get( "types" ).asList();
				if (types.contains("Package")) {
					Package newPackage = new Package(item);
					newPackage.setConataints(getArtifacts(newPackage.getPath()));
					artifacts.add(newPackage);
				} else if (types.contains("Class")) {
					Class newClass = new Class(item);
					classes.add(newClass);
					artifacts.add(newClass);
				} else if (types.contains("Interface")) {
					Interface newInterface = new Interface(item);
					interfaces.add(newInterface);
					artifacts.add(newInterface);
				} else if (types.contains("Enum")) {
					Enum newEnum = new Enum(item);
					enums.add(newEnum);
					artifacts.add(newEnum);
				} else if (types.contains("Annotation")) {
					Annotation newAnnotation = new Annotation(item);
					annotations.add(newAnnotation);
					artifacts.add(newAnnotation);
				}
			});
		return artifacts;
	}

	private void analyzeClasses() {
		classes.stream()
			.parallel()
			.forEach(item -> {
				item.setFields(driver);
				item.setMethods(driver);
				item.setSuperClass(driver, classes);
				item.setImplInterfaces(driver, interfaces);
			});
	}
	
	private void analyzeInterfaces() {
		interfaces.stream()
			.parallel()
			.forEach(item -> {
				item.setFields(driver);
				item.setMethods(driver);
				item.setImplInterfaces(driver, interfaces);
			});
	}
}
