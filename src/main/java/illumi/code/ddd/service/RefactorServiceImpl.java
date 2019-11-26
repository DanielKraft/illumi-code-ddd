package illumi.code.ddd.service;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Enum;
import illumi.code.ddd.model.artifacts.Package;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class RefactorServiceImpl implements RefactorService {
    private static final String DOMAIN_PATH = "domain.%s.model";
    private static final String IMPL_PATH = "domain.%s.model.impl";
    private static final String APPLICATION_PATH = "application.%s";
    private StructureService oldStructure;
    private StructureService newStructure;

    private Package domainModule;
    private Package applicationModule;
    private Package infrastructureModule;

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

        clean();

        refactorPaths(newStructure.getPath(), (ArrayList<Artifact>) newStructure.getStructure());
        refactorDomains();

        deleteEmptyModules(newStructure.getStructure());
        return newStructure;
    }

    private void initModules() {
        ArrayList<Artifact> structure = new ArrayList<>();

        this.applicationModule = new Package("application",
                String.format("%sapplication", newStructure.getPath()));
        newStructure.addPackage(applicationModule);
        structure.add(applicationModule);

        this.infrastructureModule = new Package("infrastructure",
                String.format("%sinfrastructure", newStructure.getPath()));
        newStructure.addPackage(infrastructureModule);
        structure.add(infrastructureModule);

        this.domainModule = new Package("domain",
                String.format("%sdomain", newStructure.getPath()));
        newStructure.addPackage(domainModule);
        structure.add(domainModule);

        initDomains();

        this.model = new Package("model",
                String.format("%s.model", this.domainModule.getPath()));
        newStructure.addPackage(model);
        this.domainModule.addContains(model);

        newStructure.setStructure(structure);
    }

    private void initDomains() {
        for (Class artifact: oldStructure.getClasses()) {
            if (artifact.isTypeOf(DDDType.AGGREGATE_ROOT)) {
                roots.add(artifact);

                String domain = artifact.getName().toLowerCase();
                newStructure.addDomain(domain);

                Package domainModule = addDomainModules(this.domainModule, domain);

                Package model = new Package("model", String.format("%s.model", domainModule.getPath()));
                domainModule.addContains(model);
                newStructure.addPackage(model);

                Package impl = new Package("impl", String.format("%s.impl", model.getPath()));
                model.addContains(impl);
                newStructure.addPackage(impl);

                model.addContains(artifact);
                newStructure.addClass(artifact);

                addDomainModules(this.applicationModule, domain);
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
                    this.applicationModule.addContains(artifact);
                    break;
                case INFRASTRUCTURE:
                case CONTROLLER:
                    this.infrastructureModule.addContains(artifact);
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
        String domain = dependsOnDependency(artifact);
        if (domain != null) {
            return domain;
        }

        domain = dependsOnField(artifact);

        return domain;
    }

    private String dependsOnDependency(Class artifact) {
        for (String dependency : artifact.getDependencies()) {
            for (Class aClass : oldStructure.getClasses()) {
                if (aClass != artifact
                        && artifact.getName().contains(dependency)) {
                    String domain = getDomainOf(aClass);
                    if (domain != null) {
                        return domain;
                    }
                }
            }
        }
        return null;
    }

    private String dependsOnField(Class artifact) {
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
            switch (artifact.getType()) {
                case SERVICE:
                    addArtifact(APPLICATION_PATH, artifact);
                    break;
                case INFRASTRUCTURE:
                case CONTROLLER:
                    this.infrastructureModule.addContains(artifact);
                    break;
                default:
                    addArtifact(DOMAIN_PATH, artifact);
                    break;
            }
            newStructure.addInterface(artifact);
        }
    }

    private void addArtifact(String path, Interface artifact) {
        Package module = getModule(String.format(path, getDomainOf(artifact)));

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
            infrastructureModule.addContains(artifact);
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

    private void refactorDomains() {
        for (String domain : newStructure.getDomains()) {
            setDomain(domain, DOMAIN_PATH);

            setDomain(domain, IMPL_PATH);

            setDomain(domain, APPLICATION_PATH);
        }
    }

    private void setDomain(String domain, String applicationPath) {
        getModule(String.format(applicationPath, domain))
                .getContains().stream()
                .parallel()
                .forEach(item -> item.setDomain(domain));
    }

    private void clean() {
        newStructure.getAllArtifacts().stream()
            .parallel()
            .forEach(item -> {
                item.setFitness(new DDDFitness());
                item.setDomain(null);
            });
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
