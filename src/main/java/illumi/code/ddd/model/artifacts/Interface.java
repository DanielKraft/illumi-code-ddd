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

/**
 * Entity-Class: Interface
 * @author Daniel Kraft
 */
public class Interface extends Artifact {
		
	private static final Logger LOGGER = LoggerFactory.getLogger(Interface.class);
	
	private static final String FACTORY = "Factory";
	private static final String REPOSITORY = "Repository";
	
	private static final String QUERY_FIELDS 				= "MATCH (i:Interface)-[:DECLARES]->(f:Field) WHERE i.fqn= {path} RETURN DISTINCT f.name as name, f.signature as type, f.visibility as visibility";
	private static final String QUERY_METHODS				= "MATCH (i:Interface)-[:DECLARES]->(m:Method) WHERE i.fqn = {path} RETURN DISTINCT m.visibility as visibility, m.name as name, m.signature as signature";
	private static final String QUERY_IMPL 					= "MATCH (i1:Interface)-[:IMPLEMENTS]->(i2:Interface) WHERE i1.fqn= {path} RETURN i2.fqn as interface";
	private static final String QUERY_PARENT_ANNOTATIONS	= "MATCH (parent:Interface)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} RETURN DISTINCT type.fqn as annotation";
	private static final String QUERY_CHILD_ANNOTATIONS 	= "MATCH (parent:Interface)-[:DECLARES]->(child:Java)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} AND (child:Field OR child:Method) RETURN DISTINCT type.fqn as annotation";
	
	private ArrayList<Field> fields;
	
	private ArrayList<Method> methods;
	
	private ArrayList<Interface> implInterfaces;
	
	private ArrayList<Annotation> annotations;
		
	public Interface(Record record) {
		super(record, null);
		init();
	}
	
	public Interface(String name, String path) {
		super(name, path, null);
		init();
	}
	
	private void init() {
		if (getName().toUpperCase().contains("FACTORY")) {
			setType(DDDType.FACTORY);
		}
		else if (getName().toUpperCase().contains("REPOSITORY")) {
			setType(DDDType.REPOSITORY);
		}
		else {
			setType(DDDType.SERVICE);
		}
		
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
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) JavaArtifactService.getAnnotations(getPath(), driver, QUERY_PARENT_ANNOTATIONS, QUERY_CHILD_ANNOTATIONS, annotations);
	}
	
	public void evaluate() {
		switch(getType()) {
			case REPOSITORY:
				evaluateRepository();
				break;
			case FACTORY:
				evaluateFactory();
				break;
			case SERVICE:
			default:
				LOGGER.info("DDD:SERVICE:{}", getName());
				setFitness(new DDDFitness());
		}
	}
	
	private void evaluateRepository() {
		LOGGER.info("DDD:REPOSITORY:{}", getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateRepositoryName(fitness);
		
		evaluateRepositoryMethods(fitness);
		
		setFitness(fitness);
	}
	
	private void evaluateRepositoryName(DDDFitness fitness) {
		if (getName().endsWith(REPOSITORY)) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the repsitory interface '%s' should end with 'Repository'", getName()));
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
	
	private void evaluateFactory() {
		LOGGER.info("DDD:FACTORY:{}", getName());
		DDDFitness fitness = new DDDFitness();
		
		evaluateFactoryName(fitness);
		
		evaluateFactoryFields(fitness);
				
		evaluateFactoryMethods(fitness);
		
		setFitness(fitness);
	}
	
	private void evaluateFactoryName(DDDFitness fitness) {
		if (getName().endsWith(FACTORY)) {
			fitness.addSuccessfulCriteria(DDDIssueType.INFO);
		} else {
			fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the factory interface '%s' should end with 'FactoryImpl'", getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory interface '%s' does not containts a repository as field.", getName()));
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
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory interface '%s' does not containts a create method.", getName()));
		}
	}
}
