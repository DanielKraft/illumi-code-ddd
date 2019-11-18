package illumi.code.ddd.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.json.JSONArray;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Annotation;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Enum;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Package;

public class AnalyseServiceImpl implements AnalyseService {
	
	private static final String QUERY_ARTIFACT = "MATCH (root:Package)-[:CONTAINS]->(artifact) WHERE root.fqn={path} AND (artifact:Package OR artifact:Class OR artifact:Interface OR artifact:Enum OR artifact:Annotation) RETURN DISTINCT artifact.name as name, artifact.fqn as path, labels(artifact) as types";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyseServiceImpl.class);
	
    private Driver driver;
    
    private StructureService structureService; 
    
    public @Inject AnalyseServiceImpl(Driver driver) { 
    	this.driver = driver;
    	this.structureService = new StructureService();
    }
	
    @Override
    public void setStructureService(StructureService structureService) {
    	this.structureService = structureService;
    }

    @Override
	public JSONArray analyzeStructure(String path) {
    	structureService.setPath(path);
    	structureService.setStructure(getArtifacts(path));
    	analyzeClasses();
    	analyzeInterfaces();
    	analyzeEnums();
    	analyzeAnnotations();
    	
    	setupDomains();
    	analyseDomains();
    	
    	findEvents();
    	return structureService.getJOSN();
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
					structureService.addPackage(newPackage);
					artifacts.add(newPackage);
				} else if (types.contains("Class")) {
					Class newClass = new Class(item);
					structureService.addClasses(newClass);
					artifacts.add(newClass);
				} else if (types.contains("Interface")) {
					Interface newInterface = new Interface(item);
					structureService.addInterfaces(newInterface);
					artifacts.add(newInterface);
				} else if (types.contains("Enum")) {
					Enum newEnum = new Enum(item);
					structureService.addEnums(newEnum);
					artifacts.add(newEnum);
				} else {
					Annotation newAnnotation = new Annotation(item);
					structureService.addAnnotations(newAnnotation);
					artifacts.add(newAnnotation);
				}
			});
		return artifacts;
	}

	private void analyzeClasses() {
		structureService.getClasses().stream()
			.parallel()
			.forEach(item -> {
				item.setFields(driver);
				item.setMethods(driver);
				item.setSuperClass(driver, structureService.getClasses());
				item.setImplInterfaces(driver, structureService.getInterfaces());
				item.setAnnotations(driver, structureService.getAnnotations());
				
				if (item.getSuperClass() != null) {
					item.setType(DDDType.ENTITY);
					item.getSuperClass().setType(DDDType.ENTITY);
				}
			});
		
		structureService.getClasses().stream()
			.parallel()
			.forEach(item -> item.setType(structureService));
	}
	
	private void analyzeInterfaces() {
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(item -> {
				item.setFields(driver);
				item.setMethods(driver);
				item.setImplInterfaces(driver, structureService.getInterfaces());
				item.setAnnotations(driver, structureService.getAnnotations());
				
				item.setType();
			});
	}
	
	private void analyzeEnums() {
		structureService.getEnums().stream()
			.parallel()
			.forEach(item -> {
				item.setFields(driver);
				item.setAnnotations(driver, structureService.getAnnotations());
			});
	}
	
	private void analyzeAnnotations() {
		structureService.getAnnotations().stream()
			.parallel()
			.forEach(item -> {
				item.setFields(driver);
				item.setMethods(driver);
				item.setAnnotations(driver, structureService.getAnnotations());
			});
	}

    private void setupDomains() {
		structureService.getClasses().stream()
			.parallel()
			.forEach(item -> {
				if (item.isTypeOf(DDDType.ENTITY)
					|| item.isTypeOf(DDDType.VALUE_OBJECT)
					|| item.isTypeOf(DDDType.SERVICE) 
					|| item.isTypeOf(DDDType.REPOSITORY) 
					|| item.isTypeOf(DDDType.FACTORY)) {
					addDomain(item);
				}
			});
		
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(item -> {
				if (item.isTypeOf(DDDType.SERVICE)
					|| item.isTypeOf(DDDType.REPOSITORY)
					|| item.isTypeOf(DDDType.FACTORY)) {
					addDomain(item);
				}
			});
		
		structureService.getEnums().stream()
			.parallel()
			.forEach(this::addDomain);
	}

	private void addDomain(Artifact item) {
		String[] split = item.getPath().split("[.]");
		String domain = split[split.length-2];
		structureService.addDomain(domain);
		item.setDomain(domain);
	}
    
    private void analyseDomains() {
    	structureService.getPackages().stream()
    		.parallel()
    		.forEach(item -> item.setAggregateRoot(structureService));
    }

	private void findEvents() {
		structureService.getClasses().stream()
			.parallel()
			.forEach(Class::setDomainEvent);
	}
}
