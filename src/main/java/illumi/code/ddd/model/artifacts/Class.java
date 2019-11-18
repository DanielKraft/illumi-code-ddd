package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import illumi.code.ddd.service.fitness.ClassFitnessService;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final String QUERY_CLASS_FIELDS 				= "MATCH (c:Class)-[:DECLARES]->(f:Field) WHERE c.fqn={path} RETURN DISTINCT f.name as name, f.signature as type, f.visibility as visibility";
	private static final String QUERY_CLASS_METHODS 			= "MATCH (c:Class)-[:DECLARES]->(m:Method) WHERE c.fqn = {path} RETURN DISTINCT m.visibility as visibility, m.name as name, m.signature as signature";
	private static final String QUERY_CLASS_SUPER 				= "MATCH (c1:Class)-[:EXTENDS]->(c2:Class) WHERE c1.fqn={path} RETURN DISTINCT c2.fqn as superClass";
	private static final String QUERY_CLASS_IMPL 				= "MATCH (c:Class)-[:IMPLEMENTS]->(i:Interface) WHERE c.fqn={path} RETURN DISTINCT i.fqn as interface";
	private static final String QUERY_CLASS_PARENT_ANNOTATIONS 	= "MATCH (parent:Class)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} RETURN DISTINCT type.fqn as annotation";
	private static final String QUERY_CLASS_CHILD_ANNOTATIONS	= "MATCH (parent:Class)-[:DECLARES]->(child:Java)-[:ANNOTATED_BY]->(annotation:Annotation)-[:OF_TYPE]->(type:Type) WHERE parent.fqn = {path} AND (child:Field OR child:Method) RETURN DISTINCT type.fqn as annotation";
	
	
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
		this.fields = (ArrayList<Field>) JavaArtifactService.getFields(getPath(), driver, QUERY_CLASS_FIELDS);
    }
	
	public void addField(Field field) {
		this.fields.add(field);
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(Driver driver) {
		this.methods = (ArrayList<Method>) JavaArtifactService.getMethods(getPath(), driver, QUERY_CLASS_METHODS);
    }
	
	public void addMethod(Method method) {
		this.methods.add(method);
	}
	
	public List<Interface> getInterfaces() {
		return implInterfaces;
	}
	
	public void setImplInterfaces(Driver driver, List<Interface> interfaces) {
		this.implInterfaces =  (ArrayList<Interface>) JavaArtifactService.getImplInterfaces(getPath(), driver, QUERY_CLASS_IMPL, interfaces);
    }
	
	public void addImplInterface(Interface implInterface) {
		this.implInterfaces.add(implInterface);
	}

	public Class getSuperClass() {
		return superClass;
	}

	public void setSuperClass(Driver driver, List<Class> classes) {
		this.superClass = JavaArtifactService.getSuperClass(getPath(), driver, QUERY_CLASS_SUPER, classes);
	}
	
	public void addSuperClass(Class superClass) {
		this.superClass = superClass;
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Driver driver, List<Annotation> annotations) {
		this.annotations = (ArrayList<Annotation>) JavaArtifactService.getAnnotations(getPath(), driver, QUERY_CLASS_PARENT_ANNOTATIONS, QUERY_CLASS_CHILD_ANNOTATIONS, annotations);
	}
	
	public void setType(StructureService structureService) {
		if (isInfrastructur()) {
			setType(DDDType.INFRASTRUCTUR);
		} else if (getType() == null) {
			if (isValueObject(structureService)) {
				setType(DDDType.VALUE_OBJECT);
			} else if (isEntity(structureService)) {
				setType(DDDType.ENTITY);
			} else if (isService(structureService)) { 
				setType(DDDType.SERVICE);
			} else {
				setType(DDDType.INFRASTRUCTUR);
			}
		} 
	}
	
	private boolean isInfrastructur() {
		return getName().toUpperCase().contains("JPA") || getName().toUpperCase().contains("CRUD");
	}

	private boolean isValueObject(StructureService structureService) {
		int ctr = 0;
		for (Field field : fields) {
			if (isConstant(field)) {
				return false;
			} else if (Field.isId(field) 
					&& !(getName().toLowerCase().endsWith("id"))) {
				return false;
			} else if (field.getType().startsWith("java.lang.") 
					|| field.getType().contains(structureService.getPath())) {
				ctr++;
			}
		}
		return !fields.isEmpty() 
				&& ctr == fields.size() 
				&& (conatiantsGetterSetter()
						|| methods.isEmpty());
	}

	private boolean isEntity(StructureService structureService) {
		
		for (Field field : fields) {
			if (isConstant(field)) {
				return false;
			} else if (Field.isId(field)) {
				return true;
			}
		}
		return !fields.isEmpty() 
				&& !containsEntityName(structureService) 
				&& (conatiantsGetterSetter()
						|| methods.isEmpty());
	}
	
	private boolean isService(StructureService structureService) {
		for (Field field : fields) {
			if (field.getType().contains(REPOSITORY)) {
				return true;
			}
		}
		return containsEntityName(structureService);
	}

	private boolean isConstant(Field field) {
		return StringUtils.isAllUpperCase(field.getName());
	}
	
	private boolean conatiantsGetterSetter() {
		for (Method method : methods) {
			if (method.getName().startsWith("get") ^ method.getName().startsWith("set")) {
				return true;
			}
		}
		return containtsUnconventionalGetter();
	}

	private boolean containtsUnconventionalGetter() {
		for (Method method : methods) {
			for (Field field : fields) {
				if (method.getSignature().startsWith(field.getType())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean containsEntityName(StructureService structureService) {
		for (Class artifact : structureService.getClasses()) {			
			if (this != artifact 
				&& getName().contains(artifact.getName()) 
				&& !getName().equals(artifact.getName() + "s")) {
				return true;
			}
		}
		return false;
	}
	
	public void setDomainEvent() {
		switch(getType()) {
			case ENTITY:
			case AGGREGATE_ROOT:
			case VALUE_OBJECT:
				if (isDomainEvent()) {
					setType(DDDType.DOMAIN_EVENT);
				}
				break;
			default:
				break;
		}
	}
	
	private boolean isDomainEvent() {		
		boolean containtsTimestamp = false;
		boolean containtsIdentity = false;
		
		for (Field field : fields) {
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
	
	public void evaluate(StructureService structureService) {
		new ClassFitnessService(this, structureService).evaluate();
	}
}
