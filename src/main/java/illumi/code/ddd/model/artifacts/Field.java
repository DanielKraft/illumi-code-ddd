package illumi.code.ddd.model.artifacts;

import java.util.List;

import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.fitness.DDDIssueType;
import illumi.code.ddd.model.DDDStructure;

public class Field {
	
	private String visibility;
	private String name;
	private String type;
	
	public Field( Record record) {
		this.visibility = record.get( "visibility" ).asString();
		this.name = record.get( "name" ).asString();
		this.type = record.get( "type" ).asString().split(" ")[0];
	}

	public Field(String visibility, String name, String type) {
		this.visibility = visibility;
		this.name = name;
		this.type = type;
	}

	public String getVisibility() {
		return visibility;
	}
	
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUMLSignature() {
		String umlVisibility = File.getUMLVisibility(visibility);
		return String.format("%s %s: %s", umlVisibility, name, type);
	}

	public static void evaluateEntity(Class artifact, DDDStructure structure, DDDFitness fitness) {
		boolean containsId = false;
		for (Field field : artifact.getFields()) {
			if (isId(field)) {
				containsId = true;
			}

			Method.evaluateEntity(artifact, field, fitness);

			// Is type of field Entity or Value Object?
			fitness.addIssue(field.getType().contains(structure.getPath()), DDDIssueType.MAJOR,
					String.format("The Field '%s' of the Entity '%s' is not a type of an Entity or a Value Object", field.getName(), artifact.getName()));
		}
		
		if (containsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.CRITICAL);
		} else if (artifact.getSuperClass() == null) {
			fitness.addFailedCriteria(DDDIssueType.CRITICAL, String.format("The Entity '%s' does not contains an ID.", artifact.getName()));
		}
	}
	
	public static boolean isId(Field field) {
		return field.getName().toUpperCase().endsWith("ID");
	}
	
	public static void evaluateValueObject(Class artifact, DDDStructure structure, DDDFitness fitness) {
		boolean containsId = false;
		for (Field field : artifact.getFields()) {
			if (Field.isId(field)
				&& !artifact.getName().toUpperCase().endsWith("ID")) {
				containsId = true;
			}
			
			// Is type of field Value Object or standard type?
			fitness.addIssue(isValidType(structure, field), DDDIssueType.MAJOR,
					String.format("The Field '%s' of Value Object '%s' is not a Value Object or a standard type.", field.getName(), artifact.getName()));
			
			// Has the field a getter and an immutable setter?
			Method.evaluateValueObject(artifact, field, fitness);
		}

		fitness.addIssue(!containsId, DDDIssueType.BLOCKER,
				String.format("The Value Object '%s' contains an ID.", artifact.getName()));
	}

	private static boolean isValidType(DDDStructure structure, Field field) {
		return (field.getType().contains(structure.getPath())
				|| field.getType().contains("java."))
				&& !field.getType().contains("java.util.");
	}

	public static void evaluateDomainEvent(Class artifact, DDDFitness fitness) {
		boolean containsTime = false;
		boolean containsId = false;
		
		for (Field field : artifact.getFields()) {
			if (field.getName().contains("time") 
				|| field.getName().contains("date") 
				|| field.getType().contains("java.time.")) {
				containsTime = true;
				Method.evaluateDomainEvent(artifact, field, fitness);
			} else if (field.getName().toUpperCase().endsWith("ID")) {
				containsId = true;
				Method.evaluateDomainEvent(artifact, field, fitness);
			}
		}

		fitness.addIssue(containsId, DDDIssueType.MAJOR,
				String.format("The domain event '%s' does not contains an ID.", artifact.getName()));

		fitness.addIssue(containsTime, DDDIssueType.MAJOR,
				String.format("The domain event '%s' does not contains any timestamp or date.", artifact.getName()));
	}
	
	public static void evaluateFactory(String name, List<Field> fields, DDDFitness fitness) {
		boolean containsRepo = false;
		for (Field field : fields) {
			if (field.getType().contains("Repository")) {
				containsRepo = true;
				break;
			}
		}

		fitness.addIssue(containsRepo, DDDIssueType.MAJOR,
				String.format("The factory interface '%s' does not contains a repository as field.", name));
	}
}
