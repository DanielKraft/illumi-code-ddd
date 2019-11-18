package illumi.code.ddd.service.fitness;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDIssueType;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.service.StructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ClassFitnessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassFitnessService.class);

    private static final String REPOSITORY = "Repository";
    private static final String FACTORY = "Factory";

    private Class artifact;
    private StructureService structureService;

    public ClassFitnessService(Class artifact, StructureService structureService) {
        this.artifact = artifact;
        this.structureService = structureService;
    }

    public void evaluate() {
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
            case INFRASTRUCTUR:
            default:
                evaluateInfrastructure();
        }
    }

    private void evaluateEntity() {
        LOGGER.info("DDD:ENTITY:{}", artifact.getName());
        DDDFitness fitness = new DDDFitness();

        Field.evaluateEntity(artifact, structureService, fitness);

        Method.evaluateEntity(artifact, fitness);

        evaluateSuperClass(artifact.getSuperClass(), fitness);

        artifact.setFitness(fitness);
    }

    private void evaluateSuperClass(Class item, DDDFitness fitness) {
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
                fitness.addFailedCriteria(DDDIssueType.CRITICAL, String.format("The Entity '%s' does not containts an ID.", item.getName()));
            }

            Method.evaluateEntity(item, fitness);

            evaluateSuperClass(item.getSuperClass(), fitness);
        }

    }

    private void evaluateValueObject() {
        LOGGER.info("DDD:VALUE_OBJECT:{}", artifact.getName());
        // Must have criteria of Entity: no ID
        DDDFitness fitness = new DDDFitness();

        Field.evaluateValueObject(artifact, structureService, fitness);

        artifact.setFitness(fitness);
    }

    private void evaluateAggregateRoot() {
        LOGGER.info("DDD:AGGREGATE_ROOT:{}", artifact.getName());
        evaluateEntity();
        DDDFitness fitness = artifact.getDDDFitness();

        evaluateDomainStructure(fitness);

    }

    private void evaluateDomainStructure(DDDFitness fitness) {
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

        if (repoAvailable) {
            fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
        } else {
            fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("No repository of the aggregate root '%s' is available", artifact.getName()));
        }

        if (factoryAvailable) {
            fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
        } else {
            fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("No factory of the aggregate root '%s' is available", artifact.getName()));
        }

        if (serviceAvailable) {
            fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
        } else {
            fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("No service of the aggregate root '%s' is available", artifact.getName()));
        }
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
        for (Package module : structureService.getPackages()) {
            if (module.getName().contains(domain)) {
                return (ArrayList<Artifact>) module.getConataints();
            }
        }
        return new ArrayList<>();
    }

    private void evaluateRepository() {
        LOGGER.info("DDD:REPOSITORY:{}", artifact.getName());
        DDDFitness fitness = new DDDFitness();

        evaluateRepositoryName(fitness);

        evaluateRepositoryInterfaces(fitness);

        Method.evaluateRepository(artifact.getName(), artifact.getMethods(), fitness);

        artifact.setFitness(fitness);
    }

    private void evaluateRepositoryName(DDDFitness fitness) {
        if (artifact.getName().endsWith("RepositoryImpl")) {
            fitness.addSuccessfulCriteria(DDDIssueType.INFO);
        } else {
            fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the repsitory '%s' should end with 'RepositoryImpl'", artifact.getName()));
        }
    }

    private void evaluateRepositoryInterfaces(DDDFitness fitness) {
        boolean containtsInterface = false;
        for (Interface i : artifact.getInterfaces()) {
            if (i.getName().endsWith(REPOSITORY)) {
                containtsInterface = true;
                break;
            }
        }

        if (containtsInterface) {
            fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
        } else {
            fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The repsitory '%s' does not implement an interface.", artifact.getName()));
        }
    }

    private void evaluateFactory() {
        LOGGER.info("DDD:FACTORY:{}", artifact.getName());
        DDDFitness fitness = new DDDFitness();

        evaluateFactoryName(fitness);

        evaluateFactoryInterfaces(fitness);

        Field.evaluateFactory(artifact.getName(), artifact.getFields(), fitness);

        Method.evaluateFactory(artifact.getName(), artifact.getMethods(), fitness);

        artifact.setFitness(fitness);
    }

    private void evaluateFactoryName(DDDFitness fitness) {
        if (artifact.getName().endsWith("FactoryImpl")) {
            fitness.addSuccessfulCriteria(DDDIssueType.INFO);
        } else {
            fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the factory '%s' should end with 'FactoryImpl'", artifact.getName()));
        }
    }

    private void evaluateFactoryInterfaces(DDDFitness fitness) {
        boolean containtsInterface = false;
        for (Interface i : artifact.getInterfaces()) {
            if (i.getName().endsWith(FACTORY)) {
                containtsInterface = true;
                break;
            }
        }

        if (containtsInterface) {
            fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
        } else {
            fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The factory '%s' does not implement an interface.", artifact.getName()));
        }
    }

    private void evaluateService() {
        LOGGER.info("DDD:SERVICE:{}", artifact.getName());
        DDDFitness fitness = new DDDFitness();

        evaluateServiceName(fitness);

        evaluateServiceInterfaces(fitness);

        artifact.setFitness(fitness);
    }

    private void evaluateServiceName(DDDFitness fitness) {
        if (artifact.getName().endsWith("Impl")) {
            fitness.addSuccessfulCriteria(DDDIssueType.INFO);
        } else {
            fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the service '%s' should end with 'Impl'", artifact.getName()));
        }
    }

    private void evaluateServiceInterfaces(DDDFitness fitness) {
        boolean containtsInterface = false;
        for (Interface i : artifact.getInterfaces()) {
            if (artifact.getName().startsWith(i.getName())) {
                containtsInterface = true;
                break;
            }
        }

        if (containtsInterface) {
            fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
        } else {
            fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The service '%s' does not implement an interface.", artifact.getName()));
        }
    }

    private void evaluateApplicationService() {
        LOGGER.info("DDD:APPLICATION_SERVICE:{}", artifact.getName());
        DDDFitness fitness = new DDDFitness();

        if (artifact.getName().contains("Application")) {
            fitness.addSuccessfulCriteria(DDDIssueType.INFO);
        } else {
            fitness.addFailedCriteria(DDDIssueType.INFO, String.format("The name of the application service '%s' does not containts 'Application' ", artifact.getName()));
        }

        if (artifact.getPath().contains("application.")) {
            fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
        } else {
            fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The application service '%s' is not part of an application module", artifact.getName()));
        }

        artifact.setFitness(fitness);
    }

    private void evaluateInfrastructure() {
        LOGGER.info("DDD:INFRASTRUCTUR:{}", artifact.getName());
        if (artifact.getPath().contains("infrastructure.")) {
            artifact.setFitness(new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR));
        } else {
            artifact.setFitness(new DDDFitness().addFailedCriteria(DDDIssueType.MINOR, String.format("The infrastructure service '%s' is not part of an infrastructure module", artifact.getName())));
        }
    }

    private void evaluateDomainEvent() {
        LOGGER.info("DDD:DOMAIN_EVENT:{}", artifact.getName());
        DDDFitness fitness = new DDDFitness();

        Field.evaluateDomainEvent(artifact, fitness);

        artifact.setFitness(fitness);
    }
}
