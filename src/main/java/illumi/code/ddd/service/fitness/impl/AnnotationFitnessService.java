package illumi.code.ddd.service.fitness.impl;

import illumi.code.ddd.model.artifacts.Annotation;
import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.fitness.DDDIssueType;

public class AnnotationFitnessService {

  private Annotation annotation;
  private DDDFitness fitness;

  public AnnotationFitnessService(Annotation annotation) {
    this.annotation = annotation;
    this.fitness = new DDDFitness();
  }

  /**
   * Evaluates the fitness of an annotation.
   *
   * @return DDDFitness
   */
  public DDDFitness evaluate() {
    evaluateAnnotationPath();

    return fitness;
  }

  private void evaluateAnnotationPath() {
    fitness.addIssue(annotation.getPath().contains("infrastructure."), DDDIssueType.MINOR,
        String.format("The annotation '%s' is not part of an infrastructure module",
            annotation.getName()));
  }
}
