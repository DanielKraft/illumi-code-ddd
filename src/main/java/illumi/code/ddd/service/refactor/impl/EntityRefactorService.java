package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.File;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityRefactorService extends DefaultRefactorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(EntityRefactorService.class);

  public EntityRefactorService(DDDRefactorData refactorData) {
    super(refactorData);
  }

  void refactor(Package model) {
    for (Artifact artifact : new ArrayList<>(model.getContains())) {
      if (artifact.isTypeOf(DDDType.ENTITY)
          || artifact.isTypeOf(DDDType.AGGREGATE_ROOT)) {
        refactorEntity(model, (Class) artifact);
      }
    }
  }

  private void refactorEntity(Package model, Class artifact) {
    if (isValueObject(artifact)) {
      artifact.setType(DDDType.VALUE_OBJECT);
      LOGGER.info("Changed {} to Value Object", artifact.getName());
    } else {
      refactorId(artifact);
      refactorFields(model, artifact);
      refactorMethods(artifact);
    }
  }

  private boolean isValueObject(Class artifact) {
    if (artifact.getSuperClass() == null) {
      for (Field field : artifact.getFields()) {
        if (artifact.getName().toLowerCase().contains(field.getName())
            && field.getType().startsWith("java.lang.")) {
          return true;
        }
      }
    }
    return false;
  }

  private void refactorId(Class artifact) {
    if (needsId(artifact)) {
      Field id = new Field(PRIVATE, "id", "java.lang.Long");
      artifact.addField(id);
    }
  }

  private boolean needsId(Class artifact) {
    for (Field field : artifact.getFields()) {
      if (field.getName().toLowerCase().endsWith("id")) {
        return false;
      }
    }
    if (artifact.getSuperClass() != null) {
      return needsId(artifact.getSuperClass());
    }
    return true;
  }


  private void refactorFields(Package model, Class artifact) {
    for (Field field : artifact.getFields()) {
      if (!field.getType().contains("java.util.")) {
        File type = getTypeOfField(model, field);
        if (type != null) {
          field.setType(type.getPath());
        } else {
          Class newValueObject = createValueObjectByField(model, artifact, field);
          refactorField(artifact, field, newValueObject);
        }
      }
    }
  }

  private Class createValueObjectByField(Package model, Class artifact, Field field) {
    Class newValueObject = createNewValueObject(artifact.getDomain(),
        model.getPath(),
        artifact.getName(),
        field);
    getRefactorData().getNewStructure().addClass(newValueObject);
    model.addContains(newValueObject);
    artifact.addDependencies(newValueObject.getName());
    return newValueObject;
  }

  private void refactorField(Class artifact, Field field, Class newValueObject) {
    String oldType = field.getType();
    String newType = newValueObject.getPath();
    field.setType(newType);
    refactorGetterAndSetter(artifact, field, oldType, newType);
  }

  private File getTypeOfField(Package model, Field field) {
    for (Artifact item : model.getContains()) {
      switch (item.getType()) {
        case ENTITY:
        case AGGREGATE_ROOT:
        case VALUE_OBJECT:
        case DOMAIN_EVENT:

          if (field.getType().endsWith(item.getName())) {
            return (File) item;
          }
          break;
        default:
          break;
      }
    }
    return null;
  }

  private Class createNewValueObject(String domain, String modelPath,
                                     String entityName, Field field) {
    String name = generateName(entityName, field);
    String path = String.format("%s.%s", modelPath, name);
    Class newValueObject = new Class(name, path);
    newValueObject.setType(DDDType.VALUE_OBJECT);
    newValueObject.setDomain(domain);
    newValueObject.addField(new Field(PRIVATE, field.getName(), field.getType()));
    newValueObject.addMethod(createEquals());
    newValueObject.addMethod(createHashCode());
    newValueObject.addMethod(createValueObjectGetter(field));
    newValueObject.addMethod(createSideEffectFreeSetter(field));

    LOGGER.info(LOG_CREATE, "ValueObject", newValueObject.getPath());
    return newValueObject;
  }

  private void refactorGetterAndSetter(Class artifact, Field field,
                                       String oldType, String newType) {
    artifact.getMethods().stream()
        .parallel()
        .forEachOrdered(method -> {
          if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())) {
            method.setSignature(method.getSignature().replace(oldType, newType));
          }
        });
  }

  private String generateName(String entityName, Field field) {
    return entityName + modifyFirstChar(field.getName());
  }

  private void refactorMethods(Class artifact) {
    if (needsMethod(artifact, "equals")) {
      artifact.addMethod(createEquals());
    }

    if (needsMethod(artifact, "hashCode")) {
      artifact.addMethod(createHashCode());
    }

    for (Field field : artifact.getFields()) {
      if (needsGetter(artifact, field)) {
        artifact.addMethod(createGetter(field));
      }

      if (needsSetter(artifact, field)) {
        if (!field.getName().toLowerCase().endsWith("id")) {
          artifact.addMethod(createSetter(field));
        } else {
          artifact.addMethod(createSideEffectFreeSetter(field));
        }
      }
    }
  }
}
