package illumi.code.ddd.service.fitness;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import illumi.code.ddd.model.DDDRating;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.StructureService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PackageFitnessServiceTest {
	
	private StructureService structureService;
	
	@BeforeAll
	void init() {
		structureService = new StructureService();
		structureService.setPath("de.test");
		structureService.addDomain("domain0");
		structureService.addDomain("domain1");
	}
	
	@Test
	void testEvaluateModule() {
		Package module = new Package("domain", "de.test.domain");

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 	result.calculateFitness(), 				"Fitness"),
				 	() -> assertEquals(DDDRating.A, 	result.getscore(), 						"Rating"),
				 	() -> assertEquals(0, 		result.getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 		result.getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(1, 		result.getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateEmptyDomainModule() {
		Package module = new Package("domain1", "de.test.domain1");

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();
		
		assertAll(	() -> assertEquals(40.0, 	result.calculateFitness(), 				"Fitness"),
				 	() -> assertEquals(DDDRating.E, 	result.getscore(), 						"Rating"),
				 	() -> assertEquals(10, 	result.getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(4, 		result.getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 		result.getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateDomainModuleWithRoot() {
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);
		
		Class aggregate = new Class("Root", "de.test.domain.domain1.Root");
		aggregate.setType(DDDType.AGGREGATE_ROOT);
		
		Package module = new Package("domain1", "de.test.domain.domain1");
		module.addContains(entity);
		module.addContains(aggregate);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 	result.calculateFitness(), 					"Fitness"),
				 	() -> assertEquals(DDDRating.A,		result.getscore(), 							"Rating"),
				 	() -> assertEquals(10, 	result.getNumberOfCriteria(), 				"#Total Criteria"),
				 	() -> assertEquals(10, 	result.getNumberOfFulfilledCriteria(),		"#Fulfilled Criteria"),
				 	() -> assertEquals(0, 		result.getIssues().size(), 					"#Issues"));
	}

	@Test
	void testEvaluateDomainModuleWithInfrastructure() {
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);

		Class infra = new Class("Infra", "de.test.domain.domain1.Infra");
		infra.setType(DDDType.INFRASTRUCTURE);

		Package module = new Package("domain1", "de.test.domain.domain1");
		module.addContains(entity);
		module.addContains(infra);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();

		assertAll(	() -> assertEquals(20.0, 	result.calculateFitness(), 				"Fitness"),
					() -> assertEquals(DDDRating.E,		result.getscore(), 						"Rating"),
					() -> assertEquals(10, 	result.getNumberOfCriteria(), 			"#Total Criteria"),
					() -> assertEquals(2, 		result.getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
					() -> assertEquals(2, 		result.getIssues().size(), 				"#Issues"));
	}

	@Test
	void testEvaluateDomainModuleWithApplicationService() {
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);

		Class app = new Class("App", "de.test.domain.domain1.App");
		app.setType(DDDType.APPLICATION_SERVICE);

		Package module = new Package("domain1", "de.test.domain.domain1");
		module.addContains(entity);
		module.addContains(app);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();

		assertAll(	() -> assertEquals(20.0, 	result.calculateFitness(), 				"Fitness"),
					() -> assertEquals(DDDRating.E,		result.getscore(), 						"Rating"),
					() -> assertEquals(10, 	result.getNumberOfCriteria(), 			"#Total Criteria"),
					() -> assertEquals(2, 		result.getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
					() -> assertEquals(2, 		result.getIssues().size(), 				"#Issues"));
	}

	@Test
	void testEvaluateDomainModuleWithController() {
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);

		Class controller = new Class("Controller", "de.test.domain.domain1.Controller");
		controller.setType(DDDType.CONTROLLER);

		Package module = new Package("domain1", "de.test.domain.domain1");
		module.addContains(entity);
		module.addContains(controller);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();

		assertAll(	() -> assertEquals(20.0, 	result.calculateFitness(), 				"Fitness"),
					() -> assertEquals(DDDRating.E,		result.getscore(), 						"Rating"),
					() -> assertEquals(10, 	result.getNumberOfCriteria(), 			"#Total Criteria"),
					() -> assertEquals(2, 		result.getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
					() -> assertEquals(2, 		result.getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInfrastructureModule() {
		Class infra = new Class("Infra", "de.test.infrastructure.Infra");
		infra.setType(DDDType.INFRASTRUCTURE);
		
		Package module = new Package("infrastructure", "de.test.infrastructure");
		module.addContains(infra);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 	result.calculateFitness(), 				"Fitness"),
				 	() -> assertEquals(DDDRating.A,		result.getscore(), 						"Rating"),
				 	() -> assertEquals(5, 		result.getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(5, 		result.getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(0, 		result.getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidInfrastructureModule() {
		Class controller = new Class("Controller", "de.test.infra.Controller");
		controller.setType(DDDType.CONTROLLER);
		
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);
		
		Package module = new Package("infra", "de.test.infra");
		module.addContains(controller);
		module.addContains(entity);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();
		
		assertAll(	() -> assertEquals(20.0, 	result.calculateFitness(), 				"Fitness"),
				 	() -> assertEquals(DDDRating.E,		result.getscore(), 						"Rating"),
				 	() -> assertEquals(5, 		result.getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(1, 		result.getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(2, 		result.getIssues().size(), 				"#Issues"));
	}

	@Test
	void testEvaluateApplicationModule() {
		Class app = new Class("ApplicationService", "de.test.application.ApplicationService");
		app.setType(DDDType.APPLICATION_SERVICE);
		
		Package module = new Package("application", "de.test.application");
		module.addContains(app);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 	result.calculateFitness(), 				"Fitness"),
				 	() -> assertEquals(DDDRating.A,		result.getscore(), 						"Rating"),
				 	() -> assertEquals(5, 		result.getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(5, 		result.getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(0, 		result.getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidApplicationModule() {
		Class app = new Class("ApplicationService", "de.test.app.ApplicationService");
		app.setType(DDDType.APPLICATION_SERVICE);
		
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);
		
		Package module = new Package("app", "de.test.app");
		module.addContains(entity);
		module.addContains(app);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		final DDDFitness result = service.evaluate();
		
		assertAll(	() -> assertEquals(20.0, 	result.calculateFitness(), 				"Fitness"),
				 	() -> assertEquals(DDDRating.E,		result.getscore(), 						"Rating"),
				 	() -> assertEquals(5, 		result.getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(1, 		result.getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(2, 		result.getIssues().size(), 				"#Issues"));
	}
}



