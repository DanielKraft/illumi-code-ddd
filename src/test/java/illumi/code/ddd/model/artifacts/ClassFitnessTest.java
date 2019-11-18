package illumi.code.ddd.model.artifacts;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import illumi.code.ddd.model.DDDRating;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.StructureService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassFitnessTest {

	private StructureService structureService;
	
	@BeforeEach
	void init() {
		structureService = new StructureService();
		structureService.setPath("de.test");
	}
	
	@Test
	void testEvaluateEntity() {
		Class superClass = new Class("Super", "de.test.Super");
		superClass.addField(new Field("private", "id", "de.test.EntityId"));
		superClass.addField(new Field("private", "name", "string"));
		superClass.addMethod(new Method("public", "equals", "boolean equals()"));
		superClass.addMethod(new Method("public", "hashCode", "int hashCode()"));
		superClass.addMethod(new Method("public", "toString", "test toString()"));
		
		Class artifact = new Class("Entity", "de.test.Entity");
		artifact.setType(DDDType.ENTITY);
		artifact.addField(new Field("private", "id", "de.test.EntityId"));
		artifact.addField(new Field("private", "name", "string"));
		artifact.addMethod(new Method("public", "equals", "boolean equals()"));
		artifact.addMethod(new Method("public", "hashCode", "int hashCode()"));
		artifact.addMethod(new Method("public", "toString", "test toString()"));
		artifact.addSuperClass(superClass);
				
		
		artifact.evaluate(structureService);
		
		assertAll(	() -> assertEquals(83.33, 		artifact.getFitness(), 										"Fitness"),
				 	() -> assertEquals(DDDRating.B, artifact.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(12, 			artifact.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(10, 			artifact.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(1, 			artifact.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateEmptyEntity() {
		Class superSuperClass = new Class("SuperSuper", "de.test.SuperSuper");
		superSuperClass.addField(new Field("private", "name", "string"));
		
		Class superClass = new Class("Super", "de.test.Super");
		superClass.addSuperClass(superSuperClass);

		Class artifact = new Class("Entity", "de.test.Entity");
		artifact.setType(DDDType.ENTITY);
		artifact.addSuperClass(superClass);
				
		
		artifact.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		artifact.getFitness(), 										"Fitness"),
				 	() -> assertEquals(DDDRating.F, artifact.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 			artifact.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			artifact.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 			artifact.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidEntity() {
		Class artifact = new Class("Entity", "de.test.Entity");
		artifact.setType(DDDType.ENTITY);
				
		
		artifact.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		artifact.getFitness(), 										"Fitness"),
				 	() -> assertEquals(DDDRating.F, artifact.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 			artifact.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			artifact.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 			artifact.getDDDFitness().getIssues().size(), 				"#Issues"));
	}

	@Test
	void testEvaluateValueObjects() {
		Class valueObject = new Class("ValueObject", "de.test.ValueObject");
		valueObject.setType(DDDType.VALUE_OBJECT);
		valueObject.addField(new Field("private", "name", "java.lang.string"));
		valueObject.addField(new Field("private", "value", "de.test.Value"));
		valueObject.addMethod(new Method("public", "getName", "java.lang.string getName()"));
		valueObject.addMethod(new Method("public", "setName", "de.test.ValueObject setName()"));
		valueObject.addMethod(new Method("public", "setValue", "void setValue(de.test.Value)"));
		valueObject.addMethod(new Method("public", "toString", "test toString()"));
				
		
		valueObject.evaluate(structureService);
		
		assertAll(	() -> assertEquals(77.78, 		valueObject.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.C, valueObject.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(18, 			valueObject.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(14, 			valueObject.getDDDFitness().getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
				 	() -> assertEquals(2, 			valueObject.getDDDFitness().getIssues().size(), 			"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidValueObjects() {
		Class valueObject = new Class("ValueObject", "de.test.ValueObject");
		valueObject.setType(DDDType.VALUE_OBJECT);
		valueObject.addField(new Field("private", "id", "long"));
				
		
		valueObject.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		valueObject.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, valueObject.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(8, 			valueObject.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			valueObject.getDDDFitness().getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
				 	() -> assertEquals(4, 			valueObject.getDDDFitness().getIssues().size(), 			"#Issues"));
	}
	
	@Test
	void testEvaluateAggregateRoot() {
		Package infra = new Package("infrastructure", "de.test.infrastructure");
		infra.setType(DDDType.MODULE);
		structureService.addPackage(infra);
		
		Package domain = new Package("aggregate", "de.test.aggregate");
		domain.setType(DDDType.MODULE);
		structureService.addPackage(domain);
		
		Class aggregate = new Class("Aggregate", "de.test.aggregate.Aggregate");
		aggregate.setType(DDDType.AGGREGATE_ROOT);
		aggregate.setDomain("aggregate");
		domain.addConataints(aggregate);
		
		Class repository = new Class("AggregateRepository", "de.test.aggregate.AggregateRepository");
		repository.setType(DDDType.REPOSITORY);
		repository.setDomain("aggregate");
		domain.addConataints(repository);
		
		Class factory = new Class("AggregateFactory", "de.test.aggregate.AggregateFactory");
		factory.setType(DDDType.FACTORY);
		factory.setDomain("aggregate");
		domain.addConataints(factory);
		
		Class service = new Class("AggregateService", "de.test.aggregate.AggregateService");
		service.setType(DDDType.SERVICE);
		service.setDomain("aggregate");
		domain.addConataints(service);
		
		aggregate.evaluate(structureService);
		
		assertAll(	() -> assertEquals(60.0, 		aggregate.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.D, aggregate.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(10, 			aggregate.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(6, 			aggregate.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 			aggregate.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidAggregateRoot() {
		Package domain = new Package("aggregate", "de.test.aggregate");
		domain.setType(DDDType.MODULE);
		structureService.addPackage(domain);
		
		Class aggregate = new Class("Aggregate", "de.test.aggregate.Aggregate");
		aggregate.setType(DDDType.AGGREGATE_ROOT);
		aggregate.setDomain("aggregate");
		domain.addConataints(aggregate);
		
		Class repository = new Class("Repository", "de.test.aggregate.Repository");
		repository.setType(DDDType.REPOSITORY);
		repository.setDomain("aggregate");
		domain.addConataints(repository);
		
		Class factory = new Class("Factory", "de.test.aggregate.Factory");
		factory.setType(DDDType.FACTORY);
		factory.setDomain("aggregate");
		domain.addConataints(factory);
		
		Class service = new Class("Service", "de.test.aggregate.Service");
		service.setType(DDDType.SERVICE);
		service.setDomain("aggregate");
		domain.addConataints(service);
		
		aggregate.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		aggregate.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, aggregate.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(10, 			aggregate.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			aggregate.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(5, 			aggregate.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateEmptyAggregateRoot() {
		Class aggregate = new Class("Aggregate", "de.test.aggregate.Aggregate");
		aggregate.setType(DDDType.AGGREGATE_ROOT);
		aggregate.setDomain("aggregate");
		
		aggregate.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		aggregate.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, aggregate.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(10, 			aggregate.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			aggregate.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(5, 			aggregate.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateRepository() {
		Class repository = new Class("TestRepositoryImpl", "de.test.domain.TestRepositoryImpl");
		repository.setType(DDDType.REPOSITORY);
		repository.setDomain("domain");
		repository.addMethod(new Method("public", "findById", "test findById()"));
		repository.addMethod(new Method("public", "getId", "test getId()"));
		repository.addMethod(new Method("public", "save", "test save()"));
		repository.addMethod(new Method("public", "add", "test add()"));
		repository.addMethod(new Method("public", "insert", "test insert()"));
		repository.addMethod(new Method("public", "delete", "test delete()"));
		repository.addMethod(new Method("public", "remove", "test remove()"));
		repository.addMethod(new Method("public", "contains", "test contains()"));
		repository.addMethod(new Method("public", "exists", "test exists()"));
		repository.addMethod(new Method("public", "update", "test update()"));
		repository.addMethod(new Method("public", "toString", "test toString()"));
		
		Interface repo = new Interface("TestRepository", "de.test.domain.TestRepository");
		repo.setType(DDDType.REPOSITORY);
		repo.setDomain("domain");
		repository.addImplInterface(repo);
		
		Interface service = new Interface("Service", "de.test.domain.Service");
		service.setType(DDDType.SERVICE);
		service.setDomain("domain");
		repository.addImplInterface(service);
		
		repository.evaluate(structureService);
		
		assertAll(	() -> assertEquals(100.0, 		repository.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A, repository.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(9, 			repository.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(9, 			repository.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(0, 			repository.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidRepository() {
		Class repository = new Class("TestRepository", "de.test.domain.TestRepository");
		repository.setType(DDDType.REPOSITORY);
		repository.setDomain("domain");
		
		repository.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		repository.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, repository.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(9, 			repository.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			repository.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(7, 			repository.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateFactory() {
		Class factoryImpl = new Class("TestFactoryImpl", "de.test.TestFactoryImpl");
		factoryImpl.setType(DDDType.FACTORY);
		factoryImpl.addField(new Field("private", "repository", "de.test.Repository"));
		factoryImpl.addField(new Field("private", "name", "string"));
		factoryImpl.addMethod(new Method("public", "create", "test create()"));
		factoryImpl.addMethod(new Method("public", "toString", "test toString()"));
		
		Interface factory = new Interface("TestFactory", "de.test.domain.TestFactory");
		factory.setType(DDDType.REPOSITORY);
		factory.setDomain("domain");
		factoryImpl.addImplInterface(factory);
		
		Interface service = new Interface("Service", "de.test.domain.Service");
		service.setType(DDDType.SERVICE);
		service.setDomain("domain");
		factoryImpl.addImplInterface(service);
		
		factoryImpl.evaluate(structureService);
		
		assertAll(	() -> assertEquals(100.0, 		factoryImpl.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A, factoryImpl.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(5, 			factoryImpl.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(5, 			factoryImpl.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(0, 			factoryImpl.getDDDFitness().getIssues().size(), 			"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidFactory() {
		Class factoryImpl = new Class("Fac", "de.test.Fac");
		factoryImpl.setType(DDDType.FACTORY);
		factoryImpl.addField(new Field("private", "name", "string"));
		factoryImpl.addMethod(new Method("public", "toString", "test toString()"));
		
		factoryImpl.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		factoryImpl.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, factoryImpl.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(5, 			factoryImpl.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			factoryImpl.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(4, 			factoryImpl.getDDDFitness().getIssues().size(), 			"#Issues"));
	}
	
	@Test
	void testEvaluateService() {
		Class serviceImpl = new Class("TestImpl", "de.test.TestImpl");
		serviceImpl.setType(DDDType.SERVICE);
		
		Interface service = new Interface("Test", "de.test.domain.Test");
		service.setType(DDDType.SERVICE);
		service.setDomain("domain");
		serviceImpl.addImplInterface(service);
		
		Interface repo = new Interface("Service", "de.test.domain.Service");
		repo.setType(DDDType.SERVICE);
		repo.setDomain("domain");
		serviceImpl.addImplInterface(repo);
		
		serviceImpl.evaluate(structureService);
		
		assertAll(	() -> assertEquals(100.0, 		serviceImpl.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A, serviceImpl.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(1, 			serviceImpl.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(1, 			serviceImpl.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(0, 			serviceImpl.getDDDFitness().getIssues().size(), 			"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidService() {
		Class serviceImpl = new Class("Test", "de.test.Test");
		serviceImpl.setType(DDDType.SERVICE);
		
		serviceImpl.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		serviceImpl.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, serviceImpl.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(1, 			serviceImpl.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			serviceImpl.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 			serviceImpl.getDDDFitness().getIssues().size(), 			"#Issues"));
	}
	
	@Test
	void testEvaluateApplicationServiceWithInvalidName() {
		Class app = new Class("TestApplication", "de.test.application.TestApplication");
		app.setType(DDDType.APPLICATION_SERVICE);
		
		app.evaluate(structureService);
		
		assertAll(	() -> assertEquals(100.0, 		app.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A, app.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(1, 			app.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(1, 			app.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(0, 			app.getDDDFitness().getIssues().size(), 			"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidApplicationService() {
		Class app = new Class("Test", "de.test.Test");
		app.setType(DDDType.APPLICATION_SERVICE);
		
		app.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		app.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, app.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(1, 			app.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			app.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 			app.getDDDFitness().getIssues().size(), 			"#Issues"));
	}

	@Test
	void testEvaluateInfrastructure() {
		Class app = new Class("Test", "de.test.infrastructure.Test");
		app.setType(DDDType.INFRASTRUCTUR);
		
		app.evaluate(structureService);
		
		assertAll(	() -> assertEquals(100.0, 		app.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A, app.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(1, 			app.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(1, 			app.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(0, 			app.getDDDFitness().getIssues().size(), 			"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidInfrastructure() {
		Class app = new Class("Test", "de.test.Test");
		app.setType(DDDType.INFRASTRUCTUR);
		
		app.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		app.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, app.getDDDFitness().getscore(), 					"Rating"),
				 	() -> assertEquals(1, 			app.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			app.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(1, 			app.getDDDFitness().getIssues().size(), 			"#Issues"));
	}
	
	@Test
	void testEvaluateDomainEvent() {
		Class event = new Class("Event", "de.test.domain.Event");
		event.setType(DDDType.DOMAIN_EVENT);
		event.addField(new Field("private", "name", "java.lang.String"));
		event.addField(new Field("private", "id", "de.test.Id"));
		event.addField(new Field("private", "time", "java.lang.long"));
		event.addField(new Field("private", "date", "java.lang.long"));
		event.addField(new Field("private", "test1", "java.time."));
		event.addField(new Field("private", "test2", "de.test.Value"));
		event.addMethod(new Method("public", "getTime", "java.lang.long getTime()"));
		event.addMethod(new Method("public", "setDate", "java.lang.long setDate()"));
		event.addMethod(new Method("public", "toString", "test toString()"));
		
		event.evaluate(structureService);
		
		assertAll(	() -> assertEquals(68.75, 		event.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.D, event.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(16, 			event.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(11, 			event.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(4, 			event.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidDomainEvent() {
		Class event = new Class("Event", "de.test.domain.Event");
		event.setType(DDDType.DOMAIN_EVENT);
		
		event.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		event.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, event.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 			event.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			event.getDDDFitness().getNumberOfFulfilledCriteria(),	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 			event.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
}
