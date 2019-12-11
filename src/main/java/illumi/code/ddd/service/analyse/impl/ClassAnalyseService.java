package illumi.code.ddd.service.analyse.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Method;
import illumi.code.ddd.model.DDDStructure;
import org.apache.commons.lang3.StringUtils;

public class ClassAnalyseService {
    private static final String REPOSITORY = "Repository";

    private Class artifact;
    private DDDStructure structure;

    public ClassAnalyseService(Class artifact, DDDStructure structure) {
        this.artifact = artifact;
        this.structure = structure;
    }

    public ClassAnalyseService(Class artifact) {
        this.artifact = artifact;
    }

    public void setType() {
        if (isInfrastructure()) {
            this.artifact.setType(DDDType.INFRASTRUCTURE);
        } else if (this.artifact.getType() == null) {
            if (isValueObject(structure)) {
                this.artifact.setType(DDDType.VALUE_OBJECT);
            } else if (isEntity(structure)) {
                this.artifact.setType(DDDType.ENTITY);
            } else if (isApplicationService()) {
                this.artifact.setType(DDDType.APPLICATION_SERVICE);
            } else if (isService(structure)) {
                this.artifact.setType(DDDType.SERVICE);
            } else {
                this.artifact.setType(DDDType.INFRASTRUCTURE);
            }
        }
    }

    private boolean isInfrastructure() {
        return this.artifact.getName().toUpperCase().contains("JPA") || this.artifact.getName().toUpperCase().contains("CRUD");
    }

    private boolean isValueObject(DDDStructure structure) {
        int ctr = 0;
        for (Field field : this.artifact.getFields()) {
            if (isConstant(field)) {
                return false;
            } else if (Field.isId(field)
                    && !(this.artifact.getName().toLowerCase().endsWith("id"))) {
                return false;
            } else if (field.getType().startsWith("java.")
                    || field.getType().contains(structure.getPath())) {
                ctr++;
            }
        }
        return !this.artifact.getFields().isEmpty()
                && ctr == this.artifact.getFields().size()
                && containsModelMethods();
    }

    private boolean isEntity(DDDStructure structure) {

        for (Field field : this.artifact.getFields()) {
            if (isConstant(field)) {
                return false;
            } else if (Field.isId(field)) {
                return true;
            }
        }
        return !this.artifact.getFields().isEmpty()
                && !containsEntityName(structure)
                && containsModelMethods();
    }

    private boolean isService(DDDStructure structure) {
        for (Field field : this.artifact.getFields()) {
            if (field.getType().contains(REPOSITORY)) {
                return true;
            }
        }
        return containsEntityName(structure);
    }

    private boolean isApplicationService() {
        for (Method method: this.artifact.getMethods()) {
            if (method.getName().equalsIgnoreCase("main")) {
                return true;
            }
        }
        return false;
    }

    private boolean isConstant(Field field) {
        return StringUtils.isAllUpperCase(field.getName());
    }


    private boolean containsModelMethods() {
        long constructors = artifact.getMethods().stream()
                .filter(method -> method.getName().equalsIgnoreCase("<init>")).count();
        return artifact.getMethods().isEmpty()
                || constructors == artifact.getMethods().size()
                || containsEqualsOrHashCode()
                || containsGetterOrSetter();
    }

    private boolean containsEqualsOrHashCode() {

        for (Method method : this.artifact.getMethods()) {
            if (method.getName().equalsIgnoreCase("equals")
                    ^ method.getName().equalsIgnoreCase("hashCode")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsGetterOrSetter() {
        for (Method method : this.artifact.getMethods()) {
            if (method.getName().startsWith("get")
                    ^ method.getName().startsWith("set")) {
                return true;
            }
        }
        return containsUnconventionalGetter();
    }

    private boolean containsUnconventionalGetter() {
        for (Method method : this.artifact.getMethods()) {
            for (Field field : this.artifact.getFields()) {
                if (method.getSignature().startsWith(field.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsEntityName(DDDStructure structure) {
        for (Class aClass : structure.getClasses()) {
            if (this.artifact != aClass
                    && this.artifact.getName().contains(aClass.getName())
                    && !this.artifact.getName().equals(aClass.getName() + "s")) {
                return true;
            }
        }
        return false;
    }

    public void setDomainEvent() {
        switch(this.artifact.getType()) {
            case ENTITY:
            case AGGREGATE_ROOT:
            case VALUE_OBJECT:
                if (isDomainEvent()) {
                    this.artifact.setType(DDDType.DOMAIN_EVENT);
                }
                break;
            default:
                break;
        }
    }

    public void setInfrastructure() {
        switch(this.artifact.getType()) {
            case ENTITY:
            case AGGREGATE_ROOT:
            case VALUE_OBJECT:
                if (onlyUsedByInfrastructure()) {
                    this.artifact.setType(DDDType.INFRASTRUCTURE);
                }
                break;
            default:
                break;
        }
    }

    private boolean onlyUsedByInfrastructure() {
        boolean usedByInfra = false;
        for (Class item : structure.getClasses()) {
            if (usedByClass(item)){
                if (!item.isTypeOf(DDDType.INFRASTRUCTURE)) {
                    return false;
                } else {
                    usedByInfra = true;
                }
            }
        }
        return usedByInfra;
    }

    private boolean usedByClass(Class item) {
        return item.getDependencies().contains(artifact.getPath())
                || (item.getSuperClass() == artifact);
    }

    private boolean isDomainEvent() {
        boolean containsTimestamp = false;
        boolean containsIdentity = false;

        for (Field field : this.artifact.getFields()) {
            if (field.getName().contains("time")
                    || field.getName().contains("date")
                    || field.getType().contains("java.time.")) {
                containsTimestamp = true;

            } else if (!field.getName().equalsIgnoreCase("id")
                    && field.getName().toUpperCase().endsWith("ID")) {
                containsIdentity = true;
            }
        }

        return containsTimestamp && containsIdentity;
    }
}
