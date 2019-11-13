package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDIssueType;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.JavaArtifactService;
import illumi.code.ddd.service.StructureService;

/**
 * Entity-Class: Class
 * @author Daniel Kraft
 */
public class Class extends Artifact {

	private static final Logger LOGGER = LoggerFactory.getLogger(Class.class);
	
	private static final String FACTORY = "Factory";
	private static final String REPOSITORY = "Repository";
	
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
		init();
	}
	
	public Class(String name, String path) {
		super(name, path, null);
		init();
	}
	
	private void init() {
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

	public List<Field> getFields() {
		return fields;
	}
	
	public void setFields(Driver driver) {
		this.fields = (ArrayList<Field>) JavaArtifactService.getFields(getPath(), driver, QUERY_FIELDS);
    }
	
	public void addField(Field field) {
		this.fields.add(field);
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(Driver driver) {
		this.methods = (ArrayList<Method>) JavaArtifactService.getMethods(getPath(), driver, QUERY_METHODS);
    }
	
	public void addMethod(Method method) {
		this.methods.add(method);
	}
	
	public List<Interface> getInterfaces() {
		return implInterfaces;
	}
	
	public void setImplInterfaces(Driver driver, List<Interface> interfaces) {
		this.implInterfaces =  (ArrayList<Interface>) JavaArtifactService.getImplInterfaces(getPath(), driver, QUERY_IMPL, interfaces);
    }
	
	public void addImplInterface(Interface implInterface) {
		this.implInterfaces.add(implInterface);
	}

	public Class getSuperClass() {
		return superClass;
	}

	public void setSuperClass(Driver driver, List<Class> classes) {
		this.superClass = JavaArtifactService.getSuperClass(getPath(), driver, QUERY_SUPER, classes);
	}
	
	public void addSuperClass(Class superClass) {
		this.superClass = superClass;
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) JavaArtifactService.getAnnotations(getPath(), driver, QUERY_PARENT_ANNOTATIONS, QUERY_CHILD_ANNOTATIONS, annotations);
	}
	
	public void evaluate(StructureService structureService) {
		switch(getType()) {
			case ENTITY:
				evaluateEntity(structureService);
				break;
			case VALUE_OBJECT:
				evaluateValueObject(structureService);
				break;
			case AGGREGATE_ROOT:
				evaluateAggregateRoot(structureService);
				break;
			case REPOSITORY:
				evaluateRepository(); 
				break;
			case FACTORY:
				evaluateFactory();
				break;
			case SERVICE:
				evaluateService();
				break;
			case DOMAIN_EVENT:
				evaluateDomainEvent(structureService);
				break;
			case APPLICATION_SERVICE:
				evaluateApplicationService();
				break;
			case CONTROLLER:
			case INFRASTRUCTUR:
			default:
				evaluateInfrastructure(); 
		}
	}

	private void evaluateEntity(StructureService structureService) {
		LOGGER.info("DDD:ENTITY:{}", getName());
		DDDFitness fitness = new DDDFitness();
				
		evaluateEntityFields(structureService, fitness);
		
		evaluateEntityMethods(this, fitness);
		
		evaluateSuperClass(getSuperClass(), fitness);
		
		setFitness(fitness);
	}

	private void evaluateEntityFields(StructureService structureService, DDDFitness fitness) {
		boolean containtsId = false;
		for (Field field : getFields()) {
			if (isFieldAnId(field)) {
				containtsId = true;
			}
			
			// Is type of field Entity or Value Object?
			if (field.getType().contains(structureService.getPath())) {
				fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
			} else {
				fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Field '%s' of the Entity '%s' is not a type of an Entity or a Value Object", field.getName(), getName()));
			}
		}
		
		if (containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else if (getSuperClass() == null) {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Entity '%s' does not containts an ID.", getName()));
		}
	}
	
	private boolean isFieldAnId(Field field) {
		return field.getName().toUpperCase().endsWith("ID");
	}

	private void evaluateEntityMethods(Class item, DDDFitness fitness) {
		int ctr = 0;
		for (Method method : item.getMethods()) {
			if (isNeededMethod(method)) {
				ctr++;
			} 
		}
		if (ctr >= 2) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else if (item.getSuperClass() == null) {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Entity '%s' does not containts all needed methods (equals/hashCode).", item.getName()));
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
	
	private void evaluateValueObject(StructureService structureService) {
		LOGGER.info("DDD:VALUE_OBJECT:{}", getName());
		// Must have criteria of Entity: no ID
		DDDFitness fitness = new DDDFitness();
		
		evaluateValueObjectFields(structureService, fitness);
				
		setFitness(fitness);
	}

	private void evaluateValueObjectFields(StructureService structureService, DDDFitness fitness) {
		boolean containtsId = false;
		for (Field field : getFields()) {
			if (isFieldAnId(field)) {
				containtsId = true;
			}
			
			// Is type of field Value Object or standard type?
			if (field.getType().contains(structureService.getPath()) || field.getType().contains("java.lang.")) {
				fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
			} else {
				fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Field '%s' of Value Object '%s' is not a Value Object or a standard type.", field.getName(), getName()));
			}
			
			// Has the field a getter and an immutable setter?
			evaluateValueObjectMethodOfField(field, fitness);
		}
		
		if (!containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Value Object '%s' containts an ID.", getName()));
		}
	}
	
	private void evaluateValueObjectMethodOfField(Field field, DDDFitness fitness) {
		boolean containtsSetter = false;
		boolean containtsGetter = false;
		for (Method method : getMethods()) {
			if (method.getName().equalsIgnoreCase("set" + field.getName())) {
				containtsSetter = true;
				if (isMethodImmutable(method)) {
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
	
	private boolean isMethodImmutable(Method method) {
		return method.getSignature().split(" ")[0].contains(getPath());
	}
	
	private void evaluateAggregateRoot(StructureService structureService) {
		LOGGER.info("DDD:AGGREGATE_ROOT:{}", getName());
		evaluateEntity(structureService);
		DDDFitness fitness = getDDDFitness();
		
		evaluateDomainStructure(structureService, fitness);
		
	}

	private void evaluateDomainStructure(StructureService structureService, DDDFitness fitness) {
		boolean repoAvailable = false;
		boolean factoryAvailable = false;
		boolean serviceAvailable = false;
		
		for (Artifact artifact : getDomainModule(structureService, getDomain())) {
			if (isAggregateRootRepository(artifact)) {
				repoAvailable = true;
			} else if(isAggregateRootFactory(artifact) ) {
				factoryAvailable = true;
			} else if(isAggregateRootService(artifact)) {
				serviceAvailable = true;
			}
		}
		
		if (repoAvailable) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("No repository of the aggregate root '%s' is available", getName()));
		}
		
		if (factoryAvailable) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("No factory of the aggregate root '%s' is available", getName()));
		}
		
		if (serviceAvailable) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("No service of the aggregate root '%s' is available", getName()));
		}
	}

	private boolean isAggregateRootRepository(Artifact artifact) {
		return artifact.getType() == DDDType.REPOSITORY && artifact.getName().contains(getName() + REPOSITORY);
	}

	private boolean isAggregateRootFactory(Artifact artifact) {
		return artifact.getType() == DDDType.FACTORY && artifact.getName().contains(getName() + FACTORY);
	}

	private boolean isAggregateRootService(Artifact artifact) {
		return artifact.getType() == DDDType.SERVICE && artifact.getName().contains(getName());
	}

	private ArrayList<Artifact> getDomainModule(StructureService structureService, String domain) {
		for (Package module : structureService.getPackages()) {
			if (module.getName().contains(domain)) {
				return (ArrayList<Artifact>) module.getConataints();
			}
		}
		return new ArrayList<>();
	}

	private void evaluateRepository() {
		LOGGER.info("DDD:REPOSITORY:{}", getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateRepositoryName(fitness);
		
		evaluateRepositoryInterfaces(fitness);
		
		evaluateRepositoryMethods(fitness);
		
		setFitness(fitness);
	}

	private void evaluateRepositoryName(DDDFitness fitness) {
		if (getName().endsWith("RepositoryImpl")) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the repsitory '%s' should end with 'RepositoryImpl'", getName()));
		}
	}

	private void evaluateRepositoryInterfaces(DDDFitness fitness) {
		boolean containtsInterface = false;
		for (Interface i : getInterfaces()) {
			if (i.getName().endsWith(REPOSITORY)) {
				containtsInterface = true;
			}
		}
		
		if (containtsInterface) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The repsitory '%s' does not implement an interface.", getName()));
		}
	}

	private void evaluateRepositoryMethods(DDDFitness fitness) {
		evaluateRepositoryMethods((ArrayList<Method>) getMethods(), getName(), fitness);
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

	private void createIssues(String name, DDDFitness fitness, boolean containtsFind, boolean containtsSave,
			boolean containtsDelete, boolean containtsContains, boolean containtsUpdate) {
		if (containtsFind) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository '%s' has no findBy/get method.", name));
		}
		
		if (containtsSave) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository '%s' has no save/add/insert method.", name));
		}
		
		if (containtsDelete) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository '%s' has no delete/remove method.", name));
		}
		
		if (containtsContains) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository '%s' has no contains/exists method.", name));
		}
		
		if (containtsUpdate) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Repository '%s' has no update method.", name));
		}
	}
	
	private void evaluateFactory() {
		LOGGER.info("DDD:FACTORY:{}", getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateFactoryName(fitness);
		
		evaluateFactoryInterfaces(fitness);
		
		evaluateFactoryFields(fitness);
				
		evaluateFactoryMethods(fitness);
		
		setFitness(fitness);
	}

	private void evaluateFactoryName(DDDFitness fitness) {
		if (getName().endsWith("FactoryImpl")) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the factory '%s' should end with 'FactoryImpl'", getName()));
		}
	}
	
	private void evaluateFactoryInterfaces(DDDFitness fitness) {
		boolean containtsInterface = false;
		for (Interface i : getInterfaces()) {
			if (i.getName().endsWith(FACTORY)) {
				containtsInterface = true;
			}
		}
		
		if (containtsInterface) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory '%s' does not implement an interface.", getName()));
		}
	}

	private void evaluateFactoryFields(DDDFitness fitness) {
		boolean containtsRepo = false;
		for (Field field : getFields()) {
			if (field.getType().contains(REPOSITORY)) {
				containtsRepo = true;
			}
		}
		
		if (containtsRepo) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory '%s' does not containts a repository as field.", getName()));
		}
	}

	private void evaluateFactoryMethods(DDDFitness fitness) {
		boolean conataintsCreate = false;
		for (Method method : getMethods()) {
			if (method.getName().startsWith("create")) {
				conataintsCreate = true;
			} 
		}
		
		if (conataintsCreate) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory '%s' does not containts a create method.", getName()));
		}
	}
	
	private void evaluateService() {
		LOGGER.info("DDD:SERVICE:{}", getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateServiceName(fitness);
		
		evaluateServiceInterfaces(fitness);

		setFitness(fitness);
	}
	
	private void evaluateServiceName(DDDFitness fitness) {
		if (getName().endsWith("Impl")) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the service '%s' should end with 'Impl'", getName()));
		}
	}
	
	private void evaluateServiceInterfaces(DDDFitness fitness) {
		boolean containtsInterface = false;
		for (Interface i : getInterfaces()) {
			if (getName().startsWith(i.getName())) {
				containtsInterface = true;
			}
		}
		
		if (containtsInterface) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The service '%s' does not implement an interface.", getName()));
		}
	}

	private void evaluateApplicationService() {
		LOGGER.info("DDD:APPLICATION_SERVICE:{}", getName());
		if (getPath().contains("application.")) {
			setFitness(new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR));
		} else {
			setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, String.format("The application service '%s' is not part of an application module", getName())));
		}
	}

	private void evaluateInfrastructure() {
		LOGGER.info("DDD:INFRASTRUCTUR:{}", getName());
		if (getPath().contains("infrastructure.")) {
			setFitness(new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR));
		} else {
			setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, String.format("The infrastructure service '%s' is not part of an infrastructure module", getName())));
		}
	}
	
	private void evaluateDomainEvent(StructureService structureService) {
		LOGGER.info("DDD:DOMAIN_EVENT:{}", getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateDomainEventFields(structureService, fitness);
				
		setFitness(fitness);
	}

	private void evaluateDomainEventFields(StructureService structureService, DDDFitness fitness) {
		int ctr = 0;
		boolean containtsId = false;
		
		for (Field field : getFields()) {
			if (field.getName().contains("time") 
				|| field.getName().contains("date") 
				|| field.getType().contains("java.time.")
				|| field.getType().contains(structureService.getPath())) {
				fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
				ctr++;
				evaluateDomainEventMethodsOfField(field, fitness);
				if (field.getName().toUpperCase().endsWith("ID")) {
					containtsId = true;
				}
			}
		}
		
		if (containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The domain event '%s' does not containts an ID.",getName()));
		}
		
		if (ctr == 0) {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The domain event '%s' does not containts any fields.",getName()));
		}
	}

	private void evaluateDomainEventMethodsOfField(Field field, DDDFitness fitness) {
		boolean containtsGetter = false;
		boolean containtsSetter = false;
		
		for (Method method : getMethods()) {
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
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The domain event '%s' containats a setter for the field '%s'.", getName(), field.getName()));
		}
	}
}
