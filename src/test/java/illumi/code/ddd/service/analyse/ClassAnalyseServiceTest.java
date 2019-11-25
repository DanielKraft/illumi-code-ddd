package illumi.code.ddd.service.analyse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.StructureService;

class ClassAnalyseServiceTest {

	private StructureService structureService;
	
	@BeforeEach
	void init() {
		structureService = new StructureService();
		structureService.setPath("de.test");
	}
	
	@Test
	void testSetTypeWithAllreadySet() {
		Class artifact = new Class("Value", "de.test.domain.Value");
		artifact.setType(DDDType.VALUE_OBJECT);
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
	}
	
	@Test
	void testSetTypeToValueObject() {
		Class artifact = new Class("Value", "de.test.domain.Value");
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
		artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		artifact.addMethod(new Method("public", "getName", "java.lang.String getName()"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
	}
	
	@Test
	void testSetTypeTovalueObjecttWithId() {
		Class artifact = new Class("ValueId", "de.test.domain.ValueId");
		artifact.addField(new Field("private", "id", "java.lang.long"));
		artifact.addMethod(new Method("public", "getId", "java.lang.long getId()"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
	}
	
	@Test
	void testSetTypeToValueObjectWithUnconventionalGetter() {
		Class artifact = new Class("Value", "de.test.domain.Value");
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
		artifact.addMethod(new Method("public", "nachname", "de.test.domain.Nachname nachname()"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
	}
	
	@Test
	void testSetTypeToValueObjectWithoutGetterAndSetter() {
		Class artifact = new Class("Value", "de.test.domain.Value");
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
	}
	
	@Test
	void testSetTypeToEntity() {
		Class entity = new Class("Entity", "de.test.domain.Entity");
		structureService.addClass(entity);
		
		Class artifact = new Class("Entitys", "de.test.domain.Entitys");
		artifact.addField(new Field("private", "other", ".de.Other"));
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		artifact.addMethod(new Method("public", "setName", "void setName(java.lang.String)"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.ENTITY, artifact.getType());
	}
	
	@Test
	void testSetTypeToEntityWithId() {
		Class artifact = new Class("Entity", "de.test.domain.Entity");
		artifact.addField(new Field("private", "other", ".de.Other"));
		artifact.addField(new Field("private", "id", "de.test.domain.EntityId"));
		artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		artifact.addMethod(new Method("public", "setName", "void setName(java.lang.String)"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.ENTITY, artifact.getType());
	}
	
	@Test
	void testSetTypeToEntityWithoutMethods() {
		Class artifact = new Class("Entity", "de.test.domain.Entity");
		artifact.addField(new Field("private", "other", "de.Other"));
		artifact.addField(new Field("private", "name", "java.lang.String"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.ENTITY, artifact.getType());
	}

	@Test
	void testSetTypeToApplicationService() {
		Class artifact = new Class("Main", "de.test.domain.Main");
		artifact.addMethod(new Method("public", "main", "void main(java.lang.String[])"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.APPLICATION_SERVICE, artifact.getType());
	}
	
	@Test
	void testSetTypeToService() {
		Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
		artifact.addField(new Field("private", "nameRepository", "de.test.domain.NameRepository"));
		artifact.addMethod(new Method("public", "generate", "void generate()"));
		structureService.addClass(artifact);
		
		Class entity1 = new Class("Entity", "de.test.domain.Entity");
		entity1.addField(new Field("private", "name", "java.lang.String"));
		structureService.addClass(entity1);
		
		Class entity2 = new Class("Name", "de.test.domain.Name");
		entity2.addField(new Field("private", "name", "java.lang.String"));
		structureService.addClass(entity2);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.SERVICE, artifact.getType());
	}
	
	@Test
	void testSetTypeToServiceWithConstant() {
		Class entity = new Class("Name", "de.test.domain.Name");
		entity.addField(new Field("private", "name", "java.lang.String"));
		structureService.addClass(entity);
		
		Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
		artifact.addField(new Field("private", "CONST", "java.lang.String"));
		artifact.addField(new Field("private", "nameRepository", "de.test.domain.NameRepository"));
		artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		artifact.addMethod(new Method("public", "generate", "void generate()"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.SERVICE, artifact.getType());
	}
	
	@Test
	void testSetTypeToInfrastructureWithJPA() {
		Class artifact = new Class("InfraJPA", "de.test.domain.InfraJPA");
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
	}
	
	@Test
	void testSetTypeToInfrastructureWithCRUD() {
		Class artifact = new Class("InfraCRUD", "de.test.domain.InfraCRUD");
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
	}
	
	@Test
	void testSetTypeToInfrastructure() {
		Class artifact = new Class("Entity", "de.test.domain.Entity");
		artifact.addField(new Field("private", "other", "de.Other"));
		artifact.addField(new Field("private", "name", "java.lang.String"));
		artifact.addMethod(new Method("public", "generate", "void generate()"));
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
	}
	
	@Test
	void testSetTypeToInfrastructureEmpty() {
		Class artifact = new Class("Infra", "de.test.domain.Infra");
		structureService.addClass(artifact);

		ClassAnalyseService service = new ClassAnalyseService(artifact, structureService);
		service.setType();

		assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
	}

	@Test
	void testSetDomainEvent() {
		Class artifact = new Class("Event", "de.test.domain.Event");
		artifact.setType(DDDType.ENTITY);
		artifact.addField(new Field("private", "timestamp", "java.lang.long"));
		artifact.addField(new Field("private", "date", "java.lang.String"));
		artifact.addField(new Field("private", "stamp", "java.time.Time"));
		artifact.addField(new Field("private", "EntityId", "java.lang.long"));
		artifact.addField(new Field("private", "desc", "java.lang.String"));

		ClassAnalyseService service = new ClassAnalyseService(artifact);
		service.setDomainEvent();
		
		assertEquals(DDDType.DOMAIN_EVENT, artifact.getType());
	}

	@Test
	void testSetDomainEventWithId() {
		Class artifact = new Class("Entity", "de.test.domain.Entity");
		artifact.setType(DDDType.AGGREGATE_ROOT);
		artifact.addField(new Field("private", "id", "java.lang.long"));

		ClassAnalyseService service = new ClassAnalyseService(artifact);
		service.setDomainEvent();
		
		assertEquals(DDDType.AGGREGATE_ROOT, artifact.getType());
	}

	@Test
	void testSetDomainEventWithDate() {
		Class artifact = new Class("Event", "de.test.domain.Event");
		artifact.setType(DDDType.VALUE_OBJECT);
		artifact.addField(new Field("private", "birth", "java.time.Time"));

		ClassAnalyseService service = new ClassAnalyseService(artifact);
		service.setDomainEvent();
		
		assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
	}

	@Test
	void testSetDomainEventOfOther() {
		Class artifact = new Class("Event", "de.test.domain.Event");
		artifact.setType(DDDType.FACTORY);

		ClassAnalyseService service = new ClassAnalyseService(artifact);
		service.setDomainEvent();
		
		assertEquals(DDDType.FACTORY, artifact.getType());
	}
}
