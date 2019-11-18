package illumi.code.ddd.service.fitness;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.artifacts.Annotation;
import org.junit.jupiter.api.Test;

import illumi.code.ddd.model.DDDRating;

class AnnotationFitnessServiceTest {
	
	@Test
	void testEvaluateAnnotation() {
		Annotation annotation = new Annotation("Annotation", "de.test.infrastructure.Annotation");

		AnnotationFitnessService service = new AnnotationFitnessService(annotation);
		final DDDFitness result = service.evaluate();
		
		assertAll(	() -> assertEquals(100.0, 	result.calculateFitness(), 				"Fitness"),
				 	() -> assertEquals(DDDRating.A, 	result.getscore(), 						"Rating"),
				 	() -> assertEquals(1, 		result.getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(1, 		result.getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(0, 		result.getIssues().size(), 				"#Issues"));
	}
	
	@Test
	void testEvaluateInvalidAnnotation() {
		Annotation annotation = new Annotation("Annotation", "de.test.Annotation");

		AnnotationFitnessService service = new AnnotationFitnessService(annotation);
		final DDDFitness result = service.evaluate();
		
		assertAll(	() -> assertEquals(0.0, 	result.calculateFitness(), 				"Fitness"),
				 	() -> assertEquals(DDDRating.F, 	result.getscore(), 						"Rating"),
				 	() -> assertEquals(1, 		result.getNumberOfCriteria(), 			"Total Criteria"),
				 	() -> assertEquals(0, 		result.getNumberOfFulfilledCriteria(), 	"Fulfilled Criteria"),
				 	() -> assertEquals(1, 		result.getIssues().size(), 				"#Issues"));
	}
}