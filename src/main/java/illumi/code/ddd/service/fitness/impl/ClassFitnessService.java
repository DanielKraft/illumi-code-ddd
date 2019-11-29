package illumi.code.ddd.service.fitness.impl;

import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.fitness.DDDIssueType;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.DDDStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ClassFitnessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassFitnessService.class);

    private static final String REPOSITORY = "Repository";
    private static final String FACTORY = "Factory";
    private static final String DOMAIN = "domain.%s.model.";

    private Class artifact;
    private DDDStructure structure;
    private DDDFitness fitness;

    public ClassFitnessService(Class artifact, DDDStructure structure) {
        this.artifact = artifact;
        this.structure = structure;
        this.fitness = new DDDFitness();
    }

    public DDDFitness evaluate() {
        switch(artifact.getType()) {
            case ENTITY:
                evaluateEntity();
                break;
            case VALUE_OBJECT:
                evaluateValueObject();
                break;
            case AGGREGATE_ROOT:
                evaluateAggregateRoot();
                break;
            case REPOSITORY:
                evaluateRepository();
                break;
            case FACTORY:
                evaluateFactory();
                break;
            case SERVICE:
                evaluateService();
                break;
            case DOMAIN_EVENT:
                evaluateDomainEvent();
                break;
            case APPLICATION_SERVICE:
                evaluateApplicationService();
                break;
            case CONTROLLER:
            case INFRASTRUCTURE:
            default:
                evaluateInfrastructure();
        }
        return fitness;
    }

    private void evaluatePath(String filter, String message) {
        fitness.addIssue(artifact.getPath().contains(filter)
                || artifact.getPath().contains(".domain.model"), DDDIssueType.MINOR, message);
    }

    private void evaluateEntity() {
        LOGGER.info("DDD:ENTITY:{}", artifact.getName());

        evaluatePath(String.format(DOMAIN, artifact.getDomain()),
                String.format("The Entity '%s' is not placed at 'domain.%s.model'",
                        artifact.getName(), artifact.getDomain()));

        Field.evaluateEntity(artifact, structure, fitness);

        Method.evaluateNeededMethods(artifact, fitness);

        evaluateSuperClass(artifact.getSuperClass());
    }

    private void evaluateSuperClass(Class item) {
        if (item != null) {
            boolean containsId = false;

            for (Field field : item.getFields()) {
                if (Field.isId(field)) {
                    containsId = true;
                    break;
                }
            }

            if (containsId) {
                fitness.addSuccessfulCriteria(DDDIssueType.CRITICAL);
            } else if (item.getSuperClass() == null) {
                fitness.addFailedCriteria(DDDIssueType.CRITICAL, String.format("The Entity '%s' does not contains an ID.", artifact.getName()));
            }

            Method.evaluateNeededMethods(item, fitness);

            evaluateSuperClass(item.getSuperClass());
        }

    }

    private void evaluateValueObject() {
        LOGGER.info("DDD:VALUE_OBJECT:{}", artifact.getName());

        evaluatePath(String.format(DOMAIN, artifact.getDomain()),
                String.format("The Value Object '%s' is not placed at 'domain.%s.model'", artifact.getName(), artifact.getDomain()));

        // Must have criteria of Entity: no ID
        Field.evaluateValueObject(artifact, structure, fitness);

        Method.evaluateNeededMethods(artifact, fitness);
    }

    private void evaluateAggregateRoot() {
        LOGGER.info("DDD:AGGREGATE_ROOT:{}", artifact.getName());
        evaluateEntity();

        evaluateDomainStructure();
    }

    private void evaluateDomainStructure() {
        boolean repoAvailable = false;
        boolean factoryAvailable = false;
        boolean serviceAvailable = false;

        for (Artifact tmpArtifact : getDomainModule(artifact.getDomain())) {
            if (isAggregateRootRepository(tmpArtifact)) {
                repoAvailable = true;
            } else if(isAggregateRootFactory(tmpArtifact) ) {
                factoryAvailable = true;
            } else if(isAggregateRootService(tmpArtifact)) {
                serviceAvailable = true;
            }
        }

        fitness.addIssue(repoAvailable, DDDIssueType.MAJOR,
                String.format("No repository of the aggregate root '%s' is available", artifact.getName()));

        fitness.addIssue(factoryAvailable, DDDIssueType.MAJOR,
                String.format("No factory of the aggregate root '%s' is available", artifact.getName()));

        fitness.addIssue(serviceAvailable, DDDIssueType.MAJOR,
                String.format("No service of the aggregate root '%s' is available", artifact.getName()));
    }

    private boolean isAggregateRootRepository(Artifact artifact) {
        return artifact.isTypeOf(DDDType.REPOSITORY) && artifact.getName().contains(this.artifact.getName() + REPOSITORY);
    }

    private boolean isAggregateRootFactory(Artifact artifact) {
        return artifact.isTypeOf(DDDType.FACTORY) && artifact.getName().contains(this.artifact.getName() + FACTORY);
    }

    private boolean isAggregateRootService(Artifact artifact) {
        return artifact.isTypeOf(DDDType.SERVICE) && artifact.getName().contains(this.artifact.getName());
    }

    private ArrayList<Artifact> getDomainModule(String domain) {
        for (Package module : structure.getPackages()) {
            if (module.getName().contains(domain)) {
                return (ArrayList<Artifact>) module.getContains();
            }
        }
        return new ArrayList<>();
    }

    private void evaluateDomainEvent() {
        LOGGER.info("DDD:DOMAIN_EVENT:{}", artifact.getName());

        evaluatePath(String.format(DOMAIN, artifact.getDomain()),
                String.format("The Domain Event '%s' is not placed at 'domain.%s.model'", artifact.getName(), artifact.getDomain()));

        Field.evaluateDomainEvent(artifact, fitness);
    }

    private void evaluateRepository() {
        LOGGER.info("DDD:REPOSITORY:{}", artifact.getName());
        evaluateRepositoryName();

        evaluatePath(String.format(DOMAIN + "impl.", artifact.getDomain()),
                String.format("The repository '%s' is not placed at 'domain.%s.model.impl'", artifact.getName(), artifact.getDomain()));

        evaluateRepositoryInterfaces();

        Method.evaluateRepository(artifact.getName(), artifact.getMethods(), fitness);
    }

    private void evaluateRepositoryName() {
        fitness.addIssue(artifact.getName().endsWith("RepositoryImpl"), DDDIssueType.INFO,
                String.format("The name of the repository '%s' should end with 'RepositoryImpl'", artifact.getName()));
    }

    private void evaluateRepositoryInterfaces() {
        boolean containsInterface = false;
        for (Interface i : artifact.getImplInterfaces()) {
            if (i.getName().endsWith(REPOSITORY)) {
                containsInterface = true;
                break;
            }
        }
        fitness.addIssue(containsInterface, DDDIssueType.MINOR,
                String.format("The repository '%s' does not implement an interface.", artifact.getName()));
    }

    private void evaluateFactory() {
        LOGGER.info("DDD:FACTORY:{}", artifact.getName());

        evaluateFactoryName();

        evaluatePath(String.format(DOMAIN + "impl.", artifact.getDomain()),
                String.format("The factory '%s' is not placed at 'domain.%s.model.impl'", artifact.getName(), artifact.getDomain()));

        evaluateFactoryInterfaces();

        Field.evaluateFactory(artifact.getName(), artifact.getFields(), fitness);

        Method.evaluateFactory(artifact.getName(), artifact.getMethods(), fitness);
    }

    private void evaluateFactoryName() {
        fitness.addIssue(artifact.getName().endsWith("FactoryImpl"), DDDIssueType.INFO,
                String.format("The name of the factory '%s' should end with 'FactoryImpl'", artifact.getName()));
    }

    private void evaluateFactoryInterfaces() {
        boolean containsInterface = false;
        for (Interface i : artifact.getImplInterfaces()) {
            if (i.getName().endsWith(FACTORY)) {
                containsInterface = true;
                break;
            }
        }
        fitness.addIssue(containsInterface, DDDIssueType.MINOR,
                String.format("The factory '%s' does not implement an interface.", artifact.getName()));
    }

    private void evaluateService() {
        LOGGER.info("DDD:SERVICE:{}", artifact.getName());

        evaluatePath("application." + artifact.getDomain() + ".",
                String.format("The service '%s' should be placed at 'application.%s'", artifact.getName(), artifact.getDomain()));
    }

    private void evaluateApplicationService() {
        LOGGER.info("DDD:APPLICATION_SERVICE:{}", artifact.getName());

        evaluateName();

        evaluatePath("application.",
                String.format("The application service '%s' is not part of an application module", artifact.getName()));
    }

    private void evaluateName() {
        fitness.addIssue(artifact.getName().contains("Application"), DDDIssueType.INFO,
                String.format("The name of the application service '%s' does not contains 'Application' ", artifact.getName()));
    }

    private void evaluateInfrastructure() {
        LOGGER.info("DDD:INFRASTRUCTURE:{}", artifact.getName());

        evaluatePath("infrastructure.",
                String.format("The infrastructure service '%s' is not part of an infrastructure module", artifact.getName()));
    }
}
