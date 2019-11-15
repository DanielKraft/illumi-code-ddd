package illumi.code.ddd.model.artifacts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.StructureService;

public class ClassAnalyseTest {

	private StructureService structureService;
	
	@BeforeEach
	public void init() {
		
		structureService = new StructureService();
		structureService.setPath("de.test");
	}
	
	@Test
	public void testSetTypeWithAllreadySet() {		
		Class artifact = new Class("Value", "de.test.domain.Value");
		artifact.setType(DDDType.VALUE_OBJECT);
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.VALUE_OBJECT, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToValueObject() {		
		Class artifact = new Class("Value", "de.test.domain.Value");
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
		artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		artifact.addMethod(new Method("public", "getName", "java.lang.String getName()"));
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.VALUE_OBJECT, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeTovalueObjecttWithId() {		
		Class artifact = new Class("ValueId", "de.test.domain.ValueId");
		artifact.addField(new Field("private", "id", "java.lang.long"));
		artifact.addMethod(new Method("public", "getId", "java.lang.long getId()"));
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.VALUE_OBJECT, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToValueObjectWithUnconventionalGetter() {		
		Class artifact = new Class("Value", "de.test.domain.Value");
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
		artifact.addMethod(new Method("public", "nachname", "de.test.domain.Nachname nachname()"));
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.VALUE_OBJECT, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToValueObjectWithoutGetterAndSetter() {		
		Class artifact = new Class("Value", "de.test.domain.Value");
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.VALUE_OBJECT, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToEntity() {	
		Class entity = new Class("Entity", "de.test.domain.Entity");
		structureService.addClasses(entity);
		
		Class artifact = new Class("Entitys", "de.test.domain.Entitys");
		artifact.addField(new Field("private", "other", ".de.Other"));
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		artifact.addMethod(new Method("public", "setName", "void setName(java.lang.String)"));
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.ENTITY, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToEntityWithId() {		
		Class artifact = new Class("Entity", "de.test.domain.Entity");
		artifact.addField(new Field("private", "other", ".de.Other"));
		artifact.addField(new Field("private", "id", "de.test.domain.EntityId"));
		artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		artifact.addMethod(new Method("public", "setName", "void setName(java.lang.String)"));
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.ENTITY, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToEntityWithoutMethods() {		
		Class artifact = new Class("Entity", "de.test.domain.Entity");
		artifact.addField(new Field("private", "other", "de.Other"));
		artifact.addField(new Field("private", "name", "java.lang.String"));
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.ENTITY, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToService() {	
		Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
		artifact.addField(new Field("private", "nameRepository", "de.test.domain.NameRepository"));
		artifact.addMethod(new Method("public", "generate", "void generate()"));
		structureService.addClasses(artifact);
		
		Class entity1 = new Class("Entity", "de.test.domain.Entity");
		entity1.addField(new Field("private", "name", "java.lang.String"));
		structureService.addClasses(entity1);
		
		Class entity2 = new Class("Name", "de.test.domain.Name");
		entity2.addField(new Field("private", "name", "java.lang.String"));
		structureService.addClasses(entity2);
		
		artifact.setType(structureService);

		assertEquals(DDDType.SERVICE, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToServiceWithConstant() {				
		Class entity = new Class("Name", "de.test.domain.Name");
		entity.addField(new Field("private", "name", "java.lang.String"));
		structureService.addClasses(entity);
		
		Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
		artifact.addField(new Field("private", "CONST", "java.lang.String"));
		artifact.addField(new Field("private", "nameRepository", "de.test.domain.NameRepository"));
		artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		artifact.addMethod(new Method("public", "generate", "void generate()"));
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.SERVICE, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToInfrastructureWithJPA() {		
		Class artifact = new Class("InfraJPA", "de.test.domain.InfraJPA");
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.INFRASTRUCTUR, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToInfrastructureWithCRUD() {		
		Class artifact = new Class("InfraCRUD", "de.test.domain.InfraCRUD");
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.INFRASTRUCTUR, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToInfrastructure() {		
		Class artifact = new Class("Entity", "de.test.domain.Entity");
		artifact.addField(new Field("private", "other", "de.Other"));
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addMethod(new Method("public", "generate", "void generate()"));
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.INFRASTRUCTUR, (DDDType) artifact.getType());
	}
	
	@Test
	public void testSetTypeToInfrastructureEmpty() {		
		Class artifact = new Class("Infra", "de.test.domain.Infra");
		structureService.addClasses(artifact);
		
		artifact.setType(structureService);

		assertEquals(DDDType.INFRASTRUCTUR, (DDDType) artifact.getType());
	}

	@Test
	public void testSetDomainEvent() {
		Class artifact = new Class("Event", "de.test.domain.Event");
		artifact.setType(DDDType.ENTITY);
		artifact.addField(new Field("private", "timestamp", "java.lang.long"));
		artifact.addField(new Field("private", "date", "java.lang.String"));
		artifact.addField(new Field("private", "stamp", "java.time.Time"));
		artifact.addField(new Field("private", "EntityId", "java.lang.long"));
		artifact.addField(new Field("private", "desc", "java.lang.String"));
		
		artifact.setDomainEvent();
		
		assertEquals(DDDType.DOMAIN_EVENT, (DDDType) artifact.getType());
	}

	@Test
	public void testSetDomainEventWithId() {
		Class artifact = new Class("Entity", "de.test.domain.Entity");
		artifact.setType(DDDType.AGGREGATE_ROOT);
		artifact.addField(new Field("private", "id", "java.lang.long"));
		
		artifact.setDomainEvent();
		
		assertEquals(DDDType.AGGREGATE_ROOT, (DDDType) artifact.getType());
	}

	@Test
	public void testSetDomainEventWithDate() {
		Class artifact = new Class("Event", "de.test.domain.Event");
		artifact.setType(DDDType.VALUE_OBJECT);
		artifact.addField(new Field("private", "birth", "java.time.Time"));
		
		artifact.setDomainEvent();
		
		assertEquals(DDDType.VALUE_OBJECT, (DDDType) artifact.getType());
	}

	@Test
	public void testSetDomainEventOfOther() {
		Class artifact = new Class("Event", "de.test.domain.Event");
		artifact.setType(DDDType.FACTORY);
		
		artifact.setDomainEvent();
		
		assertEquals(DDDType.FACTORY, (DDDType) artifact.getType());
	}
}
