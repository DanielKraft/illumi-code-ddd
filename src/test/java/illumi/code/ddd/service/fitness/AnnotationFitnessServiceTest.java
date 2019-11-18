package illumi.code.ddd.service.fitness;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.artifacts.Annotation;
import org.junit.jupiter.api.Test;

import illumi.code.ddd.model.DDDRating;

class AnnotationFitnessServiceTest {
	
	@Test
	void testEvaluateAnnotation() {
		Annotation annotation = new Annotation("Annotation", "de.test.infrastructure.Annotation");

		AnnotationFitnessService service = new AnnotationFitnessService(annotation);
		service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 		annotation.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.A, 		annotation.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(1, 			annotation.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(1, 			annotation.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(0, 			annotation.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidAnnotation() {
		Annotation annotation = new Annotation("Annotation", "de.test.Annotation");

		AnnotationFitnessService service = new AnnotationFitnessService(annotation);
		service.evaluate();
		
		assertAll(	() -> assertEquals(0.0, 		annotation.getFitness(), 									"Fitness"),
				 	() -> assertEquals(DDDRating.F, annotation.getDDDFitness().getscore(), 						"Rating"),
				 	() -> assertEquals(1, 			annotation.getDDDFitness().getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 			annotation.getDDDFitness().getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(1, 			annotation.getDDDFitness().getIssues().size(), 				"#Issues"));
	}
}