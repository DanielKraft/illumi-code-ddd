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
    private DDDFitness fitness;

    public InterfaceFitnessService(Interface artifact) {
        this.artifact = artifact;
        this.fitness = new DDDFitness();
    }

    public DDDFitness evaluate() {
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
        }
        return fitness;
    }

    private void evaluateRepository() {
        LOGGER.info("DDD:REPOSITORY:{}", artifact.getName());

        evaluateRepositoryName();

        evaluatePath();

        Method.evaluateRepository(artifact.getName(), artifact.getMethods(), fitness);
    }

    private void evaluateRepositoryName() {
        if (artifact.getName().endsWith(REPOSITORY)) {
            fitness.addSuccessfulCriteria(DDDIssueType.INFO);
        } else {
            fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the repository interface '%s' should end with 'Repository'", artifact.getName()));
        }
    }

    private void evaluatePath() {
        if (artifact.getPath().contains("domain." + artifact.getDomain() + ".model.")) {
            fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
        } else {
            fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The interface '%s' should be placed in 'domain.%s.model'", artifact.getName(), artifact.getDomain()));
        }
    }

    private void evaluateFactory() {
        LOGGER.info("DDD:FACTORY:{}", artifact.getName());

        evaluateFactoryName();

        evaluatePath();

        Field.evaluateFactory(artifact.getName(), artifact.getFields(), fitness);

        Method.evaluateFactory(artifact.getName(), artifact.getMethods(), fitness);
    }

    private void evaluateFactoryName() {
        if (artifact.getName().endsWith(FACTORY)) {
            fitness.addSuccessfulCriteria(DDDIssueType.INFO);
        } else {
            fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the factory interface '%s' should end with 'FactoryImpl'", artifact.getName()));
        }
    }
}
