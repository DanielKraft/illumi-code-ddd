package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.Structure;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.service.refactor.RefactorService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class RefactorServiceImpl implements RefactorService {

    private Structure oldStructure;
    private Structure newStructure;

    private Package domainModule;
    private Package applicationModule;
    private Package infrastructureModule;

    private Package modelModule;

    private ArrayList<Class> roots;

    public @Inject
    RefactorServiceImpl() {
        roots = new ArrayList<>();
    }

    void setDomainModule(Package domainModule) {
        this.domainModule = domainModule;
    }

    void setApplicationModule(Package applicationModule) {
        this.applicationModule = applicationModule;
    }

    void setInfrastructureModule(Package infrastructureModule) {
        this.infrastructureModule = infrastructureModule;
    }

    void setModelModule(Package modelModule) {
        this.modelModule = modelModule;
    }

    void addRoots(Class root) {
        this.roots.add(root);
    }

    Structure getOldStructure() {
        return oldStructure;
    }

    Structure getNewStructure() {
        return newStructure;
    }

    Package getDomainModule() {
        return domainModule;
    }

    Package getApplicationModule() {
        return applicationModule;
    }

    Package getInfrastructureModule() {
        return infrastructureModule;
    }

    Package getModelModule() {
        return modelModule;
    }

    ArrayList<Class> getRoots() {
        return roots;
    }

    @Override
    public void setOldStructure(Structure oldStructure) {
        this.oldStructure = oldStructure;
        this.newStructure = new Structure();
        this.newStructure.setPath(oldStructure.getPath());
        this.roots = new ArrayList<>();
    }

    @Override
    public Structure refactor() {

        new InitializeService(this).initModules();
        new AssignService(this).assign();

        deleteEmptyModules(newStructure.getStructure());
        return newStructure;
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
}
