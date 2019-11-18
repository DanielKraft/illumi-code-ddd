package illumi.code.ddd.model.artifacts;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.StructureService;

class PackageAnalyseTest {

	private StructureService structureService;
	
	@BeforeEach
	void init() {
		
		structureService = new StructureService();
		structureService.setPath("de.test");
		structureService.addDomain("domain");
	}
	
	@Test
	void testSetAggragateRoot() {
		Package module = new Package("domain", "de.test.domain");
		
		Class entity1 = new Class("Entity1", "de.test.domain.Entity1");
		entity1.setType(DDDType.ENTITY);
		entity1.addField(new Field("private", "entity2", "de.test.domain.Entity2"));
		entity1.addField(new Field("private", "entity4", "de.test.domain.Entity4"));
		module.addConataints(entity1);
		
		Class entity2 = new Class("Entity2", "de.test.domain.Entity2");
		entity2.setType(DDDType.ENTITY);
		module.addConataints(entity2);
		
		Class entity3 = new Class("Entity3", "de.test.domain.Entity3");
		entity3.setType(DDDType.ENTITY);
		entity3.addField(new Field("private", "entity2", "de.test.domain.Entity2"));
		entity3.addField(new Field("private", "valueObject", "de.test.domain.ValueObject"));
		module.addConataints(entity3);
		
		Class entity4 = new Class("Entity4", "de.test.domain.Entity4");
		entity4.setType(DDDType.ENTITY);
		module.addConataints(entity4);

		Class value = new Class("ValueObject", "de.test.domain.ValueObject");
		value.setType(DDDType.VALUE_OBJECT);
		module.addConataints(value);

		Class root = new Class("Root", "de.test.domain.Root");
		root.setType(DDDType.ENTITY);
		root.addField(new Field("private", "entity1", "de.test.domain.Entity1"));
		root.addField(new Field("private", "entity3", "de.test.domain.Entity3"));
		module.addConataints(root);
		
		module.setAggregateRoot(structureService);
		

		assertAll("Should find aggregate root",
			() -> assertEquals(DDDType.VALUE_OBJECT, value.getType(), 		"Value Object"),
			() -> assertEquals(DDDType.ENTITY, 			entity1.getType(), 	"Entity 1"),
			() -> assertEquals(DDDType.ENTITY, 			entity2.getType(), 	"Entity 1"),
			() -> assertEquals(DDDType.ENTITY, 			entity3.getType(), 	"Entity 1"),
			() -> assertEquals(DDDType.ENTITY, 			entity4.getType(), 	"Entity 1"),
			() -> assertEquals(DDDType.AGGREGATE_ROOT, 	root.getType(), 		"Aggregate Root"));
	}
	
	@Test
	void testSetAggragateRootWithMultipeCandidates() {
		Package module = new Package("domain", "de.test.domain");

		Class entity = new Class("Entity", "de.test.domain.Entity");
		entity.setType(DDDType.ENTITY);
		module.addConataints(entity);

		Class root = new Class("Domain", "de.test.domain.Domain");
		root.setType(DDDType.ENTITY);
		module.addConataints(root);
		
		module.setAggregateRoot(structureService);
		

		assertAll("Should find aggregate root",
			() -> assertEquals(DDDType.ENTITY,			entity.getType(), 	"Entity"),
			() -> assertEquals(DDDType.AGGREGATE_ROOT,	root.getType(), 	"Aggregate Root"));
	}
	
	@Test
	void testSetAggragateRootWithOneCandidate() {
		Package module = new Package("domain", "de.test.domain");
		
		Class root = new Class("Root", "de.test.domain.Root");
		root.setType(DDDType.ENTITY);
		module.addConataints(root);
		
		System.out.println(module.getConataints().size());
		
		module.setAggregateRoot(structureService);
		

		assertAll("Should find aggregate root",
			() -> assertEquals(DDDType.AGGREGATE_ROOT, root.getType()));
	}
	
	@Test
	void testSetAggragateRootWithNoEntities() {
		Package module = new Package("domain", "de.test.domain");
		
		module.setAggregateRoot(structureService);
		

		assertAll("Should find aggregate root",
			() -> assertEquals(0, 	module.getConataints().size()));
	}
	
	@Test
	void testSetAggragateRootOfNoDomain() {
		Package module = new Package("infra", "de.test.infra");
		
		Class root = new Class("Root", "de.test.infra.Root");
		root.setType(DDDType.ENTITY);
		module.addConataints(root);
		
		module.setAggregateRoot(structureService);
		

		assertAll("Should find aggregate root",
			() -> assertEquals(DDDType.ENTITY, root.getType()));
	}
}
