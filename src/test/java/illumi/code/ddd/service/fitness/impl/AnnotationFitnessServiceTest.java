package illumi.code.ddd.service.fitness.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.artifacts.Annotation;
import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.fitness.DDDRating;

import org.junit.jupiter.api.Test;

class AnnotationFitnessServiceTest {

  @Test
  void testEvaluateAnnotation() {
    Annotation annotation = new Annotation("Annotation", "de.test.infrastructure.Annotation");

    AnnotationFitnessService service = new AnnotationFitnessService(annotation);
    final DDDFitness result = service.evaluate();

    assertAll(
        () -> assertEquals(100.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.A, result.getScore(), "Rating"),
        () -> assertEquals(1, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(1, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(0, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidAnnotation() {
    Annotation annotation = new Annotation("Annotation", "de.test.Annotation");

    AnnotationFitnessService service = new AnnotationFitnessService(annotation);
    final DDDFitness result = service.evaluate();

    assertAll(
        () -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(1, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(1, result.getIssues().size(), "#Issues"));
  }
}