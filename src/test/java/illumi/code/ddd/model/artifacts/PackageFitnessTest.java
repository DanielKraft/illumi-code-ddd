package illumi.code.ddd.model.artifacts;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import illumi.code.ddd.model.DDDRating;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.StructureService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PackageFitnessTest {
	
	private StructureService structureService;
	
	@BeforeAll
	public void init() {
		structureService = new StructureService();
		structureService.setPath("de.test");
		structureService.addDomain("domain0");
		structureService.addDomain("domain1");
	}
	
	@Test
	public void testEvaluateModule() {
		Package module = new Package("domain", "de.test.domain");
		
		module.evaluate(structureService);
		
		assertAll(	() -> assertEquals(100.0, 		module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A, module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(0, 			module.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			module.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(1, 			module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	public void testEvaluateEmptyDomainModule() {
		Package module = new Package("domain1", "de.test.domain1");
		
		module.evaluate(structureService);
		
		assertAll(	() -> assertEquals(25.0, 		module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 			module.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(1, 			module.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 			module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	public void testEvaluateDomainModuleWithRoot() {
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);
		
		Class aggregate = new Class("Root", "de.test.domain.domain1.Root");
		aggregate.setType(DDDType.AGGREGATE_ROOT);
		
		Package module = new Package("domain1", "de.test.domain.domain1");
		module.addConataints(entity);
		module.addConataints(aggregate);
		
		module.evaluate(structureService);
		
		assertAll(	() -> assertEquals(100.0, 		module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A,	module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 			module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(4, 			module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(0, 			module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	public void testEvaluateInfrastructureModule() {
		Class infra = new Class("Infra", "de.test.infrastructure.Infra");
		infra.setType(DDDType.INFRASTRUCTUR);
		
		Package module = new Package("infrastructure", "de.test.infrastructure");
		module.addConataints(infra);
		
		module.evaluate(structureService);
		
		assertAll(	() -> assertEquals(100.0, 		module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A,	module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 			module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(4, 			module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(0, 			module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	public void testEvaluateInvalidInfrastructureModule() {
		Class controller = new Class("Controller", "de.test.infra.Controller");
		controller.setType(DDDType.CONTROLLER);
		
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);
		
		Package module = new Package("infra", "de.test.infra");
		module.addConataints(controller);
		module.addConataints(entity);
		
		module.evaluate(structureService);
		
		assertAll(	() -> assertEquals(25.0, 		module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F,	module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 			module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(1, 			module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(2, 			module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}

	@Test
	public void testEvaluateApplicationModule() {
		Class app = new Class("ApplicationService", "de.test.application.ApplicationService");
		app.setType(DDDType.APPLICATION_SERVICE);
		
		Package module = new Package("application", "de.test.application");
		module.addConataints(app);
		
		module.evaluate(structureService);
		
		assertAll(	() -> assertEquals(100.0, 		module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A,	module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 			module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(4, 			module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(0, 			module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	public void testEvaluateInvalidApplicationModule() {
		Class app = new Class("ApplicationService", "de.test.app.ApplicationService");
		app.setType(DDDType.APPLICATION_SERVICE);
		
		Class entity = new Class("Entity", "de.test.domain.domain1.Entity");
		entity.setType(DDDType.ENTITY);
		
		Package module = new Package("app", "de.test.app");
		module.addConataints(entity);
		module.addConataints(app);
		
		module.evaluate(structureService);
		
		assertAll(	() -> assertEquals(25.0, 		module.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F,	module.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(4, 			module.getDDDFitness().getNumberOfCriteria(), 			"#Total Criteria"),
				 	() -> assertEquals(1, 			module.getDDDFitness().getNumberOfFulfilledCriteria(),	"#Fulfilled Criteria"),
				 	() -> assertEquals(2, 			module.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
}



