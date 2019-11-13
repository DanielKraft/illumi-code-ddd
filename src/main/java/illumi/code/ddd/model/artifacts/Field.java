package illumi.code.ddd.model.artifacts;

import java.util.List;

import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDIssueType;
import illumi.code.ddd.service.StructureService;

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
	public static void evaluateEntity(Class artifact, StructureService structureService, DDDFitness fitness) {
		boolean containtsId = false;
		for (Field field : artifact.getFields()) {
			if (isId(field)) {
				containtsId = true;
			}
			
			// Is type of field Entity or Value Object?
			if (field.getType().contains(structureService.getPath())) {
				fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
			} else {
				fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The Field '%s' of the Entity '%s' is not a type of an Entity or a Value Object", field.getName(), artifact.getName()));
			}
		}
		
		if (containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else if (artifact.getSuperClass() == null) {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Entity '%s' does not containts an ID.", artifact.getName()));
		}
	}
	
	public static boolean isId(Field field) {
		return field.getName().toUpperCase().endsWith("ID");
	}
	
	public static void evaluateValueObject(Class artifact, StructureService structureService, DDDFitness fitness) {
		boolean containtsId = false;
		for (Field field : artifact.getFields()) {
			if (Field.isId(field)) {
				containtsId = true;
			}
			
			// Is type of field Value Object or standard type?
			if (field.getType().contains(structureService.getPath()) || field.getType().contains("java.lang.")) {
				fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
			} else {
				fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Field '%s' of Value Object '%s' is not a Value Object or a standard type.", field.getName(), artifact.getName()));
			}
			
			// Has the field a getter and an immutable setter?
			Method.evaluateValueObject(artifact, field, fitness);
		}
		
		if (!containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The Value Object '%s' containts an ID.", artifact.getName()));
		}
	}
	
	public static void evaluateDomainEvent(Class artifact, StructureService structureService, DDDFitness fitness) {
		int ctr = 0;
		boolean containtsId = false;
		
		for (Field field : artifact.getFields()) {
			if (field.getName().contains("time") 
				|| field.getName().contains("date") 
				|| field.getType().contains("java.time.")
				|| field.getType().contains(structureService.getPath())) {
				fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
				ctr++;
				Method.evaluateDomainEvent(artifact, field, fitness);
				if (field.getName().toUpperCase().endsWith("ID")) {
					containtsId = true;
				}
			}
		}
		
		if (containtsId) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The domain event '%s' does not containts an ID.", artifact.getName()));
		}
		
		if (ctr == 0) {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The domain event '%s' does not containts any fields.", artifact.getName()));
		}
	}
	
	public static void evaluateFactory(String name, List<Field> fields, DDDFitness fitness) {
		boolean containtsRepo = false;
		for (Field field : fields) {
			if (field.getType().contains("Repository")) {
				containtsRepo = true;
			}
		}
		
		if (containtsRepo) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory interface '%s' does not containts a repository as field.", name));
		}
	}
}
