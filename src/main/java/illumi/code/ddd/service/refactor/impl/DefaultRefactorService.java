package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Method;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.service.refactor.ArtifactRefactorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultRefactorService implements ArtifactRefactorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRefactorService.class);

    static final String PRIVATE = "private";
    static final String PUBLIC = "public";

    private DDDRefactorData refactorData;

    DefaultRefactorService(DDDRefactorData refactorData) {
        this.refactorData = refactorData;
    }

    public DDDRefactorData getRefactorData() {
        return refactorData;
    }

    @Override
    public void refactor() {
        refactorData.getDomainModule().getContains().stream()
                .parallel()
                .forEach(item -> {
                    if (item instanceof Package) {
                        if (!item.getName().equalsIgnoreCase("model")) {
                            Package model = getModelOfDomain(item);
                            refactor(model);
                        } else {
                            refactor((Package) item);
                        }
                    }
                });
    }

    private Package getModelOfDomain(Artifact item) {
        return (Package) ((Package) item).getContains().get(0);
    }

    abstract void refactor(Package model);

    String toSingular(String name) {
        if (name.endsWith("ies")) {
            return (name + "&").replace("ies&", "y");
        }
        if (name.endsWith("s")) {
            return name.substring(0, name.length()-1);
        }
        return name;
    }

    String modifyFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    boolean needsMethod(Class artifact, String name) {
        for (Method method : artifact.getMethods()) {
            if (method.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }
        return true;
    }

    Method createEquals() {
        LOGGER.info("Create java.lang.Boolean equals(Object)");
        return new Method(PUBLIC, "equals", "java.lang.Boolean equals(Object)");
    }

    Method createHashCode() {
        LOGGER.info("Create java.lang.Integer hashCode()");
        return new Method(PUBLIC, "hashCode", "java.lang.Integer hashCode()");
    }
}
