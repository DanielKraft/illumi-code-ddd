package illumi.code.ddd.service;

import java.util.ArrayList;

import javax.inject.Inject;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.Package;
import illumi.code.ddd.model.Artifact;
import illumi.code.ddd.model.Class;
import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.Interface;
import illumi.code.ddd.model.Method;
import illumi.code.ddd.model.Field;

public class FitnessServiceImpl implements FitnessService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyseServiceImpl.class);
	        
    private StructureService structureService;
    
    public @Inject FitnessServiceImpl() { }
    
    public void setStructureService(StructureService structureService) {
    	this.structureService = structureService;
    }
    
	@Override
	public JSONArray getMetrics() {
		evaluateModules();
		evaluateClasses();
		evaluateInterfaces();
		evaluateAnnotations();
		return structureService.getJOSN();
	}
	
	private void evaluateModules() {
		LOGGER.info("Evaluation of Modules");
		structureService.getPackages().stream()
			.parallel()
			.forEach(item -> {
				LOGGER.info("DDD:MODULE:" + item.getName());
				DDDFitness fitness = new DDDFitness();
				
				if (structureService.getDomains().contains(item.getName())) {
					fitness = new DDDFitness(3, 1);
					if (item.getPath().contains(structureService.getPath() + "domain." + item.getName())) {
						fitness.incNumberOfFulfilledCriteria();
					}
					
					if (containsAggregateRoot(item)) {
						fitness.incNumberOfFulfilledCriteria();
					}
				} else if (containsInfrastructure(item)) {
					fitness = new DDDFitness(2, item.getPath().contains(structureService.getPath() + "infrastructur") ? 2 : 1);
				} else if (containsApplication(item)) {
					fitness = new DDDFitness(2, item.getPath().contains(structureService.getPath() + "application") ? 2 : 1);
				} else if (item.getName().contains(structureService.getPath() + "domain") 
							|| item.getName().contains(structureService.getPath() + "infrastructur") 
							|| item.getName().contains(structureService.getPath() + "application")) {
					fitness = new DDDFitness(1, 1);
				} else {
					fitness = new DDDFitness(1, 0);
				}
				item.setFitness(fitness);
			});
	}

	private boolean containsAggregateRoot(Package module) {
		for (Artifact artifact : module.getConataints()) {
			if (artifact.getType() == DDDType.AGGREGATE_ROOT) {
				return true;
			}
		}
		return false;
	}

	private boolean containsInfrastructure(Package module) {
		for (Artifact artifact : module.getConataints()) {
			if (artifact.getType() != DDDType.MODULE 
				&& (artifact.getType() != DDDType.INFRASTRUCTUR || artifact.getType() != DDDType.CONTROLLER)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean containsApplication(Package module) {
		for (Artifact artifact : module.getConataints()) {
			if (artifact.getType() != DDDType.MODULE && artifact.getType() != DDDType.APPLICATION_SERVICE) {
				return false;
			}
		}
		return true;
	}

	private void evaluateClasses() {
		LOGGER.info("Evaluation of Classes");
		structureService.getClasses().stream()
			.parallel()
			.forEach(item -> {
				if (item.getType() == DDDType.ENTITY) {
					LOGGER.info("DDD:ENTITY:" + item.getName());
					evaluateEntity(item);
				} else if (item.getType() == DDDType.VALUE_OBJECT) {
					LOGGER.info("DDD:VALUE_OBJECT:" + item.getName());
					evaluateValueObject(item);
				} else if (item.getType() == DDDType.AGGREGATE_ROOT) {
					LOGGER.info("DDD:AGGREGATE_ROOT:" + item.getName());
					evaluateAggregateRoot(item);
				} else if (item.getType() == DDDType.FACTORY) {
					LOGGER.info("DDD:FACTORY:" + item.getName());
					evaluateFactory(item);
				} else if (item.getType() == DDDType.REPOSITORY) {
					LOGGER.info("DDD:REPOSITORY:" + item.getName());
					evaluateRepository(item); 
				} else if (item.getType() == DDDType.SERVICE) {
					LOGGER.info("DDD:SERVICE:" + item.getName());
					evaluateService(item);
				} else if (item.getType() == DDDType.APPLICATION_SERVICE) {
					LOGGER.info("DDD:APPLICATION_SERVICE:" + item.getName());
					item.setFitness(new DDDFitness(1, item.getPath().contains("application.") ? 1 : 0));
				} else if (item.getType() == DDDType.CONTROLLER || item.getType() == DDDType.INFRASTRUCTUR) {
					LOGGER.info("DDD:INFRASTRUCTUR:" + item.getName());
					item.setFitness(new DDDFitness(1, item.getPath().contains("infrastructure.") ? 1 : 0));  
				} else {
					item.setFitness(new DDDFitness());
				}
			});
	}
	
	private void evaluateAggregateRoot(Class aggregate) {
		evaluateEntity(aggregate);
		DDDFitness fitness = aggregate.getDDDFitness();
		
		fitness.addNumberOfCriteria(6);
		for (Artifact artifact : getDomainModule(aggregate.getDomain())) {
			if (artifact.getType() == DDDType.REPOSITORY && artifact.getName().contains(aggregate.getName() + "Repository")) {
				fitness.incNumberOfFulfilledCriteria();
			} else if (artifact.getType() == DDDType.FACTORY && artifact.getName().contains(aggregate.getName() + "Factory")) {
				fitness.incNumberOfFulfilledCriteria();
			} else if (artifact.getType() == DDDType.SERVICE && artifact.getName().contains(aggregate.getName())) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		
	}

	private ArrayList<Artifact> getDomainModule(String domain) {
		for (Package module : structureService.getPackages()) {
			if (module.getName().contains(domain)) {
				return module.getConataints();
			}
		}
		return new ArrayList<>();
	}

	private void evaluateEntity(Class entity) {
		DDDFitness fitness = new DDDFitness();
		
		// Must have criteria of Entity: ID, equals() and hashCode()
		fitness.addNumberOfCriteria(3);
		
		for (Field field : entity.getFields()) {
			if (isFieldAnId(field)) {
				fitness.incNumberOfFulfilledCriteria();
			}
			
			// Is type of field Entity or Value Object?
			fitness.incNumberOfCriteria();
			if (field.getType().contains(structureService.getPath())) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		
		for (Method method : entity.getMethods()) {
			if (isNeededMethod(method)) {
				fitness.incNumberOfFulfilledCriteria();
			} 
		}
		
		evaluateSuperClass(entity.getSuperClass(), fitness);
		
		entity.setFitness(fitness);
	}

	private void evaluateSuperClass(Class item, DDDFitness fitness) {
		if (item != null) {
			for (Field field : item.getFields()) {
				if (isFieldAnId(field)) {
					fitness.incNumberOfFulfilledCriteria();
					break;
				}
			}
			
			for (Method method : item.getMethods()) {
				if (isNeededMethod(method)) {
					fitness.incNumberOfFulfilledCriteria();
				} 
			}
			
			evaluateSuperClass(item.getSuperClass(), fitness);
		}
		
	}
	
	private boolean isFieldAnId(Field field) {
		return field.getName().toUpperCase().endsWith("ID");
	}
	
	private boolean isNeededMethod(Method method) {
		// equals() or hashCode()? 
		return method.getName().equals("equals") || method.getName().equals("hashCode");
	}
	
	private void evaluateValueObject(Class valueObject) {
		// Must have criteria of Entity: no ID
		DDDFitness fitness = new DDDFitness(1, 1);
		
		for (Field field : valueObject.getFields()) {
			if (isFieldAnId(field)) {
				fitness.decNumberOfFulfilledCriteria();
			}
			
			// Is type of field Value Object or standard type?
			fitness.addNumberOfCriteria(4);
			if (field.getType().contains(structureService.getPath()) || field.getType().contains("java.lang.")) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		
		for (Method method : valueObject.getMethods()) {
			for (Field field : valueObject.getFields()) {
				if (method.getName().toUpperCase().contains(field.getName().toUpperCase())) {
					if (method.getName().startsWith("set")) {
						fitness.incNumberOfFulfilledCriteria();
						if (isMethodImmutable(method, valueObject)) {
							fitness.incNumberOfFulfilledCriteria();
						} 
					} else if (method.getName().startsWith("get")) {
						fitness.incNumberOfFulfilledCriteria();
					}
					break;
				}
			}
		}
		
		evaluateSuperClass(valueObject.getSuperClass(), fitness);
		
		valueObject.setFitness(fitness);
	}
	
	private boolean isMethodImmutable(Method method, Class artifact) {
		return method.getSignature().split(" ")[0].contains(artifact.getPath());
	}

	private void evaluateRepository(Class repository) {
		DDDFitness fitness = new DDDFitness(7, 0);
		
		if (repository.getName().endsWith("RepositoryImpl")) {
			fitness.incNumberOfFulfilledCriteria();
		}
		
		for (Interface i : repository.getInterfaces()) {
			if (i.getName().endsWith("Repository")) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		
		int findCounter = 0;
		
		for (Method method : repository.getMethods()) {
			if (method.getName().startsWith("findBy") || method.getName().startsWith("get")) {
				findCounter++;
				fitness.incNumberOfFulfilledCriteria();
			} else if (method.getName().startsWith("save") || method.getName().startsWith("add") || method.getName().startsWith("insert")) {
				fitness.incNumberOfFulfilledCriteria();
			} else if (method.getName().startsWith("delete") || method.getName().startsWith("remove")) {
				fitness.incNumberOfFulfilledCriteria();
			} else if (method.getName().startsWith("contains") || method.getName().startsWith("exists")) {
				fitness.incNumberOfFulfilledCriteria();
			} else if (method.getName().startsWith("update")) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		
		if (findCounter > 1) {
			fitness.addNumberOfCriteria(findCounter-1);
		}
		
		repository.setFitness(fitness);
	}
	
	private void evaluateFactory(Class factory) {
		DDDFitness fitness = new DDDFitness(4, 0);
		
		if (factory.getName().endsWith("FactoryImpl")) {
			fitness.incNumberOfFulfilledCriteria();
		}
		
		for (Interface i : factory.getInterfaces()) {
			if (i.getName().endsWith("Factory")) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		
		for (Field field : factory.getFields()) {
			if (field.getType().contains("Repository")) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
				
		for (Method method : factory.getMethods()) {
			if (method.getName().startsWith("create")) {
				fitness.incNumberOfFulfilledCriteria();
			} 
		}
		
		factory.setFitness(fitness);
	}
	
	private void evaluateService(Class service) {
		DDDFitness fitness = new DDDFitness(2, 0);
		
		if (service.getName().endsWith("Impl")) {
			fitness.incNumberOfFulfilledCriteria();
		}
		
		for (Interface i : service.getInterfaces()) {
			if (service.getName().startsWith(i.getName())) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		service.setFitness(fitness);
	}
	
	private void evaluateInterfaces() {
		LOGGER.info("Evaluation of Interfaces");
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(item -> {
				if (item.getType() == DDDType.FACTORY) {
					LOGGER.info("DDD:FACTORY:" + item.getName());
					evaluateFactory(item);
				} else if (item.getType() == DDDType.REPOSITORY) {
					LOGGER.info("DDD:REPOSITORY:" + item.getName());
					evaluateRepository(item);
				} else  {
					LOGGER.info("DDD:SERVICE:" + item.getName());
					item.setFitness(new DDDFitness());
				}
			});
	}
	
	private void evaluateRepository(Interface repository) {
		DDDFitness fitness = new DDDFitness(6, 0);
		
		if (repository.getName().endsWith("Repository")) {
			fitness.incNumberOfFulfilledCriteria();
		}
		
		int findCounter = 0;
		
		for (Method method : repository.getMethods()) {
			if (method.getName().startsWith("findBy") || method.getName().startsWith("get")) {
				findCounter++;
				fitness.incNumberOfFulfilledCriteria();
			} else if (method.getName().startsWith("save") || method.getName().startsWith("add") || method.getName().startsWith("insert")) {
				fitness.incNumberOfFulfilledCriteria();
			} else if (method.getName().startsWith("delete") || method.getName().startsWith("remove")) {
				fitness.incNumberOfFulfilledCriteria();
			} else if (method.getName().startsWith("contains") || method.getName().startsWith("exists")) {
				fitness.incNumberOfFulfilledCriteria();
			} else if (method.getName().startsWith("update")) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		
		if (findCounter > 1) {
			fitness.addNumberOfCriteria(findCounter-1);
		}
		
		repository.setFitness(fitness);
	}

	private void evaluateFactory(Interface factory) {
		DDDFitness fitness = new DDDFitness(3, 0);
		
		if (factory.getName().endsWith("Factory")) {
			fitness.incNumberOfFulfilledCriteria();
		}
		
		for (Field field : factory.getFields()) {
			if (field.getType().contains("Repository")) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
				
		for (Method method : factory.getMethods()) {
			if (method.getName().startsWith("create")) {
				fitness.incNumberOfFulfilledCriteria();
			} 
		}
		
		factory.setFitness(fitness);
	}

	private void evaluateAnnotations() {
		LOGGER.info("Evaluation of Annotations");
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(item -> {
				LOGGER.info("DDD:INFRASTRUCTUR:" + item.getName());
				item.setFitness(new DDDFitness(1, item.getPath().contains("infrastructure.") ? 1 : 0));
			});
	}
}
