package illumi.code.ddd.service.fitness;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
		service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 	module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A, 	module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(0, 		module.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 		module.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(1, 		module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateEmptyDomainModule() {
		Package module = new Package("domain1", "de.test.domain1");

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		service.evaluate();
		
		assertAll(	() -> assertEquals(42.86, 	module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, 	module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(7, 		module.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(3, 		module.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 		module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateDomainModuleWithRoot() {
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);
		
		Class aggregate = new Class("Root", "de.test.domain.domain1.Root");
		aggregate.setType(DDDType.AGGREGATE_ROOT);
		
		Package module = new Package("domain1", "de.test.domain.domain1");
		module.addConataints(entity);
		module.addConataints(aggregate);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 	module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A,		module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(7, 		module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(7, 		module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(0, 		module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInfrastructureModule() {
		Class infra = new Class("Infra", "de.test.infrastructure.Infra");
		infra.setType(DDDType.INFRASTRUCTUR);
		
		Package module = new Package("infrastructure", "de.test.infrastructure");
		module.addConataints(infra);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 	module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A,		module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 		module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(4, 		module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(0, 		module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidInfrastructureModule() {
		Class controller = new Class("Controller", "de.test.infra.Controller");
		controller.setType(DDDType.CONTROLLER);
		
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);
		
		Package module = new Package("infra", "de.test.infra");
		module.addConataints(controller);
		module.addConataints(entity);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		service.evaluate();
		
		assertAll(	() -> assertEquals(25.0, 	module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F,		module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 		module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(1, 		module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(2, 		module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}

	@Test
	void testEvaluateApplicationModule() {
		Class app = new Class("ApplicationService", "de.test.application.ApplicationService");
		app.setType(DDDType.APPLICATION_SERVICE);
		
		Package module = new Package("application", "de.test.application");
		module.addConataints(app);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 	module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A,		module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 		module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(4, 		module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(0, 		module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidApplicationModule() {
		Class app = new Class("ApplicationService", "de.test.app.ApplicationService");
		app.setType(DDDType.APPLICATION_SERVICE);
		
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);
		
		Package module = new Package("app", "de.test.app");
		module.addConataints(entity);
		module.addConataints(app);

		PackageFitnessService service = new PackageFitnessService(module, structureService);
		service.evaluate();
		
		assertAll(	() -> assertEquals(25.0, 	module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F,		module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 		module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(1, 		module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(2, 		module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
}



