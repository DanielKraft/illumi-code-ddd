package illumi.code.ddd.service.analyse;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Method;
import illumi.code.ddd.service.StructureService;
import org.apache.commons.lang3.StringUtils;

public class ClassAnalyseService {
    private static final String REPOSITORY = "Repository";

    private Class artifact;
    private StructureService structureService;

    public ClassAnalyseService(Class artifact, StructureService structureService) {
        this.artifact = artifact;
        this.structureService = structureService;
    }

    public ClassAnalyseService(Class artifact) {
        this.artifact = artifact;
    }

    public void setType() {
        if (isInfrastructur()) {
            this.artifact.setType(DDDType.INFRASTRUCTUR);
        } else if (this.artifact.getType() == null) {
            if (isValueObject(structureService)) {
                this.artifact.setType(DDDType.VALUE_OBJECT);
            } else if (isEntity(structureService)) {
                this.artifact.setType(DDDType.ENTITY);
            } else if (isService(structureService)) {
                this.artifact.setType(DDDType.SERVICE);
            } else {
                this.artifact.setType(DDDType.INFRASTRUCTUR);
            }
        }
    }

    private boolean isInfrastructur() {
        return this.artifact.getName().toUpperCase().contains("JPA") || this.artifact.getName().toUpperCase().contains("CRUD");
    }

    private boolean isValueObject(StructureService structureService) {
        int ctr = 0;
        for (Field field : this.artifact.getFields()) {
            if (isConstant(field)) {
                return false;
            } else if (Field.isId(field)
                    && !(this.artifact.getName().toLowerCase().endsWith("id"))) {
                return false;
            } else if (field.getType().startsWith("java.lang.")
                    || field.getType().contains(structureService.getPath())) {
                ctr++;
            }
        }
        return !this.artifact.getFields().isEmpty()
                && ctr == this.artifact.getFields().size()
                && (conatiantsGetterSetter()
                || this.artifact.getMethods().isEmpty());
    }

    private boolean isEntity(StructureService structureService) {

        for (Field field : this.artifact.getFields()) {
            if (isConstant(field)) {
                return false;
            } else if (Field.isId(field)) {
                return true;
            }
        }
        return !this.artifact.getFields().isEmpty()
                && !containsEntityName(structureService)
                && (conatiantsGetterSetter()
                || this.artifact.getMethods().isEmpty());
    }

    private boolean isService(StructureService structureService) {
        for (Field field : this.artifact.getFields()) {
            if (field.getType().contains(REPOSITORY)) {
                return true;
            }
        }
        return containsEntityName(structureService);
    }

    private boolean isConstant(Field field) {
        return StringUtils.isAllUpperCase(field.getName());
    }

    private boolean conatiantsGetterSetter() {
        for (Method method : this.artifact.getMethods()) {
            if (method.getName().startsWith("get") ^ method.getName().startsWith("set")) {
                return true;
            }
        }
        return containtsUnconventionalGetter();
    }

    private boolean containtsUnconventionalGetter() {
        for (Method method : this.artifact.getMethods()) {
            for (Field field : this.artifact.getFields()) {
                if (method.getSignature().startsWith(field.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsEntityName(StructureService structureService) {
        for (Class aClass : structureService.getClasses()) {
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

    private boolean isDomainEvent() {
        boolean containtsTimestamp = false;
        boolean containtsIdentity = false;

        for (Field field : this.artifact.getFields()) {
            if (field.getName().contains("time")
                    || field.getName().contains("date")
                    || field.getType().contains("java.time.")) {
                containtsTimestamp = true;

            } else if (!field.getName().equalsIgnoreCase("id")
                    && field.getName().toUpperCase().endsWith("ID")) {
                containtsIdentity = true;
            }
        }

        return containtsTimestamp && containtsIdentity;
    }
}
