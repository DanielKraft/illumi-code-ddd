package illumi.code.ddd.service.refactor.impl;


import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Enum;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.DDDRefactorData;

import java.util.ArrayList;

class AssignService {
    private static final String DOMAIN_PATH = "domain.%s.model";
    private static final String IMPL_PATH = "domain.%s.model.impl";
    private static final String APPLICATION_PATH = "application.%s";

    private DDDRefactorData refactorData;

    AssignService(DDDRefactorData refactorData) {
        this.refactorData = refactorData;
    }

    void assign() {
        assignClasses();
        assignInterfaces();
        assignEnums();
        assignAnnotations();

        cleanDomain();

        refactorPaths(refactorData.getNewStructure().getPath(),
                (ArrayList<Artifact>) refactorData.getNewStructure().getStructure());
        refactorDomains();
    }

    private void assignClasses() {
        for (Class artifact: refactorData.getOldStructure().getClasses()) {
            switch (artifact.getType()) {
                case APPLICATION_SERVICE:
                    refactorData.getApplicationModule().addContains(artifact);
                    break;
                case INFRASTRUCTURE:
                case CONTROLLER:
                    refactorData.getInfrastructureModule().addContains(artifact);
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
            refactorData.getNewStructure().addClass(artifact);
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
                refactorData.getModelModule().addContains(artifact);
            }
        }
    }

    private Package getModule(String path) {
        for (Package module : refactorData.getNewStructure().getPackages()) {
            if (module.getPath().endsWith(path)) {
                return module;
            }
        }
        return null;
    }

    private String getDomainOf(Artifact artifact) {
        for (Class root: refactorData.getRoots()) {
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
            for (Class aClass : refactorData.getOldStructure().getClasses()) {
                if (aClass != artifact
                        && aClass.getName().equalsIgnoreCase(dependency)) {
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
            for (Class aClass : refactorData.getOldStructure().getClasses()) {
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
        for (Interface artifact: refactorData.getOldStructure().getInterfaces()) {
            switch (artifact.getType()) {
                case SERVICE:
                    addArtifact(APPLICATION_PATH, artifact);
                    break;
                case INFRASTRUCTURE:
                case CONTROLLER:
                    refactorData.getInfrastructureModule().addContains(artifact);
                    break;
                default:
                    addArtifact(DOMAIN_PATH, artifact);
                    break;
            }
            refactorData.getNewStructure().addInterface(artifact);
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
                refactorData.getModelModule().addContains(artifact);
            }
        }
    }

    private Package dependsOn(Interface artifact) {
        for (Package module : refactorData.getNewStructure().getPackages()) {
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
        for (Enum artifact: refactorData.getOldStructure().getEnums()) {
            addArtifact(artifact);
            refactorData.getNewStructure().addEnum(artifact);
        }
    }

    private void addArtifact(Enum artifact) {
        Package module = getModule(String.format(DOMAIN_PATH, getDomainOf(artifact)));

        if (module != null) {
            module.addContains(artifact);
        } else {
            refactorData.getModelModule().addContains(artifact);
        }
    }

    private void assignAnnotations() {
        for (Annotation artifact: refactorData.getOldStructure().getAnnotations()) {
            refactorData.getInfrastructureModule().addContains(artifact);
            refactorData.getNewStructure().addAnnotation(artifact);
        }
    }

    private void refactorPaths(String path, ArrayList<Artifact> structure) {
        for (Artifact artifact : structure) {
            String oldPath = artifact.getPath();
            String newPath = path + artifact.getName();
            artifact.setPath(newPath);
            if (artifact instanceof Package) {
                refactorPaths(artifact.getPath() + ".", (ArrayList<Artifact>) ((Package) artifact).getContains());
            } else {
                refactorDependencies(oldPath, newPath);
            }
        }
    }

    private void refactorDependencies(String oldPath, String newPath) {
        for (Artifact artifact : refactorData.getNewStructure().getAllArtifacts()) {
            if (artifact instanceof File) {
                refactorFieldDependencies(oldPath, newPath, (File) artifact);
                refactorMethodDependencies(oldPath, newPath, (File) artifact);
            }
        }
    }

    private void refactorFieldDependencies(String oldPath, String newPath, File artifact) {
        artifact.getFields().stream()
                .parallel()
                .forEachOrdered(field -> field.setType(field.getType().replace(oldPath, newPath)));
    }

    private void refactorMethodDependencies(String oldPath, String newPath, File artifact) {
        artifact.getMethods().stream()
                .parallel()
                .forEachOrdered(method -> method.setSignature(method.getSignature().replace(oldPath, newPath)));
    }

    private void refactorDomains() {
        for (String domain : refactorData.getNewStructure().getDomains()) {
            setDomain(domain, DOMAIN_PATH);

            setDomain(domain, IMPL_PATH);

            setDomain(domain, APPLICATION_PATH);
        }
    }

    private void setDomain(String domain, String applicationPath) {
        Package module = getModule(String.format(applicationPath, domain));
        if (module != null) {
            module.getContains().stream()
                    .parallel()
                    .forEachOrdered(item -> item.setDomain(domain));
        }
    }

    private void cleanDomain() {
        refactorData.getNewStructure().getAllArtifacts().stream()
                .parallel()
                .forEachOrdered(item -> item.setDomain(null));
    }
}
