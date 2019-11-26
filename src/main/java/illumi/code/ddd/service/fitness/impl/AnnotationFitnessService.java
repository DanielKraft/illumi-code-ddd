package illumi.code.ddd.service.fitness.impl;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDIssueType;
import illumi.code.ddd.model.artifacts.Annotation;

public class AnnotationFitnessService {

    private Annotation annotation;
    private DDDFitness fitness;

    public AnnotationFitnessService(Annotation annotation) {
        this.annotation = annotation;
        this.fitness = new DDDFitness();
    }

    public DDDFitness evaluate() {
        evaluateAnnotationPath();

        return fitness;
    }

    private void evaluateAnnotationPath() {
        fitness.addIssue(annotation.getPath().contains("infrastructure."), DDDIssueType.MINOR,
                String.format("The annotation '%s' is not part of an infrastructure module", annotation.getName()));
    }
}
