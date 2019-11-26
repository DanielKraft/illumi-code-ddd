package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.DDDRefactorData;

import java.util.ArrayList;

class InitializeService {

    private DDDRefactorData refactorData;

    InitializeService(DDDRefactorData refactorData) {
        this.refactorData = refactorData;
    }

    void initModules() {
        ArrayList<Artifact> structure = new ArrayList<>();

        refactorData.setApplicationModule(new Package("application",
                String.format("%sapplication", refactorData.getNewStructure().getPath())));
        refactorData.getNewStructure().addPackage(refactorData.getApplicationModule());
        structure.add(refactorData.getApplicationModule());

        refactorData.setInfrastructureModule(new Package("infrastructure",
                String.format("%sinfrastructure", refactorData.getNewStructure().getPath())));
        refactorData.getNewStructure().addPackage(refactorData.getInfrastructureModule());
        structure.add(refactorData.getInfrastructureModule());

        refactorData.setDomainModule(new Package("domain",
                String.format("%sdomain", refactorData.getNewStructure().getPath())));
        refactorData.getNewStructure().addPackage(refactorData.getDomainModule());
        structure.add(refactorData.getDomainModule());

        initDomains();

        refactorData.setModelModule(new Package("model",
                String.format("%s.model", refactorData.getDomainModule().getPath())));
        refactorData.getNewStructure().addPackage(refactorData.getModelModule());
        refactorData.getDomainModule().addContains(refactorData.getModelModule());

        refactorData.getNewStructure().setStructure(structure);
    }

    private void initDomains() {
        for (Class artifact: refactorData.getOldStructure().getClasses()) {
            if (artifact.isTypeOf(DDDType.AGGREGATE_ROOT)) {
                refactorData.addRoots(artifact);

                String domain = artifact.getName().toLowerCase();
                refactorData.getNewStructure().addDomain(domain);

                Package module = addDomainModules(refactorData.getDomainModule(), domain);

                Package model = new Package("model", String.format("%s.model", module.getPath()));
                module.addContains(model);
                refactorData.getNewStructure().addPackage(model);

                Package impl = new Package("impl", String.format("%s.impl", model.getPath()));
                model.addContains(impl);
                refactorData.getNewStructure().addPackage(impl);

                model.addContains(artifact);
                refactorData.getNewStructure().addClass(artifact);

                addDomainModules(refactorData.getApplicationModule(), domain);
            }
        }
    }

    private Package addDomainModules(Package module, String domain) {
        Package newModule = new Package(domain, String.format("%s.%s", module.getPath(), domain));
        module.addContains(newModule);
        refactorData.getNewStructure().addPackage(newModule);

        return newModule;
    }
}
