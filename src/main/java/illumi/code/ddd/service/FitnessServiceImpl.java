package illumi.code.ddd.service;

import java.util.ArrayList;

import javax.inject.Inject;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Method;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.DDDFitness;

public class FitnessServiceImpl implements FitnessService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FitnessServiceImpl.class);
	        
    private StructureService structureService;
    
    public @Inject FitnessServiceImpl() {
    	// @Inject is needed
    }
    
    @Override
    public void setStructureService(StructureService structureService) {
    	this.structureService = structureService;
    }
    
	@Override
	public JSONArray getStructureWithFitness() {
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
				LOGGER.info("DDD:MODULE:{}", item.getName());
				DDDFitness fitness = new DDDFitness();
				
				if (isDomainModule(item)) {
					fitness = evaluateDomainModule(item);
				} else if (containsInfrastructure(item)) {
					fitness = evaluateInfrastructureModule(item);
				} else if (containsApplication(item)) {
					fitness = evaluateApplicationModule(item);
				} else if (isDDDRootStructure(item)) {
					fitness = new DDDFitness(1, 1);
				} else {
					fitness = new DDDFitness(1, 0);
				}
				item.setFitness(fitness);
			});
	}

	private DDDFitness evaluateInfrastructureModule(Package item) {
		DDDFitness fitness;
		fitness = new DDDFitness(2, item.getPath().contains(structureService.getPath() + "infrastructur") ? 2 : 1);
		return fitness;
	}

	private DDDFitness evaluateApplicationModule(Package item) {
		DDDFitness fitness;
		fitness = new DDDFitness(2, item.getPath().contains(structureService.getPath() + "application") ? 2 : 1);
		return fitness;
	}

	private boolean isDDDRootStructure(Package item) {
		return item.getName().contains(structureService.getPath() + "domain") 
					|| item.getName().contains(structureService.getPath() + "infrastructur") 
					|| item.getName().contains(structureService.getPath() + "application");
	}

	private DDDFitness evaluateDomainModule(Package item) {
		DDDFitness fitness;
		fitness = new DDDFitness(3, 1);
		if (item.getPath().contains(structureService.getPath() + "domain." + item.getName())) {
			fitness.incNumberOfFulfilledCriteria();
		}
		
		if (containsAggregateRoot(item)) {
			fitness.incNumberOfFulfilledCriteria();
		}
		return fitness;
	}

	private boolean isDomainModule(Package item) {
		return structureService.getDomains().contains(item.getName());
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
				switch(item.getType()) {
					case ENTITY:
						evaluateEntity(item);
						break;
					case VALUE_OBJECT:
						evaluateValueObject(item);
						break;
					case AGGREGATE_ROOT:
						evaluateAggregateRoot(item);
						break;
					case FACTORY:
						evaluateFactory(item);
						break;
					case REPOSITORY:
						evaluateRepository(item); 
						break;
					case SERVICE:
						evaluateService(item);
						break;
					case APPLICATION_SERVICE:
						evaluateApplicationService(item);
						break;
					case CONTROLLER:
					case INFRASTRUCTUR:
						evaluateInfrastructure(item);  
						break;
					default:
						item.setFitness(new DDDFitness());
				}
				
			});
	}
	
	private void evaluateAggregateRoot(Class aggregate) {
		LOGGER.info("DDD:AGGREGATE_ROOT:{}", aggregate.getName());
		evaluateEntity(aggregate);
		DDDFitness fitness = aggregate.getDDDFitness();
		
		fitness.addNumberOfCriteria(6);
		evaluateDomainStructure(aggregate, fitness);
		
	}

	private void evaluateDomainStructure(Class aggregate, DDDFitness fitness) {
		for (Artifact artifact : getDomainModule(aggregate.getDomain())) {
			if (isAggregateRootRepository(aggregate, artifact) 
				|| isAggregateRootFactory(aggregate, artifact) 
				|| isAggregateRootService(aggregate, artifact)) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
	}

	private boolean isAggregateRootRepository(Class aggregate, Artifact artifact) {
		return artifact.getType() == DDDType.REPOSITORY && artifact.getName().contains(aggregate.getName() + "Repository");
	}

	private boolean isAggregateRootFactory(Class aggregate, Artifact artifact) {
		return artifact.getType() == DDDType.FACTORY && artifact.getName().contains(aggregate.getName() + "Factory");
	}

	private boolean isAggregateRootService(Class aggregate, Artifact artifact) {
		return artifact.getType() == DDDType.SERVICE && artifact.getName().contains(aggregate.getName());
	}

	private ArrayList<Artifact> getDomainModule(String domain) {
		for (Package module : structureService.getPackages()) {
			if (module.getName().contains(domain)) {
				return (ArrayList<Artifact>) module.getConataints();
			}
		}
		return new ArrayList<>();
	}

	private void evaluateEntity(Class entity) {
		LOGGER.info("DDD:ENTITY:{}", entity.getName());
		DDDFitness fitness = new DDDFitness();
		
		// Must have criteria of Entity: ID, equals() and hashCode()
		fitness.addNumberOfCriteria(3);
		
		evaluateEntityFields(entity, fitness);
		
		evaluateEntityMethods(entity, fitness);
		
		evaluateSuperClass(entity.getSuperClass(), fitness);
		
		entity.setFitness(fitness);
	}

	private void evaluateEntityMethods(Class entity, DDDFitness fitness) {
		for (Method method : entity.getMethods()) {
			if (isNeededMethod(method)) {
				fitness.incNumberOfFulfilledCriteria();
			} 
		}
	}

	private void evaluateEntityFields(Class entity, DDDFitness fitness) {
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
	}

	private void evaluateSuperClass(Class item, DDDFitness fitness) {
		if (item != null) {
			for (Field field : item.getFields()) {
				if (isFieldAnId(field)) {
					fitness.incNumberOfFulfilledCriteria();
					break;
				}
			}
			
			evaluateEntityMethods(item, fitness);
			
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
		LOGGER.info("DDD:VALUE_OBJECT:{}", valueObject.getName());
		// Must have criteria of Entity: no ID
		DDDFitness fitness = new DDDFitness(1, 1);
		
		evaluateValueObjectFields(valueObject, fitness);
		
		evaluateValueObjectMethods(valueObject, fitness);
		
		evaluateSuperClass(valueObject.getSuperClass(), fitness);
		
		valueObject.setFitness(fitness);
	}

	private void evaluateValueObjectMethods(Class valueObject, DDDFitness fitness) {
		for (Method method : valueObject.getMethods()) {
			if (method.getName().startsWith("set")) {
				fitness.incNumberOfFulfilledCriteria();
				if (isMethodImmutable(method, valueObject)) {
					fitness.incNumberOfFulfilledCriteria();
				} 
			} else if (method.getName().startsWith("get")) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
	}

	private void evaluateValueObjectFields(Class valueObject, DDDFitness fitness) {
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
	}
	
	private boolean isMethodImmutable(Method method, Class artifact) {
		return method.getSignature().split(" ")[0].contains(artifact.getPath());
	}

	private void evaluateRepository(Class repository) {
		LOGGER.info("DDD:REPOSITORY:{}", repository.getName());
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
		
		findCounter = evaluateRepositoryMethods(repository, fitness, findCounter);
		
		if (findCounter > 1) {
			fitness.addNumberOfCriteria(findCounter-1);
		}
		
		repository.setFitness(fitness);
	}

	private int evaluateRepositoryMethods(Class repository, DDDFitness fitness, int findCounter) {
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
		return findCounter;
	}
	
	private void evaluateFactory(Class factory) {
		LOGGER.info("DDD:FACTORY:{}", factory.getName());
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
		LOGGER.info("DDD:SERVICE:{}", service.getName());
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

	private void evaluateApplicationService(Class appService) {
		LOGGER.info("DDD:APPLICATION_SERVICE:{}", appService.getName());
		appService.setFitness(new DDDFitness(1, appService.getPath().contains("application.") ? 1 : 0));
	}

	private void evaluateInfrastructure(Class infra) {
		LOGGER.info("DDD:INFRASTRUCTUR:{}", infra.getName());
		infra.setFitness(new DDDFitness(1, infra.getPath().contains("infrastructure.") ? 1 : 0));
	}
	
	private void evaluateInterfaces() {
		LOGGER.info("Evaluation of Interfaces");
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(item -> {
				if (item.getType() == DDDType.FACTORY) {
					LOGGER.info("DDD:FACTORY:{}", item.getName());
					evaluateFactory(item);
				} else if (item.getType() == DDDType.REPOSITORY) {
					LOGGER.info("DDD:REPOSITORY:{}", item.getName());
					evaluateRepository(item);
				} else  {
					LOGGER.info("DDD:SERVICE:{}", item.getName());
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
		
		findCounter = evaluateRepositoryMethods(repository, fitness, findCounter);
		
		if (findCounter > 1) {
			fitness.addNumberOfCriteria(findCounter-1);
		}
		
		repository.setFitness(fitness);
	}

	private int evaluateRepositoryMethods(Interface repository, DDDFitness fitness, int findCounter) {
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
		return findCounter;
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
				LOGGER.info("DDD:INFRASTRUCTUR:{}", item.getName());
				item.setFitness(new DDDFitness(1, item.getPath().contains("infrastructure.") ? 1 : 0));
			});
	}
}
