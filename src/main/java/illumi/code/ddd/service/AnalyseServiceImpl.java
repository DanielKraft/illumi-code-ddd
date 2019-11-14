package illumi.code.ddd.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
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
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Method;
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
			.forEach(item -> {
				if (isInfrastructur(item)) {
					item.setType(DDDType.INFRASTRUCTUR);
				} else if (item.getType() == null) {
					if (isValueObject(item)) {
						item.setType(DDDType.VALUE_OBJECT);
					} else if (isEntity(item)) {
						item.setType(DDDType.ENTITY);
					} else if (isService(item)) { 
						item.setType(DDDType.SERVICE);
					} else {
						item.setType(DDDType.INFRASTRUCTUR);
					}
				} 
			});

	}
	
	private boolean isInfrastructur(Artifact artifact) {
		return artifact.getName().toUpperCase().contains("JPA") || artifact.getName().toUpperCase().contains("CRUD");
	}

	private boolean isEntity(Class artifact) {
		
		for (Field field : artifact.getFields()) {
			if (isConstant(field)) {
				return false;
			}
		}
		return !artifact.getFields().isEmpty() && !containsEntityName(artifact) && conatiantsGetterSetter(artifact);
	}

	private boolean isValueObject(Class artifact) {
		int ctr = 0;
		for (Field field : artifact.getFields()) {
			if (isConstant(field)) {
				return false;
			} else if (field.getType().startsWith("java.lang.") || field.getType().contains(structureService.getPath())) {
				ctr++;
			}
		}
		return !artifact.getFields().isEmpty() && ctr == artifact.getFields().size() && conatiantsGetterSetter(artifact);
	}

	private boolean isConstant(Field field) {
		return StringUtils.isAllUpperCase(field.getName());
	}
	
	private boolean isService(Class item) {
		for (Field field : item.getFields()) {
			if (field.getType().contains("Repository")) {
				return true;
			}
		}
		return containsEntityName(item);
	}
	
	private boolean conatiantsGetterSetter(Class item) {
		for (Method method : item.getMethods()) {
			if (method.getName().startsWith("get") || method.getName().startsWith("set")) {
				return true;
			}
		}
		return containtsUnconventionalGetter(item);
	}

	private boolean containtsUnconventionalGetter(Class item) {
		for (Method method : item.getMethods()) {
			for (Field field : item.getFields()) {
				if (method.getSignature().startsWith(field.getType())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean containsEntityName(Class item) {
		for (Class artifact : structureService.getClasses()) {
			if (item != artifact && item.getName().contains(artifact.getName()) && !item.getName().equals(artifact.getName() + "s")) {
				return true;
			}
		}
		return false;
	}
	
	private void analyzeInterfaces() {
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(item -> {
				item.setFields(driver);
				item.setMethods(driver);
				item.setImplInterfaces(driver, structureService.getInterfaces());
				item.setAnnotations(driver, structureService.getAnnotations());
				
				if (isInfrastructur(item)) {
					item.setType(DDDType.INFRASTRUCTUR);
				}  
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
				if (item.getType() == DDDType.ENTITY 
					|| item.getType() == DDDType.VALUE_OBJECT 
					|| item.getType() == DDDType.SERVICE 
					|| item.getType() == DDDType.REPOSITORY 
					|| item.getType() == DDDType.FACTORY) {
					addDomain(item);
				}
			});
		
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(item -> {
				if (item.getType() == DDDType.SERVICE 
					|| item.getType() == DDDType.REPOSITORY 
					|| item.getType() == DDDType.FACTORY) {
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
    
    public void analyseDomains() {
    	structureService.getPackages().stream()
    		.parallel()
    		.forEach(item -> {
    			if (isDomain(item)) {
    				ArrayList<Artifact> candidates = getAggregateRootCandidates((ArrayList<Artifact>) item.getConataints());
    				if (candidates.size() == 1) {
    					candidates.get(0).setType(DDDType.AGGREGATE_ROOT);
    				} else {
    					for (Artifact artifact : candidates) {
							if (structureService.getDomains().contains(artifact.getName().toLowerCase())) {
								artifact.setType(DDDType.AGGREGATE_ROOT);
							}
						}
    				}
    			}
    		});
    }

	private boolean isDomain(Package module) {
		return structureService.getDomains().contains(module.getName());
	}

	private ArrayList<Artifact> getEntities(ArrayList<Artifact> conataints) {
		ArrayList<Artifact> entities = new ArrayList<>();
		
		for (Artifact artifact : conataints) {
			if (artifact.isTypeOf(DDDType.ENTITY)) {
				entities.add(artifact);
			}
		}
		
		return entities;
	}
	
	private ArrayList<Artifact> getAggregateRootCandidates(ArrayList<Artifact> conataints) {
		ArrayList<Artifact> entities = getEntities((ArrayList<Artifact>) conataints);
		if (!entities.isEmpty()) {
			ArrayList<Integer> dependencies = getDependencies(entities);
			return getEntityWithMinmalDependencies(entities, dependencies);
		} else {
			return new ArrayList<>();
		}
	}
	
	private ArrayList<Integer> getDependencies(ArrayList<Artifact> entities) {
		ArrayList<Integer> dependencies = new ArrayList<>();
		entities.stream()
			.parallel()
			.forEachOrdered(artifact -> dependencies.add(countDependencies(entities, artifact)));
		return dependencies;
	}

	private Integer countDependencies(ArrayList<Artifact> entities, Artifact artifact) {
		int ctr = 0;
		for (Artifact entity : entities) {
			if (entity != artifact) {
				for (Field field : ((Class) entity).getFields()) {
					if (field.getType().equals(artifact.getPath())) {
						ctr++;
					}
				}
			}
		}
		return ctr;
	}

	private ArrayList<Artifact> getEntityWithMinmalDependencies(ArrayList<Artifact> entities, ArrayList<Integer> dependencies) {
		ArrayList<Artifact> result = new ArrayList<>();
		result.add(entities.get(0));
		
		int highscore = dependencies.get(0);
		for (int i = 1; i < dependencies.size(); i++) {			
			if (dependencies.get(i) == highscore) {
				result.add(entities.get(i));
			} else if (dependencies.get(i) < highscore) {
				highscore = dependencies.get(i);
				result = new ArrayList<>();
				result.add(entities.get(i));
			}
		}
		return result;
	}

	private void findEvents() {
		structureService.getClasses().stream()
		.parallel()
		.forEach(item -> {
			switch(item.getType()) {
				case ENTITY:
				case AGGREGATE_ROOT:
				case VALUE_OBJECT:
					if (isDomainEvent(item)) {
						item.setType(DDDType.DOMAIN_EVENT);
					}
					break;
				default:
					break;
			}
		});
	}
	
	private boolean isDomainEvent(Class artifact) {		
		boolean containtsTimestamp = false;
		boolean containtsIdentity = false;
		
		for (Field field : artifact.getFields()) {
			if (field.getName().contains("time") 
				|| field.getName().contains("date") 
				|| field.getType().contains("java.time.")) {
				containtsTimestamp = true;
				
			} else if (!field.getName().equalsIgnoreCase("id") 
					&& field.getName().toUpperCase().endsWith("ID")) {
				containtsIdentity = true;
			}
		}
		
		return containtsTimestamp && containtsIdentity;
	}
}
