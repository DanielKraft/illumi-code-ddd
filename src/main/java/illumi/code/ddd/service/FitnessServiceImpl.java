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
import illumi.code.ddd.model.DDDIssueType;

public class FitnessServiceImpl implements FitnessService {
	
	private static final String FACTORY = "Factory";

	private static final String REPOSITORY = "Repository";

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
				} else {
					fitness = new DDDFitness().addFailedCriteria(DDDIssueType.INFO, "The module '" + item.getName() + "' is no DDD-Module.");
				}
				item.setFitness(fitness);
			});
	}

	private boolean isDomainModule(Package item) {
		return structureService.getDomains().contains(item.getName());
	}

	private DDDFitness evaluateDomainModule(Package item) {
		DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);
		if (item.getPath().contains(structureService.getPath() + "domain." + item.getName())) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The module '" + item.getName() + "' is not a submodule of the module 'domain'.");
		}
		
		if (containsAggregateRoot(item)) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "The module '" + item.getName() + "' does not contain an Aggregate Root.");
		}
		return fitness;
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
			if (artifact.getType() == DDDType.INFRASTRUCTUR || artifact.getType() == DDDType.CONTROLLER) {
				return true;
			}
		}
		return false;
	}

	private DDDFitness evaluateInfrastructureModule(Package item) {
		DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);
		
		if (item.getPath().contains(structureService.getPath() + "infrastructur")) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The module '" + item.getName() + "' is not an infrastructure module.");
		}
		
		if (containsOnlyInfrastructure(item)) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "The module '" + item.getName() + "' dose not only containts infrastructure artifacts.");
		}
		
		return fitness;
	}
	
	private boolean containsOnlyInfrastructure(Package module) {
		for (Artifact artifact : module.getConataints()) {
			if (artifact.getType() != DDDType.INFRASTRUCTUR && artifact.getType() != DDDType.CONTROLLER) {
				return false;
			}
		}
		return true;
	}
	
	private boolean containsApplication(Package module) {
		for (Artifact artifact : module.getConataints()) {
			if (artifact.getType() == DDDType.APPLICATION_SERVICE) {
				return true;
			}
		}
		return false;
	}

	private DDDFitness evaluateApplicationModule(Package item) {
		DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);
		
		if (item.getPath().contains(structureService.getPath() + "application")) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The module '" + item.getName() + "' is not an application module.");
		}
		
		if (containsOnlyApplication(item)) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "The module '" + item.getName() + "' dose not only containts infrastructure artifacts.");
		}
		
		return fitness;
	}

	private boolean containsOnlyApplication(Package module) {
		for (Artifact artifact : module.getConataints()) {
			if (artifact.getType() != DDDType.APPLICATION_SERVICE) {
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
					case REPOSITORY:
						evaluateRepository(item); 
						break;
					case FACTORY:
						evaluateFactory(item);
						break;
					case SERVICE:
						evaluateService(item);
						break;
					case DOMAIN_EVENT:
						evaluateDomainEvent(item);
						break;
					case APPLICATION_SERVICE:
						evaluateApplicationService(item);
						break;
					case CONTROLLER:
					case INFRASTRUCTUR:
					default:
						evaluateInfrastructure(item); 
				}
				
			});
	}

	private void evaluateEntity(Class entity) {
		LOGGER.info("DDD:ENTITY:{}", entity.getName());
		DDDFitness fitness = new DDDFitness();
				
		evaluateEntityFields(entity, fitness);
		
		evaluateEntityMethods(entity, fitness);
		
		evaluateSuperClass(entity.getSuperClass(), fitness);
		
		entity.setFitness(fitness);
	}

	private void evaluateEntityFields(Class entity, DDDFitness fitness) {
		boolean containtsId = false;
		for (Field field : entity.getFields()) {
			if (isFieldAnId(field)) {
				containtsId = true;
			}
			
			// Is type of field Entity or Value Object?
			if (field.getType().contains(structureService.getPath())) {
				fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
			} else {
				fitness.addFailedCriteria(DDDIssueType.MINOR, "The Field '" + field.getName() + "' of the Entity '" + entity.getName() + "' is not a type of an Entity or a Value Object");
			}
		}
		
		if (containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else if (entity.getSuperClass() == null) {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "The Entity '" + entity.getName() + "' does not containts an ID.");
		}
	}
	
	private boolean isFieldAnId(Field field) {
		return field.getName().toUpperCase().endsWith("ID");
	}

	private void evaluateEntityMethods(Class entity, DDDFitness fitness) {
		int ctr = 0;
		for (Method method : entity.getMethods()) {
			if (isNeededMethod(method)) {
				ctr++;
			} 
		}
		if (ctr >= 2) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else if (entity.getSuperClass() == null) {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Entity '" + entity.getName() + "' does not containts all needed methods (equals/hashCode).");
		}
	}
	
	private boolean isNeededMethod(Method method) {
		// equals() or hashCode()? 
		return method.getName().equals("equals") || method.getName().equals("hashCode");
	}

	private void evaluateSuperClass(Class item, DDDFitness fitness) {
		if (item != null) {
			boolean containtsId = false;
			
			for (Field field : item.getFields()) {
				if (isFieldAnId(field)) {
					containtsId = true;
					break;
				}
			}
			
			if (containtsId) {
				fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
			} else if (item.getSuperClass() == null) {
				fitness.addFailedCriteria(DDDIssueType.MAJOR, "The Entity '" + item.getName() + "' does not containts an ID.");
			}
			
			evaluateEntityMethods(item, fitness);
			
			evaluateSuperClass(item.getSuperClass(), fitness);
		}
		
	}
	
	private void evaluateValueObject(Class valueObject) {
		LOGGER.info("DDD:VALUE_OBJECT:{}", valueObject.getName());
		// Must have criteria of Entity: no ID
		DDDFitness fitness = new DDDFitness();
		
		evaluateValueObjectFields(valueObject, fitness);
				
		valueObject.setFitness(fitness);
	}

	private void evaluateValueObjectFields(Class valueObject, DDDFitness fitness) {
		boolean containtsId = false;
		for (Field field : valueObject.getFields()) {
			if (isFieldAnId(field)) {
				containtsId = true;
			}
			
			// Is type of field Value Object or standard type?
			if (field.getType().contains(structureService.getPath()) || field.getType().contains("java.lang.")) {
				fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
			} else {
				fitness.addFailedCriteria(DDDIssueType.MAJOR, "The Field '" + field.getName() + "' of Value Object '" + valueObject.getName() + "' is not a Value Object or a standard type.");
			}
			
			// Has the field a getter and an immutable setter?
			evaluateValueObjectMethodOfField(valueObject, field, fitness);
		}
		
		if (!containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "The Value Object '" + valueObject.getName() + "' containts an ID.");
		}
	}
	
	private void evaluateValueObjectMethodOfField(Class valueObject, Field field, DDDFitness fitness) {
		boolean containtsSetter = false;
		boolean containtsGetter = false;
		for (Method method : valueObject.getMethods()) {
			if (method.getName().toUpperCase().equals("SET" + field.getName().toUpperCase())) {
				containtsSetter = true;
				if (isMethodImmutable(method, valueObject)) {
					fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
				} else {
					fitness.addFailedCriteria(DDDIssueType.MAJOR, "The method '" + method.getName() + "(...)' is not immutable.");
				}
			} else if (method.getName().toUpperCase().equals("GET" + field.getName().toUpperCase())) {
				containtsGetter = true;
			}
		}
		
		if (containtsSetter) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The field '" + field.getName() + "' does not have a setter.");
		}
		
		if (containtsGetter) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The field '" + field.getName() + "' does not have a Getter.");
		}
	}
	
	private boolean isMethodImmutable(Method method, Class artifact) {
		return method.getSignature().split(" ")[0].contains(artifact.getPath());
	}
	
	private void evaluateAggregateRoot(Class aggregate) {
		LOGGER.info("DDD:AGGREGATE_ROOT:{}", aggregate.getName());
		evaluateEntity(aggregate);
		DDDFitness fitness = aggregate.getDDDFitness();
		
		evaluateDomainStructure(aggregate, fitness);
		
	}

	private void evaluateDomainStructure(Class aggregate, DDDFitness fitness) {
		boolean repoAvailable = false;
		boolean factoryAvailable = false;
		boolean serviceAvailable = false;
		
		for (Artifact artifact : getDomainModule(aggregate.getDomain())) {
			if (isAggregateRootRepository(aggregate, artifact)) {
				repoAvailable = true;
			} else if(isAggregateRootFactory(aggregate, artifact) ) {
				factoryAvailable = true;
			} else if(isAggregateRootService(aggregate, artifact)) {
				serviceAvailable = true;
			}
		}
		
		if (repoAvailable) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "No repository of the aggregate root '" + aggregate.getName() + "' is available");
		}
		
		if (factoryAvailable) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "No factory of the aggregate root '" + aggregate.getName() + "' is available");
		}
		
		if (serviceAvailable) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "No service of the aggregate root '" + aggregate.getName() + "' is available");
		}
	}

	private boolean isAggregateRootRepository(Class aggregate, Artifact artifact) {
		return artifact.getType() == DDDType.REPOSITORY && artifact.getName().contains(aggregate.getName() + REPOSITORY);
	}

	private boolean isAggregateRootFactory(Class aggregate, Artifact artifact) {
		return artifact.getType() == DDDType.FACTORY && artifact.getName().contains(aggregate.getName() + FACTORY);
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

	private void evaluateRepository(Class repository) {
		LOGGER.info("DDD:REPOSITORY:{}", repository.getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateRepositoryName(repository, fitness);
		
		evaluateRepositoryInterfaces(repository, fitness);
		
		evaluateRepositoryMethods(repository, fitness);
		
		repository.setFitness(fitness);
	}

	private void evaluateRepositoryName(Class repository, DDDFitness fitness) {
		if (repository.getName().endsWith("RepositoryImpl")) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, "The name of the repsitory '" + repository.getName() + "' should end with 'RepositoryImpl'");
		}
	}

	private void evaluateRepositoryInterfaces(Class repository, DDDFitness fitness) {
		boolean containtsInterface = false;
		for (Interface i : repository.getInterfaces()) {
			if (i.getName().endsWith(REPOSITORY)) {
				containtsInterface = true;
			}
		}
		
		if (containtsInterface) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The repsitory '" + repository.getName() + "' does not implement an interface.");
		}
	}

	private void evaluateRepositoryMethods(Class repository, DDDFitness fitness) {
		boolean containtsFind = false, containtsSave = false, containtsDelete = false, containtsContains = false, containtsUpdate = false;
		
		for (Method method : repository.getMethods()) {
			if (isFind(method)) {
				containtsFind = true;
			} else if (isSave(method)) {
				containtsSave = true;
			} else if (isDelete(method)) {
				containtsDelete = true;
			} else if (isContaints(method)) {
				containtsContains = true;
			} else if (isUpdate(method)) {
				containtsUpdate = true;
			}
		}
		
		if (containtsFind)
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository '" + repository.getName() + "' has no findBy/get method.");
		
		if (containtsSave)		
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository '" + repository.getName() + "' has no save/add/insert method.");
		
		if (containtsDelete)	
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository '" + repository.getName() + "' has no delete/remove method.");
		
		if (containtsContains)	
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository '" + repository.getName() + "' has no contains/exists method.");
		
		if (containtsUpdate)	
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository '" + repository.getName() + "' has no update method.");
	}

	private boolean isFind(Method method) {
		return method.getName().startsWith("findBy") 
				|| method.getName().startsWith("get");
	}

	private boolean isSave(Method method) {
		return method.getName().startsWith("save") 
				|| method.getName().startsWith("add") 
				|| method.getName().startsWith("insert");
	}

	private boolean isDelete(Method method) {
		return method.getName().startsWith("delete") 
				|| method.getName().startsWith("remove");
	}

	private boolean isContaints(Method method) {
		return method.getName().startsWith("contains")
				|| method.getName().startsWith("exists");
	}

	private boolean isUpdate(Method method) {
		return method.getName().startsWith("update");
	}
	
	private void evaluateFactory(Class factory) {
		LOGGER.info("DDD:FACTORY:{}", factory.getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateFactoryName(factory, fitness);
		
		evaluateFactoryInterfaces(factory, fitness);
		
		evaluateFactoryFields(factory, fitness);
				
		evaluateFactoryMethods(factory, fitness);
		
		factory.setFitness(fitness);
	}

	private void evaluateFactoryName(Class factory, DDDFitness fitness) {
		if (factory.getName().endsWith("FactoryImpl")) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, "The name of the factory '" + factory.getName() + "' should end with 'FactoryImpl'");
		}
	}
	
	private void evaluateFactoryInterfaces(Class factory, DDDFitness fitness) {
		boolean containtsInterface = false;
		for (Interface i : factory.getInterfaces()) {
			if (i.getName().endsWith(FACTORY)) {
				containtsInterface = true;
			}
		}
		
		if (containtsInterface) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The factory '" + factory.getName() + "' does not implement an interface.");
		}
	}

	private void evaluateFactoryFields(Class factory, DDDFitness fitness) {
		boolean containtsRepo = false;
		for (Field field : factory.getFields()) {
			if (field.getType().contains(REPOSITORY)) {
				containtsRepo = true;
			}
		}
		
		if (containtsRepo) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The factory '" + factory.getName() + "' does not containts a repository as field.");
		}
	}

	private void evaluateFactoryMethods(Class factory, DDDFitness fitness) {
		boolean conataintsCreate = false;
		for (Method method : factory.getMethods()) {
			if (method.getName().startsWith("create")) {
				conataintsCreate = true;
			} 
		}
		
		if (conataintsCreate) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The factory '" + factory.getName() + "' does not containts a create method.");
		}
	}
	
	private void evaluateService(Class service) {
		LOGGER.info("DDD:SERVICE:{}", service.getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateServiceName(service, fitness);
		
		evaluateServiceInterfaces(service, fitness);

		service.setFitness(fitness);
	}
	
	private void evaluateServiceName(Class service, DDDFitness fitness) {
		if (service.getName().endsWith("Impl")) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, "The name of the service '" + service.getName() + "' should end with 'Impl'");
		}
	}
	
	private void evaluateServiceInterfaces(Class service, DDDFitness fitness) {
		boolean containtsInterface = false;
		for (Interface i : service.getInterfaces()) {
			if (service.getName().startsWith(i.getName())) {
				containtsInterface = true;
			}
		}
		
		if (containtsInterface) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The service '" + service.getName() + "' does not implement an interface.");
		}
	}

	private void evaluateApplicationService(Class appService) {
		LOGGER.info("DDD:APPLICATION_SERVICE:{}", appService.getName());
		if (appService.getPath().contains("application.")) {
			appService.setFitness(new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR));
		} else {
			appService.setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, "The application service '" + appService.getName() + "' is not part of an application module"));
		}
	}

	private void evaluateInfrastructure(Class infra) {
		LOGGER.info("DDD:INFRASTRUCTUR:{}", infra.getName());
		if (infra.getPath().contains("infrastructure.")) {
			infra.setFitness(new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR));
		} else {
			infra.setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, "The infrastructure service '" + infra.getName() + "' is not part of an infrastructure module"));
		}
	}
	
	private void evaluateDomainEvent(Class event) {
		LOGGER.info("DDD:DOMAIN_EVENT:{}", event.getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateDomainEventFields(event, fitness);
				
		event.setFitness(fitness);
	}

	private void evaluateDomainEventFields(Class event, DDDFitness fitness) {
		int ctr = 0;
		boolean containtsId = false;
		
		for (Field field : event.getFields()) {
			if (field.getName().contains("time") 
				|| field.getName().contains("date") 
				|| field.getType().contains("java.time.")
				|| field.getType().contains(structureService.getPath())) {
				fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
				ctr++;
				evaluateDomainEventMethodsOfField(event, field, fitness);
				if (field.getName().toUpperCase().endsWith("ID")) {
					containtsId = true;
				}
			}
		}
		
		if (containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "The domain event '" + event.getName() + "' does not containts an ID.");
		}
		
		if (ctr < 0) {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "The domain event '" + event.getName() + "' does not containts any fields.");
		}
	}

	private void evaluateDomainEventMethodsOfField(Class event, Field field, DDDFitness fitness) {
		boolean containtsGetter = false;
		boolean containtsSetter = false;
		
		for (Method method : event.getMethods()) {
			if (method.getName().toUpperCase().startsWith("GET" + field.getName().toUpperCase())) {
				containtsGetter = true;
			} else if (method.getName().toUpperCase().startsWith("SET" + field.getName().toUpperCase())) {
				containtsSetter = true;
			}
		}
		
		if (containtsGetter) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "");
		}
		
		if (!containtsSetter) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, "The domain event '" + event.getName() + "' containats a setter for the field '" + field.getName() + "'.");
		}
	}

	private void evaluateInterfaces() {
		LOGGER.info("Evaluation of Interfaces");
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(item -> {
				switch(item.getType()) {
				case FACTORY:
					evaluateFactory(item);
					break;
				case REPOSITORY:
					evaluateRepository(item);
					break;
				case SERVICE:
				default:
					LOGGER.info("DDD:SERVICE:{}", item.getName());
					item.setFitness(new DDDFitness());
				}
			});
	}
	
	private void evaluateRepository(Interface repository) {
		LOGGER.info("DDD:REPOSITORY:{}", repository.getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateRepositoryName(repository, fitness);
		
		evaluateRepositoryMethods(repository, fitness);
		
		repository.setFitness(fitness);
	}
	
	private void evaluateRepositoryName(Interface repository, DDDFitness fitness) {
		if (repository.getName().endsWith(REPOSITORY)) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, "The name of the repsitory interface '" + repository.getName() + "' should end with 'Repository'");
		}
	}

	private void evaluateRepositoryMethods(Interface repository, DDDFitness fitness) {
		boolean containtsFind = false, containtsSave = false, containtsDelete = false, containtsContains = false, containtsUpdate = false;
		
		for (Method method : repository.getMethods()) {
			if (isFind(method)) {
				containtsFind = true;
			} else if (isSave(method)) {
				containtsSave = true;
			} else if (isDelete(method)) {
				containtsDelete = true;
			} else if (isContaints(method)) {
				containtsContains = true;
			} else if (isUpdate(method)) {
				containtsUpdate = true;
			}
		}
		
		if (containtsFind)
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository interface '" + repository.getName() + "' has no findBy/get method.");
		
		if (containtsSave)		
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository interface '" + repository.getName() + "' has no save/add/insert method.");
		
		if (containtsDelete)	
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository interface '" + repository.getName() + "' has no delete/remove method.");
		
		if (containtsContains)	
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository interface '" + repository.getName() + "' has no contains/exists method.");
		
		if (containtsUpdate)	
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		else 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The Repository interface '" + repository.getName() + "' has no update method.");
	}

	private void evaluateFactory(Interface factory) {
		LOGGER.info("DDD:FACTORY:{}", factory.getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateFactoryName(factory, fitness);
		
		evaluateFactoryFields(factory, fitness);
				
		evaluateFactoryMethods(factory, fitness);
		
		factory.setFitness(fitness);
	}
	
	private void evaluateFactoryName(Interface factory, DDDFitness fitness) {
		if (factory.getName().endsWith(FACTORY)) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, "The name of the factory interface '" + factory.getName() + "' should end with 'FactoryImpl'");
		}
	}
	
	private void evaluateFactoryFields(Interface factory, DDDFitness fitness) {
		boolean containtsRepo = false;
		for (Field field : factory.getFields()) {
			if (field.getType().contains(REPOSITORY)) {
				containtsRepo = true;
			}
		}
		
		if (containtsRepo) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The factory interface '" + factory.getName() + "' does not containts a repository as field.");
		}
	}

	private void evaluateFactoryMethods(Interface factory, DDDFitness fitness) {
		boolean conataintsCreate = false;
		for (Method method : factory.getMethods()) {
			if (method.getName().startsWith("create")) {
				conataintsCreate = true;
			} 
		}
		
		if (conataintsCreate) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, "The factory interface '" + factory.getName() + "' does not containts a create method.");
		}
	}

	private void evaluateAnnotations() {
		LOGGER.info("Evaluation of Annotations");
		structureService.getAnnotations().stream()
			.parallel()
			.forEach(item -> {
				LOGGER.info("DDD:INFRASTRUCTUR:{}", item.getName());
				if (item.getPath().contains("infrastructure.")) {
					item.setFitness(new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR));
				} else {
					item.setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, "The annotation '" + item.getName() + "' is not part of an infrastructure module"));
				}
			});
	}
}
