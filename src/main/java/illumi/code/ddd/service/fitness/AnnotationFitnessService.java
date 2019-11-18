package illumi.code.ddd.service.fitness;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDIssueType;
import illumi.code.ddd.model.artifacts.Annotation;

public class AnnotationFitnessService {

    private Annotation annotation;

    public AnnotationFitnessService(Annotation annotation) {
        this.annotation = annotation;
    }

    public void evaluate() {
        if (annotation.getPath().contains("infrastructure.")) {
            annotation.setFitness(new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR));
        } else {
            annotation.setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, String.format("The annotation '%s' is not part of an infrastructure module", annotation.getName())));
        }
    }
}
