package illumi.code.ddd.service;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Enum;
import illumi.code.ddd.model.artifacts.Package;

import javax.inject.Inject;
import java.util.ArrayList;

public class RefactorServiceImpl implements RefactorService {
    private static final String DOMAIN_PATH = "domain.%s.model";
    private static final String IMPL_PATH = "domain.%s.model.impl";
    private static final String APPLICATION_PATH = "application.%s";
    private StructureService oldStructure;
    private StructureService newStructure;

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
    public void setOldStructure(StructureService oldStructure) {
        this.oldStructure = oldStructure;
        this.newStructure = new StructureService();
        this.newStructure.setPath(oldStructure.getPath());
        this.roots = new ArrayList<>();
    }

    @Override
    public StructureService refactor() {
        initModules();

        assignClasses();
        assignInterfaces();
        assignEnums();
        assignAnnotations();

        refactorPaths(newStructure.getPath(), newStructure.getStructure());
        return newStructure;
    }

    private void initModules() {
        ArrayList<Artifact> structure = new ArrayList<>();

        this.application = new Package("application",
                String.format("%sapplication", newStructure.getPath()));
        newStructure.addPackage(application);
        structure.add(application);

        this.infrastructure = new Package("infrastructure",
                String.format("%sinfrastructure", newStructure.getPath()));
        newStructure.addPackage(infrastructure);
        structure.add(infrastructure);

        this.domain = new Package("domain",
                String.format("%sdomain", newStructure.getPath()));
        newStructure.addPackage(domain);
        structure.add(domain);

        initDomains();

        this.model = new Package("model",
                String.format("%s.model", this.domain.getPath()));
        newStructure.addPackage(model);
        this.domain.addContains(model);

        newStructure.setStructure(structure);
    }

    private void initDomains() {
        for (Class artifact: oldStructure.getClasses()) {
            if (artifact.isTypeOf(DDDType.AGGREGATE_ROOT)) {
                roots.add(artifact);

                String domain = artifact.getName().toLowerCase();
                newStructure.addDomain(domain);

                Package domainModule = addDomainModules(this.domain, domain);

                Package model = new Package("model", String.format("%s.model", domainModule.getPath()));
                domainModule.addContains(model);
                newStructure.addPackage(model);

                Package impl = new Package("impl", String.format("%s.impl", model.getPath()));
                model.addContains(impl);
                newStructure.addPackage(impl);

                model.addContains(artifact);
                newStructure.addClass(artifact);

                addDomainModules(this.application, domain);
            }
        }
    }

    private Package addDomainModules(Package module, String domain) {
        Package newModule = new Package(domain, String.format("%s.%s", module.getPath(), domain));
        module.addContains(newModule);
        newStructure.addPackage(newModule);

        return newModule;
    }

    private void assignClasses() {
        for (Class artifact: oldStructure.getClasses()) {
            switch (artifact.getType()) {
                case APPLICATION_SERVICE:
                    this.application.addContains(artifact);
                    break;
                case INFRASTRUCTURE:
                case CONTROLLER:
                    this.infrastructure.addContains(artifact);
                    break;
                case SERVICE:
                    addArtifact(artifact, APPLICATION_PATH);
                    break;
                case FACTORY:
                case REPOSITORY:
                    addArtifact(artifact, IMPL_PATH);
                    break;
                default:
                    addArtifact(artifact, DOMAIN_PATH);
                    break;
            }
            newStructure.addClass(artifact);
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
        for (Package module : newStructure.getPackages()) {
            if (module.getPath().endsWith(path)) {
                return module;
            }
        }
        return null;
    }

    private String getDomainOf(Artifact artifact) {
        for (Class root: roots) {
            if (isRootOf(root, artifact)) {
                return root.getName().toLowerCase();
            }
        }
        return null;
    }

    private boolean isRootOf(Class root, Artifact artifact) {
        return artifact.getName().toLowerCase().contains(root.getName().toLowerCase())
                || artifact.getName().toLowerCase().contains(root.getDomain().toLowerCase())
                || artifact.getPath().contains("." + root.getDomain() + ".")
                || (artifact.getDomain() != null
                && artifact.getDomain().equalsIgnoreCase(root.getDomain()));
    }

    private String dependsOn(Class artifact) {
        for (String dependency : artifact.getDependencies()) {
            for (Class aClass : oldStructure.getClasses()) {
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
            for (Class aClass : oldStructure.getClasses()) {
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

    private void assignInterfaces() {
        for (Interface artifact: oldStructure.getInterfaces()) {
            addArtifact(artifact);
            newStructure.addInterface(artifact);
        }
    }

    private void addArtifact(Interface artifact) {
        Package module = getModule(String.format(DOMAIN_PATH, getDomainOf(artifact)));

        if (module != null) {
            module.addContains(artifact);
        } else {
            module = dependsOn(artifact);
            if (module != null) {
                module.addContains(artifact);
            } else {
                this.model.addContains(artifact);
            }
        }
    }

    private Package dependsOn(Interface artifact) {
        for (Package module : newStructure.getPackages()) {
            for (Artifact item : module.getContains()) {
                if (!(item instanceof Package)
                    && artifact.getName().toLowerCase().contains(item.getName().toLowerCase())) {
                    return module;
                }
            }
        }
        return null;
    }

    private void assignEnums() {
        for (Enum artifact: oldStructure.getEnums()) {
            addArtifact(artifact);
            newStructure.addEnum(artifact);
        }
    }

    private void addArtifact(Enum artifact) {
        Package module = getModule(String.format(DOMAIN_PATH, getDomainOf(artifact)));

        if (module != null) {
            module.addContains(artifact);
        } else {
            this.model.addContains(artifact);
        }
    }

    private void assignAnnotations() {
        for (Annotation artifact: oldStructure.getAnnotations()) {
            infrastructure.addContains(artifact);
            newStructure.addAnnotation(artifact);
        }
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
