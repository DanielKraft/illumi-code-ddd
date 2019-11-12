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
			.forEach(module -> {
				LOGGER.info("DDD:MODULE:{}", module.getName());
				module.evaluate(structureService);
			});
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
				fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Field '%s' of the Entity '%s' is not a type of an Entity or a Value Object", field.getName(), entity.getName()));
			}
		}
		
		if (containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else if (entity.getSuperClass() == null) {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Entity '%s' does not containts an ID.", entity.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Entity '%s' does not containts all needed methods (equals/hashCode).", entity.getName()));
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
				fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Entity '%s' does not containts an ID.", item.getName()));
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
				fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Field '%s' of Value Object '%s' is not a Value Object or a standard type.", field.getName(), valueObject.getName()));
			}
			
			// Has the field a getter and an immutable setter?
			evaluateValueObjectMethodOfField(valueObject, field, fitness);
		}
		
		if (!containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Value Object '%s' containts an ID.", valueObject.getName()));
		}
	}
	
	private void evaluateValueObjectMethodOfField(Class valueObject, Field field, DDDFitness fitness) {
		boolean containtsSetter = false;
		boolean containtsGetter = false;
		for (Method method : valueObject.getMethods()) {
			if (method.getName().equalsIgnoreCase("set" + field.getName())) {
				containtsSetter = true;
				if (isMethodImmutable(method, valueObject)) {
					fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
				} else {
					fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The method '%s(...)' is not immutable.", method.getName()));
				}
			} else if (method.getName().equalsIgnoreCase("get" + field.getName())) {
				containtsGetter = true;
			}
		}
		
		if (containtsSetter) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The field '%s' does not have a setter.", field.getName()));
		}
		
		if (containtsGetter) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The field '%s' does not have a Getter.", field.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("No repository of the aggregate root '%s' is available", aggregate.getName()));
		}
		
		if (factoryAvailable) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("No factory of the aggregate root '%s' is available", aggregate.getName()));
		}
		
		if (serviceAvailable) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("No service of the aggregate root '%s' is available", aggregate.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the repsitory '%s' should end with 'RepositoryImpl'", repository.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The repsitory '%s' does not implement an interface.", repository.getName()));
		}
	}

	private void evaluateRepositoryMethods(Class repository, DDDFitness fitness) {
		evaluateRepositoryMethods((ArrayList<Method>) repository.getMethods(), repository.getName(), fitness);
	}

	private void evaluateRepositoryMethods(ArrayList<Method> methods, String name, DDDFitness fitness) {
		boolean containtsFind = false;
		boolean containtsSave = false;
		boolean containtsDelete = false;
		boolean containtsContains = false;
		boolean containtsUpdate = false;
		
		for (Method method : methods) {
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
		
		createIssues(name, fitness, containtsFind, containtsSave, containtsDelete, containtsContains, containtsUpdate);
	}

	private void createIssues(String name, DDDFitness fitness, boolean containtsFind, boolean containtsSave,
			boolean containtsDelete, boolean containtsContains, boolean containtsUpdate) {
		if (containtsFind) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository interface '%s' has no findBy/get method.", name));
		}
		
		if (containtsSave) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository interface '%s' has no save/add/insert method.", name));
		}
		
		if (containtsDelete) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository interface '%s' has no delete/remove method.", name));
		}
		
		if (containtsContains) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository interface '%s' has no contains/exists method.", name));
		}
		
		if (containtsUpdate) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
 					
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository interface '%s' has no update method.", name));
		}
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
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the factory '%s' should end with 'FactoryImpl'", factory.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory '%s' does not implement an interface.", factory.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory '%s' does not containts a repository as field.", factory.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory '%s' does not containts a create method.", factory.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the service '%s' should end with 'Impl'", service.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The service '%s' does not implement an interface.", service.getName()));
		}
	}

	private void evaluateApplicationService(Class appService) {
		LOGGER.info("DDD:APPLICATION_SERVICE:{}", appService.getName());
		if (appService.getPath().contains("application.")) {
			appService.setFitness(new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR));
		} else {
			appService.setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, String.format("The application service '%s' is not part of an application module", appService.getName())));
		}
	}

	private void evaluateInfrastructure(Class infra) {
		LOGGER.info("DDD:INFRASTRUCTUR:{}", infra.getName());
		if (infra.getPath().contains("infrastructure.")) {
			infra.setFitness(new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR));
		} else {
			infra.setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, String.format("The infrastructure service '%s' is not part of an infrastructure module", infra.getName())));
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
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The domain event '%s' does not containts an ID.",event.getName()));
		}
		
		if (ctr < 0) {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The domain event '%s' does not containts any fields.",event.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The domain event '%s' containats a setter for the field '%s'.", event.getName(), field.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the repsitory interface '%s' should end with 'Repository'", repository.getName()));
		}
	}

	private void evaluateRepositoryMethods(Interface repository, DDDFitness fitness) {
		evaluateRepositoryMethods((ArrayList<Method>) repository.getMethods(), repository.getName(), fitness);
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
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the factory interface '%s' should end with 'FactoryImpl'", factory.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory interface '%s' does not containts a repository as field.", factory.getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory interface '%s' does not containts a create method.", factory.getName()));
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
					item.setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, String.format("The annotation '%s' is not part of an infrastructure module", item.getName())));
				}
			});
	}
}
