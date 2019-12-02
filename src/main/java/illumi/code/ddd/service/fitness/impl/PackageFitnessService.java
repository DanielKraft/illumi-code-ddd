package illumi.code.ddd.service.fitness.impl;

import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.fitness.DDDIssueType;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.DDDStructure;

public class PackageFitnessService {

    private Package module;
    private DDDStructure structure;

    public PackageFitnessService(Package module, DDDStructure structure) {
        this.module = module;
        this.structure = structure;
    }

    public DDDFitness evaluate() {
        if (isModelModule()) {
            return new DDDFitness();
        } else if (isDomainModule()) {
            return evaluateDomainModule();
        } else if (isInfrastructure()) {
            return evaluateInfrastructureModule();
        } else if (isApplication()) {
            return evaluateApplicationModule();
        } else {
            return new DDDFitness().addFailedCriteria(DDDIssueType.INFO, String.format("The module '%s' is no DDD-Module.", module.getName()));
        }
    }

    private boolean isModelModule() {
        return module.getPath().endsWith(".domain.model");
    }

    private boolean isDomainModule() {
        return structure.getDomains().contains(module.getName())
                && !module.getPath().contains("application")
                && !module.getPath().contains("infrastructure");
    }

    private DDDFitness evaluateDomainModule() {
        DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);

        fitness.addIssue(module.getPath().contains(structure.getPath() + "domain." + module.getName()), DDDIssueType.MINOR,
                String.format("The module '%s' is not a submodule of the module 'domain'.", module.getName()));

        fitness.addIssue(containsOnlyDomain(), DDDIssueType.MAJOR,
                String.format("The module '%s' dose not only contains domain artifacts.", module.getName()));

        fitness.addIssue(containsAggregateRoot(module), DDDIssueType.CRITICAL,
                String.format("The module '%s' does not contain an Aggregate Root.", module.getName()));

        return fitness;
    }

    private boolean containsOnlyDomain() {
        for (Artifact artifact : module.getContains()) {
            if (artifact.isTypeOf(DDDType.INFRASTRUCTURE)
                    || artifact.isTypeOf(DDDType.CONTROLLER)
                    || artifact.isTypeOf(DDDType.APPLICATION_SERVICE)) {
                return false;
            }
        }
        return true;
    }

    private boolean containsAggregateRoot(Package module) {
        boolean contains = false;
        for (Artifact artifact : module.getContains()) {
            if (artifact.getType() == DDDType.AGGREGATE_ROOT) {
                return true;
            } else if (artifact instanceof Package) {
                contains = contains
                        || containsAggregateRoot((Package) artifact);
            }
        }
        return contains;
    }

    private boolean isInfrastructure() {
        for (Artifact artifact : module.getContains()) {
            if (artifact.getType() == DDDType.INFRASTRUCTURE || artifact.getType() == DDDType.CONTROLLER) {
                return true;
            }
        }
        return false;
    }

    private DDDFitness evaluateInfrastructureModule() {
        DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);

        fitness.addIssue(module.getPath().contains(structure.getPath() + "infrastructure"), DDDIssueType.MINOR,
                String.format("The module '%s' is not an infrastructure module.", module.getName()));

        fitness.addIssue(containsOnlyInfrastructure(), DDDIssueType.MAJOR,
                String.format("The module '%s' dose not only contains infrastructure artifacts.", module.getName()));

        return fitness;
    }

    private boolean containsOnlyInfrastructure() {
        for (Artifact artifact : module.getContains()) {
            if (artifact.getType() != DDDType.INFRASTRUCTURE && artifact.getType() != DDDType.CONTROLLER) {
                return false;
            }
        }
        return true;
    }

    private boolean isApplication() {
        for (Artifact artifact : module.getContains()) {
            if (artifact.getType() == DDDType.APPLICATION_SERVICE) {
                return true;
            }
        }
        return false;
    }

    private DDDFitness evaluateApplicationModule() {
        DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);

        fitness.addIssue(module.getPath().contains(structure.getPath() + "application"), DDDIssueType.MINOR,
                String.format("The module '%s' is not an application module.", module.getName()));

        fitness.addIssue(containsOnlyApplication(), DDDIssueType.MAJOR,
                String.format("The module '%s' dose not only contains application artifacts.", module.getName()));

        return fitness;
    }

    private boolean containsOnlyApplication() {
        for (Artifact artifact : module.getContains()) {
            if (!artifact.isTypeOf(DDDType.APPLICATION_SERVICE)
                    && !artifact.isTypeOf(DDDType.MODULE)) {
                return false;
            }
        }
        return true;
    }
}
