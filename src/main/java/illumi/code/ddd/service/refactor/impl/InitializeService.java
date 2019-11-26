package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;

class InitializeService {

    private RefactorServiceImpl refactorService;

    InitializeService(RefactorServiceImpl refactorService) {
        this.refactorService = refactorService;
    }

    void initModules() {
        ArrayList<Artifact> structure = new ArrayList<>();

        refactorService.setApplicationModule(new Package("application",
                String.format("%sapplication", refactorService.getNewStructure().getPath())));
        refactorService.getNewStructure().addPackage(refactorService.getApplicationModule());
        structure.add(refactorService.getApplicationModule());

        refactorService.setInfrastructureModule(new Package("infrastructure",
                String.format("%sinfrastructure", refactorService.getNewStructure().getPath())));
        refactorService.getNewStructure().addPackage(refactorService.getInfrastructureModule());
        structure.add(refactorService.getInfrastructureModule());

        refactorService.setDomainModule(new Package("domain",
                String.format("%sdomain", refactorService.getNewStructure().getPath())));
        refactorService.getNewStructure().addPackage(refactorService.getDomainModule());
        structure.add(refactorService.getDomainModule());

        initDomains();

        refactorService.setModelModule(new Package("model",
                String.format("%s.model", refactorService.getDomainModule().getPath())));
        refactorService.getNewStructure().addPackage(refactorService.getModelModule());
        refactorService.getDomainModule().addContains(refactorService.getModelModule());

        refactorService.getNewStructure().setStructure(structure);
    }

    private void initDomains() {
        for (Class artifact: refactorService.getOldStructure().getClasses()) {
            if (artifact.isTypeOf(DDDType.AGGREGATE_ROOT)) {
                refactorService.addRoots(artifact);

                String domain = artifact.getName().toLowerCase();
                refactorService.getNewStructure().addDomain(domain);

                Package module = addDomainModules(refactorService.getDomainModule(), domain);

                Package model = new Package("model", String.format("%s.model", module.getPath()));
                module.addContains(model);
                refactorService.getNewStructure().addPackage(model);

                Package impl = new Package("impl", String.format("%s.impl", model.getPath()));
                model.addContains(impl);
                refactorService.getNewStructure().addPackage(impl);

                model.addContains(artifact);
                refactorService.getNewStructure().addClass(artifact);

                addDomainModules(refactorService.getApplicationModule(), domain);
            }
        }
    }

    private Package addDomainModules(Package module, String domain) {
        Package newModule = new Package(domain, String.format("%s.%s", module.getPath(), domain));
        module.addContains(newModule);
        refactorService.getNewStructure().addPackage(newModule);

        return newModule;
    }
}
