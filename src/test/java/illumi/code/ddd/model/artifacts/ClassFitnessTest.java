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
public class ClassFitnessTest {

	private StructureService structureService;
	
	@BeforeAll
	public void init() {
		structureService = new StructureService();
		structureService.setPath("de.test");
	}
	
	@Test
	public void testEvaluateEntity() {
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
		
		assertAll(	() -> assertEquals(87.5, 		artifact.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.B, artifact.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(8, 			artifact.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(7, 			artifact.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(1, 			artifact.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	public void testEvaluateEmptyEntity() {	
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
				 	() -> assertEquals(3, 			artifact.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			artifact.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 			artifact.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	public void testEvaluateInvalidEntity() {	
		Class artifact = new Class("Entity", "de.test.Entity");
		artifact.setType(DDDType.ENTITY);
				
		
		artifact.evaluate(structureService);
		
		assertAll(	() -> assertEquals(0.0, 		artifact.getFitness(), 										"Fitness"),
				 	() -> assertEquals(DDDRating.F, artifact.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(3, 			artifact.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			artifact.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(2, 			artifact.getDDDFitness().getIssues().size(), 				"#Issues"));
	}

}
