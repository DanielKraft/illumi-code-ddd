package illumi.code.ddd.service.fitness;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDIssueType;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterfaceFitnessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceFitnessService.class);

    private static final String REPOSITORY = "Repository";
    private static final String FACTORY = "Factory";

    private Interface artifact;

    public InterfaceFitnessService(Interface artifact) {
        this.artifact = artifact;
    }

    public void evaluate() {
        switch(artifact.getType()) {
            case REPOSITORY:
                evaluateRepository();
                break;
            case FACTORY:
                evaluateFactory();
                break;
            case SERVICE:
            default:
                LOGGER.info("DDD:SERVICE:{}", artifact.getName());
                artifact.setFitness(new DDDFitness());
        }
    }

    private void evaluateRepository() {
        LOGGER.info("DDD:REPOSITORY:{}", artifact.getName());
        DDDFitness fitness = new DDDFitness();

        evaluateRepositoryName(fitness);

        Method.evaluateRepository(artifact.getName(), artifact.getMethods(), fitness);

        artifact.setFitness(fitness);
    }

    private void evaluateRepositoryName(DDDFitness fitness) {
        if (artifact.getName().endsWith(REPOSITORY)) {
            fitness.addSuccessfulCriteria(DDDIssueType.INFO);
        } else {
            fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the repsitory interface '%s' should end with 'Repository'", artifact.getName()));
        }
    }

    private void evaluateFactory() {
        LOGGER.info("DDD:FACTORY:{}", artifact.getName());
        DDDFitness fitness = new DDDFitness();

        evaluateFactoryName(fitness);

        Field.evaluateFactory(artifact.getName(), artifact.getFields(), fitness);

        Method.evaluateFactory(artifact.getName(), artifact.getMethods(), fitness);

        artifact.setFitness(fitness);
    }

    private void evaluateFactoryName(DDDFitness fitness) {
        if (artifact.getName().endsWith(FACTORY)) {
            fitness.addSuccessfulCriteria(DDDIssueType.INFO);
        } else {
            fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the factory interface '%s' should end with 'FactoryImpl'", artifact.getName()));
        }
    }
}
