package illumi.code.ddd.model.artifacts;

import java.util.List;

import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDIssueType;

/**
 * Entity-Class: Method
 * @author Daniel Kraft
 */
public class Method {
			
	private String visibility;
	private String name;
	private String signature;
	
	public Method( Record record ) {
		this.visibility = record.get( "visibility" ).asString();
		this.name = record.get( "name" ).asString();
		this.signature = record.get( "signature" ).asString();
	}
	
	public Method(String visibility, String name, String signature) {
		this.visibility = visibility;
		this.name = name;
		this.signature = signature;
	}

	public String getVisibility() {
		return visibility;
	}
	
	public String getName() {
		return name;
	}

	public String getSignature() {
		return signature;
	}

	static void evaluateEntity(Class artifact, DDDFitness fitness) {
		int ctr = 0;
		for (Method method : artifact.getMethods()) {
			if (isNeededMethod(method)) {
				ctr++;
			} 
		}
		if (ctr >= 2) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else if (artifact.getSuperClass() == null) {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Entity '%s' does not containts all needed methods (equals/hashCode).", artifact.getName()));
		}
	}
	
	private static boolean isNeededMethod(Method method) {
		// equals() or hashCode()? 
		return method.getName().equals("equals") || method.getName().equals("hashCode");
	}
	
	static void evaluateValueObject(Class artifact, Field field, DDDFitness fitness) {
		boolean containtsSetter = false;
		boolean containtsGetter = false;
		for (Method method : artifact.getMethods()) {
			if (method.getName().equalsIgnoreCase("set" + field.getName())) {
				containtsSetter = true;
				if (isMethodImmutable(artifact, method)) {
					fitness.addSuccessfulCriteria(DDDIssueType.CRITICAL);
				} else {
					fitness.addFailedCriteria(DDDIssueType.CRITICAL, String.format("The method '%s(...)' is not immutable.", method.getName()));
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
	
	private static boolean isMethodImmutable(Class artifact, Method method) {
		return method.getSignature().split(" ")[0].contains(artifact.getPath());
	}

	static void evaluateDomainEvent(Class artifact, Field field, DDDFitness fitness) {
		boolean containtsGetter = false;
		boolean containtsSetter = false;
		
		for (Method method : artifact.getMethods()) {
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
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The domain event '%s' containats a setter for the field '%s'.", artifact.getName(), field.getName()));
		}
	}
	
	static void evaluateRepository(String name, List<Method> methods, DDDFitness fitness) {
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

	private static void createIssues(String name, DDDFitness fitness, boolean containtsFind, boolean containtsSave,
			boolean containtsDelete, boolean containtsContains, boolean containtsUpdate) {
		if (containtsFind) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Repository '%s' has no findBy/get method.", name));
		}
		
		if (containtsSave) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Repository '%s' has no save/add/insert method.", name));
		}
		
		if (containtsDelete) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Repository '%s' has no delete/remove method.", name));
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
	
	private static boolean isFind(Method method) {
		return method.getName().startsWith("findBy") 
				|| method.getName().startsWith("get");
	}

	private static boolean isSave(Method method) {
		return method.getName().startsWith("save") 
				|| method.getName().startsWith("add") 
				|| method.getName().startsWith("insert");
	}

	private static boolean isDelete(Method method) {
		return method.getName().startsWith("delete") 
				|| method.getName().startsWith("remove");
	}

	private static boolean isContaints(Method method) {
		return method.getName().startsWith("contains")
				|| method.getName().startsWith("exists");
	}

	private static boolean isUpdate(Method method) {
		return method.getName().startsWith("update");
	}
	
	static void evaluateFactory(String name, List<Method> methods, DDDFitness fitness) {
		boolean conataintsCreate = false;
		for (Method method : methods) {
			if (isCreate(method)) {
				conataintsCreate = true;
				break;
			} 
		}
		
		if (conataintsCreate) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The factory '%s' does not containts a create method.", name));
		}
	}
	
	private static boolean isCreate(Method method) {
		return method.getName().startsWith("create");
	}
}
