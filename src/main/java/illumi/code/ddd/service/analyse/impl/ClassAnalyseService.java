package illumi.code.ddd.service.analyse.impl;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Method;
import org.apache.commons.lang3.StringUtils;

public class ClassAnalyseService {
  private static final String REPOSITORY = "Repository";
  private static final String FACTORY = "Factory";

  private Class artifact;
  private DDDStructure structure;

  public ClassAnalyseService(Class artifact, DDDStructure structure) {
    this.artifact = artifact;
    this.structure = structure;
  }

  public ClassAnalyseService(Class artifact) {
    this.artifact = artifact;
  }

  /**
   * Set DDDType of the class.
   */
  public void setType() {
    if (isInfrastructure()) {
      this.artifact.setType(DDDType.INFRASTRUCTURE);
    } else if (this.artifact.getType() == null) {
      if (isValueObject()) {
        this.artifact.setType(DDDType.VALUE_OBJECT);
      } else if (isEntity()) {
        this.artifact.setType(DDDType.ENTITY);
      } else if (isApplicationService()) {
        this.artifact.setType(DDDType.APPLICATION_SERVICE);
      } else if (isService()) {
        this.artifact.setType(DDDType.SERVICE);
      } else {
        this.artifact.setType(DDDType.INFRASTRUCTURE);
      }
    }
  }

  private boolean isInfrastructure() {
    return this.artifact.getName().toUpperCase().contains("JPA")
        || this.artifact.getName().toUpperCase().contains("CRUD");
  }

  private boolean isValueObject() {
    int ctr = 0;
    for (Field field : artifact.getFields()) {
      if (isInvalidValueObjectField(field)) {
        return false;
      } else if (field.getType().startsWith("java.")
          || field.getType().contains(structure.getPath())) {
        ctr++;
      }
    }
    return !this.artifact.getFields().isEmpty()
        && (ctr == this.artifact.getFields().size()
        || containsModelMethods());
  }

  private boolean isInvalidValueObjectField(Field field) {
    return isId(field)
        || isConstant(field)
        || isRepositoryOrFactory(field);
  }

  private boolean isId(Field field) {
    return Field.isId(field)
        && !(this.artifact.getName().toLowerCase().endsWith("id"));
  }

  private boolean isRepositoryOrFactory(Field field) {
    String[] split = field.getType().split("[.]");
    String type = split[split.length - 1];

    return type.contains(REPOSITORY)
        || field.getName().contains(REPOSITORY.toLowerCase())
        || type.contains(FACTORY)
        || field.getName().contains(FACTORY.toLowerCase());
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

  private boolean isEntity() {

    for (Field field : this.artifact.getFields()) {
      if (isConstant(field)) {
        return false;
      } else if (Field.isId(field)) {
        return true;
      }
    }
    return !this.artifact.getFields().isEmpty()
        && containsModelMethods()
        && !containsEntityName();
  }

  private boolean isService() {
    for (Field field : this.artifact.getFields()) {
      if (field.getType().contains(REPOSITORY)) {
        return true;
      }
    }
    return containsEntityName();
  }

  private boolean isApplicationService() {
    for (Method method : this.artifact.getMethods()) {
      if (method.getName().equalsIgnoreCase("main")) {
        return true;
      }
    }
    return false;
  }

  private boolean isConstant(Field field) {
    return StringUtils.isAllUpperCase(field.getName());
  }

  private boolean containsEntityName() {
    for (Class item : structure.getClasses()) {
      if (this.artifact != item
          && this.artifact.getName().contains(item.getName())
          && !this.artifact.getName().equals(item.getName() + "s")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Analyse if this class is a domain event.
   */
  public void setDomainEvent() {
    switch (this.artifact.getType()) {
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

  /**
   * Analyse if this class depends on infrastructure.
   */
  public void setInfrastructure() {
    switch (this.artifact.getType()) {
      case ENTITY:
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
      if (usedByClass(item)) {
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
