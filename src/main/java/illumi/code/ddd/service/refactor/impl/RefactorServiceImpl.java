package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.service.refactor.RefactorService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class RefactorServiceImpl implements RefactorService {

    private DDDRefactorData refactorData;

    public @Inject RefactorServiceImpl() {
        // Empty
    }

    @Override
    public void setOldStructure(DDDStructure oldStructure) {
        this.refactorData = new DDDRefactorData(oldStructure);
    }

    @Override
    public DDDStructure refactor() {

        new InitializeService(refactorData).initModules();
        new AssignService(refactorData).assign();

        new EntityRefactorService(refactorData).refactor();
        new ValueObjectRefactorService(refactorData).refactor();
        new DomainEventRefactorService(refactorData).refactor();
        new RepositoryRefactorService(refactorData).refactor();
        new FactoryRefactorService(refactorData).refactor();
        new AggregateRootRefactorService(refactorData).refactor();

        deleteEmptyModules(refactorData.getNewStructure().getStructure());
        cleanFitness();

        refactorDependencies();

        return refactorData.getNewStructure();
    }

    private void deleteEmptyModules(List<Artifact> structure) {
        for (Artifact artifact : new ArrayList<>(structure)) {
            if (artifact instanceof Package) {
                if (((Package) artifact).getContains().isEmpty()) {
                    structure.remove(artifact);
                } else {
                    deleteEmptyModules(((Package) artifact).getContains());
                }
            }
        }
    }

    private void cleanFitness() {
        refactorData.getNewStructure().getAllArtifacts().stream()
                .parallel()
                .forEachOrdered(item -> item.setFitness(new DDDFitness()));
    }

    private void refactorDependencies() {
        refactorData.getNewStructure().getClasses().stream()
                .parallel()
                .forEachOrdered(artifact -> {
                    for (String dependency : new ArrayList<>(artifact.getDependencies())) {
                        String[] split = dependency.split("[.]");
                        String name = split[split.length-1];
                        String newPath = findNewPath(name);
                        if (newPath != null) {
                            artifact.getDependencies().remove(dependency);
                            artifact.addDependencies(newPath);
                        }
                    }
                });
    }

    private String findNewPath(String name) {
        for (Artifact artifact : refactorData.getNewStructure().getAllArtifacts()) {
            if (artifact.getName().equals(name)) {
                return artifact.getPath();
            }
        }
        return null;
    }
}
