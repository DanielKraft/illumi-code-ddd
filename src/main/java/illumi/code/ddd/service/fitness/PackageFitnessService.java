package illumi.code.ddd.service.fitness;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDIssueType;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.service.StructureService;

public class PackageFitnessService {

    private Package module;
    private StructureService structureService;

    public PackageFitnessService(Package module, StructureService structureService) {
        this.module = module;
        this.structureService = structureService;
    }

    public DDDFitness evaluate() {
        if (isDomainModule()) {
            return evaluateDomainModule();
        } else if (isInfrastructure()) {
            return evaluateInfrastructureModule();
        } else if (isApplication()) {
            return evaluateApplicationModule();
        } else {
            return new DDDFitness().addFailedCriteria(DDDIssueType.INFO, String.format("The module '%s' is no DDD-Module.", module.getName()));
        }
    }

    private boolean isDomainModule() {
        return structureService.getDomains().contains(module.getName());
    }

    private DDDFitness evaluateDomainModule() {
        DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);

        fitness.addIssue(module.getPath().contains(structureService.getPath() + "domain." + module.getName()), DDDIssueType.MINOR,
                String.format("The module '%s' is not a submodule of the module 'domain'.", module.getName()));

        fitness.addIssue(containsOnlyDomain(), DDDIssueType.MAJOR,
                String.format("The module '%s' dose not only contains domain artifacts.", module.getName()));

        fitness.addIssue(containsAggregateRoot(), DDDIssueType.CRITICAL,
                String.format("The module '%s' does not contain an Aggregate Root.", module.getName()));

        return fitness;
    }

    private boolean containsOnlyDomain() {
        for (Artifact artifact : module.getConataints()) {
            if (artifact.isTypeOf(DDDType.INFRASTRUCTURE)
                    || artifact.isTypeOf(DDDType.CONTROLLER)
                    || artifact.isTypeOf(DDDType.APPLICATION_SERVICE)) {
                return false;
            }
        }
        return true;
    }

    private boolean containsAggregateRoot() {
        for (Artifact artifact : module.getConataints()) {
            if (artifact.getType() == DDDType.AGGREGATE_ROOT) {
                return true;
            }
        }
        return false;
    }

    private boolean isInfrastructure() {
        for (Artifact artifact : module.getConataints()) {
            if (artifact.getType() == DDDType.INFRASTRUCTURE || artifact.getType() == DDDType.CONTROLLER) {
                return true;
            }
        }
        return false;
    }

    private DDDFitness evaluateInfrastructureModule() {
        DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);

        fitness.addIssue(module.getPath().contains(structureService.getPath() + "infrastructure"), DDDIssueType.MINOR,
                String.format("The module '%s' is not an infrastructure module.", module.getName()));

        fitness.addIssue(containsOnlyInfrastructure(), DDDIssueType.MAJOR,
                String.format("The module '%s' dose not only contains infrastructure artifacts.", module.getName()));

        return fitness;
    }

    private boolean containsOnlyInfrastructure() {
        for (Artifact artifact : module.getConataints()) {
            if (artifact.getType() != DDDType.INFRASTRUCTURE && artifact.getType() != DDDType.CONTROLLER) {
                return false;
            }
        }
        return true;
    }

    private boolean isApplication() {
        for (Artifact artifact : module.getConataints()) {
            if (artifact.getType() == DDDType.APPLICATION_SERVICE) {
                return true;
            }
        }
        return false;
    }

    private DDDFitness evaluateApplicationModule() {
        DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);

        fitness.addIssue(module.getPath().contains(structureService.getPath() + "application"), DDDIssueType.MINOR,
                String.format("The module '%s' is not an application module.", module.getName()));

        fitness.addIssue(containsOnlyApplication(), DDDIssueType.MAJOR,
                String.format("The module '%s' dose not only contains application artifacts.", module.getName()));

        return fitness;
    }

    private boolean containsOnlyApplication() {
        for (Artifact artifact : module.getConataints()) {
            if (artifact.getType() != DDDType.APPLICATION_SERVICE) {
                return false;
            }
        }
        return true;
    }
}
