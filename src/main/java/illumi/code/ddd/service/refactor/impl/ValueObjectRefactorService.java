package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ValueObjectRefactorService extends DefaultRefactorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValueObjectRefactorService.class);

    public ValueObjectRefactorService(DDDRefactorData refactorData) {
        super(refactorData);
    }

    void refactor(Package model) {
        for (Artifact artifact : new ArrayList<>(model.getContains())) {
            if (artifact instanceof Class
                    && artifact.isTypeOf(DDDType.VALUE_OBJECT)) {
                refactorValueObject(model, (Class) artifact);
            }
        }
    }

    private void refactorValueObject(Package model, Class artifact) {

        if (needsMethod(artifact, "equals")) {
            artifact.addMethod(createEquals());
        }

        if (needsMethod(artifact, "hashCode")) {
            artifact.addMethod(createHashCode());
        }

        for (Field field : new ArrayList<>(artifact.getFields())) {
            if (Field.isId(field) && !artifact.getName().toLowerCase().endsWith("id")) {
                deleteMethodsOfField(artifact, field);
                artifact.getFields().remove(field);
            } else {
                if (needsGetter(artifact, field)) {
                    artifact.addMethod(createGetter(field));
                }

                if (needsSetter(artifact, field)) {
                    artifact.addMethod(createSetter(field));
                }
            }
        }
    }

    private void deleteMethodsOfField(Class artifact, Field field) {
        for (Method method : new ArrayList<>(artifact.getMethods())) {
            if (method.getName().equalsIgnoreCase("get" + field.getName())
                    || method.getName().equalsIgnoreCase(field.getName())
                    || method.getName().equalsIgnoreCase("set" + field.getName())) {
                artifact.getMethods().remove(method);
            }
        }
    }

    private boolean needsGetter(Class artifact, Field field) {
        for (Method method : artifact.getMethods()) {
            if (method.getName().equalsIgnoreCase("get" + field.getName())) {
                refactorGetter(field, method);
                return false;
            } else if (method.getName().equalsIgnoreCase(field.getName())) {
                return false;
            }
        }
        return true;
    }

    private void refactorGetter(Field field, Method method) {
        method.setSignature(method.getSignature().replace(method.getName(), field.getName()));
        method.setName(field.getName());
    }

    private Method createGetter(Field field) {
        String name = field.getName();
        String signature = String.format("%s %s()", field.getType(), name);
        LOGGER.info("Create {}", signature);
        return new Method(PUBLIC, name, signature);
    }

    private boolean needsSetter(Class artifact, Field field) {
        for (Method method : artifact.getMethods()) {
            if (method.getName().equalsIgnoreCase("set" + field.getName())) {
                refactorSetter(field, method);
                return false;
            }
        }
        return true;
    }

    private void refactorSetter(Field field, Method method) {
        if (method.getVisibility().equalsIgnoreCase(PUBLIC)) {
            method.setVisibility(PRIVATE);
        }
    }

    private Method createSetter(Field field) {
        String name = String.format("set%s", modifyFirstChar(field.getName()));
        String signature = String.format("void %s(%s)", name, field.getType());
        LOGGER.info("Create {}", signature);
        return new Method(PRIVATE, name, signature);
    }
}
