package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class EntityRefactorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityRefactorService.class);

    private DDDRefactorData refactorData;

    public EntityRefactorService(DDDRefactorData refactorData) {
        this.refactorData = refactorData;
    }

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

    private void refactor(Package model) {
        for (Artifact artifact : new ArrayList<>(model.getContains())) {
            switch(artifact.getType()) {
                case ENTITY:
                case AGGREGATE_ROOT:
                    refactorEntity(model, (Class) artifact);
                    break;
            }
        }
    }

    private void refactorEntity(Package model, Class artifact) {
        refactorFields(model, artifact);

        refactorMethods(artifact);
    }

    private void refactorFields(Package model, Class artifact) {
        for (Field field : artifact.getFields()) {
            Class type = getTypeOfField(model, field);
            if (type != null) {
                field.setType(type.getPath());
            } else {
                Class newValueObject = createNewValueObject(model.getPath(), artifact.getName(), field);
                refactorData.getNewStructure().addClass(newValueObject);
                model.addContains(newValueObject);
                field.setType(newValueObject.getPath());
            }
        }
    }

    private Class getTypeOfField(Package model, Field field) {
        for (Artifact item : model.getContains()) {
            if (item instanceof Class) {
                switch (item.getType()) {
                    case ENTITY:
                    case AGGREGATE_ROOT:
                    case VALUE_OBJECT:
                    case DOMAIN_EVENT:
                        if (field.getType().endsWith(item.getName())
                                || field.getName().toLowerCase().contains(item.getName().toLowerCase())) {
                            return (Class) item;
                        }
                        break;
                }
            }
        }
        return null;
    }

    private Class createNewValueObject(String modelPath, String entityName, Field field) {
        String name = generateName(entityName, field);
        String path = String.format("%s.%s", modelPath, name);
        Class newValueObject = new Class(name, path);
        newValueObject.setType(DDDType.VALUE_OBJECT);
        newValueObject.addField(new Field("private", field.getName(), field.getType()));
        newValueObject.addMethod(createEquals());
        newValueObject.addMethod(createHashCode());
        newValueObject.addMethod(new Method("public", field.getName(), field.getType()));
        newValueObject.addMethod(new Method("private", "set" + modifyFirstChar(field.getName()), path));

        LOGGER.info("Created " + newValueObject.getPath());
        return newValueObject;
    }

    private String generateName(String entityName, Field field) {
        return entityName + modifyFirstChar(field.getName());
    }

    private String modifyFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void refactorMethods(Class artifact) {
        if (needsMethod(artifact, "equals")) {
            artifact.addMethod(createEquals());
        }

        if (needsMethod(artifact, "hashCode")) {
            artifact.addMethod(createHashCode());
        }

        for (Field field : artifact.getFields()) {
            if (needsMethod(artifact, "get" + field.getName())) {
                artifact.addMethod(createGetter(field));
            }

            if (!field.getName().toLowerCase().endsWith("id")
                    && needsMethod(artifact, "set" + field.getName())) {
                artifact.addMethod(createSetter(field));
            }
        }
    }

    private boolean needsMethod(Class artifact, String name) {
        for (Method method : artifact.getMethods()) {
            if (method.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }
        return true;
    }

    private Method createGetter(Field field) {
        String name = String.format("get%s", modifyFirstChar(field.getName()));
        String signature = String.format("%s %s()", field.getType(), name);
        return new Method("public", name, signature);
    }

    private Method createSetter(Field field) {
        String name = String.format("set%s", modifyFirstChar(field.getName()));
        String signature = String.format("void %s(%s)", name, field.getType());
        return new Method("public", name, signature);
    }

    private Method createEquals() {
        return new Method("public", "equals", "java.lang.Boolean equals(Object)");
    }

    private Method createHashCode() {
        return new Method("public", "hashCode", "java.lang.Integer hashCode()");
    }
}