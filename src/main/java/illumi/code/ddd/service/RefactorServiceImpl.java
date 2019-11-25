package illumi.code.ddd.service;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;

public class RefactorServiceImpl implements RefactorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefactorServiceImpl.class);

    private StructureService structureService;
    private StructureService improveStructureService;

    private Package domain;
    private Package application;
    private Package infrastructure;

    private Package model;

    private ArrayList<Class> roots;

    public @Inject
    RefactorServiceImpl() {
        // @Inject is needed
    }

    @Override
    public void setStructureService(StructureService structureService) {
        this.structureService = structureService;
        this.improveStructureService = new StructureService();
        this.improveStructureService.setPath(structureService.getPath());
        this.roots = new ArrayList<>();
    }

    @Override
    public StructureService refactor() {
        initModules();

        sortClasses();

        refactorPaths(improveStructureService.getPath(), improveStructureService.getStructure());
        return improveStructureService;
    }

    private void initModules() {
        ArrayList<Artifact> structure = new ArrayList<>();

        this.application = new Package("application",
                String.format("%sapplication", improveStructureService.getPath()));
        improveStructureService.addPackage(application);
        structure.add(application);

        this.infrastructure = new Package("infrastructure",
                String.format("%sinfrastructure", improveStructureService.getPath()));
        improveStructureService.addPackage(infrastructure);
        structure.add(infrastructure);

        this.domain = new Package("domain",
                String.format("%sdomain", improveStructureService.getPath()));
        improveStructureService.addPackage(domain);
        structure.add(domain);

        initDomains();

        this.model = new Package("model",
                String.format("%s.model", this.domain.getPath()));
        improveStructureService.addPackage(model);
        this.domain.addContains(model);

        improveStructureService.setStructure(structure);
    }

    private void initDomains() {
        for (Class artifact: structureService.getClasses()) {
            if (artifact.isTypeOf(DDDType.AGGREGATE_ROOT)) {
                roots.add(artifact);

                String domain = artifact.getName().toLowerCase();
                improveStructureService.addDomain(domain);

                Package domainModule = addDomainModules(this.domain, domain);

                Package model = new Package("model", String.format("%s.model", domainModule.getPath()));
                domainModule.addContains(model);
                improveStructureService.addPackage(model);

                Package impl = new Package("impl", String.format("%s.impl", model.getPath()));
                model.addContains(impl);
                improveStructureService.addPackage(impl);

                model.addContains(artifact);
                improveStructureService.addClass(artifact);

                addDomainModules(this.application, domain);
            }
        }
    }

    private Package addDomainModules(Package module, String domain) {
        Package newModule = new Package(domain, String.format("%s.%s", module.getPath(), domain));
        module.addContains(newModule);
        improveStructureService.addPackage(newModule);

        return newModule;
    }

    private void sortClasses() {
        for (Class artifact: structureService.getClasses()) {
            switch (artifact.getType()) {
                case APPLICATION_SERVICE:
                    this.application.addContains(artifact);
                    break;
                case INFRASTRUCTURE:
                case CONTROLLER:
                    this.infrastructure.addContains(artifact);
                    break;
                case SERVICE:
                    addArtifact(artifact, "application.%s");
                    break;
                case FACTORY:
                case REPOSITORY:
                    addArtifact(artifact, "domain.%s.model.impl");
                    break;
                default:
                    addArtifact(artifact, "domain.%s.model");
                    break;
            }
            improveStructureService.addClass(artifact);
        }
    }

    private void addArtifact(Class artifact, String s) {
        Package module = getModule(String.format(s, getDomainOf(artifact)));

        if (module != null) {
            module.addContains(artifact);
        } else {
            module = getModule(String.format(s, dependsOn(artifact)));
            if (module != null) {
                module.addContains(artifact);
            } else {
                this.model.addContains(artifact);
            }
        }
    }

    private Package getModule(String path) {
        for (Package module : improveStructureService.getPackages()) {
            if (module.getPath().endsWith(path)) {
                return module;
            }
        }
        return null;
    }

    private String getDomainOf(Class artifact) {
        for (Class root: roots) {
            if (isRootOf(root, artifact)) {
                return root.getName().toLowerCase();
            }
        }
        return null;
    }

    private boolean isRootOf(Class root, Class artifact) {
        return artifact.getName().toLowerCase().contains(root.getName().toLowerCase())
                || artifact.getName().toLowerCase().contains(root.getDomain().toLowerCase())
                || artifact.getPath().contains("." + root.getDomain() + ".")
                || (artifact.getDomain() != null
                    && artifact.getDomain().equalsIgnoreCase(root.getDomain()));
    }

    private String dependsOn(Class artifact) {
        for (String dependency : artifact.getDependencies()) {
            for (Class aClass : structureService.getClasses()) {
                if (aClass != artifact
                    && artifact.getPath().contains(dependency)) {
                    String domain = getDomainOf(aClass);
                    if (domain != null) {
                        return domain;
                    }
                }
            }
        }

        for (Field field : artifact.getFields()) {
            for (Class aClass : structureService.getClasses()) {
                if (aClass != artifact
                    && field.getName().toLowerCase().contains(aClass.getName().toLowerCase())) {
                    String domain = getDomainOf(aClass);
                    if (domain != null) {
                        return domain;
                    }
                }
            }
        }

        return null;
    }

    private void refactorPaths(String path, ArrayList<Artifact> structure) {
        for (Artifact artifact : structure) {
            artifact.setPath(path + artifact.getName());
            if (artifact instanceof Package) {
                refactorPaths(artifact.getPath() + ".", (ArrayList<Artifact>) ((Package) artifact).getContains());
            }
        }
    }
}
