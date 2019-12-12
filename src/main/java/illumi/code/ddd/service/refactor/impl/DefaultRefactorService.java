package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.File;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Method;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.service.refactor.ArtifactRefactorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultRefactorService implements ArtifactRefactorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRefactorService.class);

  static final String PRIVATE = "private";
  static final String PUBLIC = "public";
  static final String LOG_CREATE = "[CREATE] - {} - {}";
  static final String REPOSITORY_IMPL = "RepositoryImpl";
  static final String REPOSITORY = "Repository";
  static final String FACTORY_IMPL = "FactoryImpl";
  static final String FACTORY = "Factory";
  static final String METHOD = "Method";

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
        .forEachOrdered(item -> {
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

  abstract void refactor(Package model);

  private Package getModelOfDomain(Artifact item) {
    return (Package) ((Package) item).getContains().get(0);
  }

  Class createImpl(Package impl, Interface interfaceFile, DDDType dddType) {
    String name = String.format("%sImpl", interfaceFile.getName());
    String path = String.format("%s.%s", impl.getPath(), name);
    Class classImpl = new Class(name, path);
    classImpl.setType(dddType);
    classImpl.setDomain(interfaceFile.getDomain());

    classImpl.addImplInterface(interfaceFile);

    copyMethods(interfaceFile, classImpl);

    return classImpl;
  }

  Interface createInterface(Package model, Class classImpl, DDDType dddType) {
    String name = classImpl.getName().replace("Impl", "");
    String path = String.format("%s.%s", model.getPath(), name);
    Interface interfaceFile = new Interface(name, path);
    interfaceFile.setType(dddType);
    interfaceFile.setDomain(classImpl.getDomain());

    interfaceFile.addImplInterface(interfaceFile);

    copyMethods(classImpl, interfaceFile);

    return interfaceFile;
  }

  Class getImpl(Package impl, Interface anInterface, DDDType dddType) {
    for (Artifact item : impl.getContains()) {
      if (item instanceof Class
          && item.isTypeOf(dddType)
          && item.getName().toLowerCase().contains(anInterface.getName().toLowerCase())) {
        return (Class) item;
      }
    }
    return null;
  }

  Class getEntity(Package model, File file) {
    for (Artifact artifact : model.getContains()) {
      if (artifact instanceof Class
          && file.getName().toLowerCase().contains(artifact.getName().toLowerCase())) {
        return (Class) artifact;
      }
    }
    return null;
  }

  String getIdOfEntity(Class entity) {
    for (Field field : entity.getFields()) {
      if (Field.isId(field)) {
        return field.getType();
      }
    }

    if (entity.getSuperClass() != null) {
      return getIdOfEntity(entity.getSuperClass());
    }

    return "";
  }

  boolean needsMethod(Class artifact, String name) {
    for (Method method : artifact.getMethods()) {
      if (method.getName().equalsIgnoreCase(name)) {
        return false;
      }
    }
    return true;
  }

  boolean needsGetter(Class artifact, Field field) {
    for (Method method : artifact.getMethods()) {
      if (method.getName().equalsIgnoreCase("get" + field.getName())) {
        refactorGetter(method, field);
        return false;
      }
    }
    return true;
  }

  boolean needsSetter(Class artifact, Field field) {
    for (Method method : artifact.getMethods()) {
      if (method.getName().equalsIgnoreCase("set" + field.getName())) {
        refactorSetter(method, field);
        return false;
      }
    }
    return true;
  }

  boolean needsSideEffectFreeSetter(Class artifact, Field field) {
    for (Method method : artifact.getMethods()) {
      if (method.getName().equalsIgnoreCase("set" + field.getName())) {
        refactorSideEffectFreeSetter(method, field);
        return false;
      }
    }
    return true;
  }

  Method createMethod(String name, String attribute) {
    return createMethod("void", name, attribute);
  }

  Method createMethod(String value, String name, String attribute) {
    String signature = String.format("%s %s(%s)", value, name, attribute);
    LOGGER.info(LOG_CREATE, METHOD, signature);
    return new Method(PUBLIC, name, signature);
  }

  Method createEquals() {
    LOGGER.info(LOG_CREATE, METHOD, "java.lang.Boolean equals(Object)");
    return new Method(PUBLIC, "equals", "java.lang.Boolean equals(Object)");
  }

  Method createHashCode() {
    LOGGER.info(LOG_CREATE, METHOD, "java.lang.Integer hashCode()");
    return new Method(PUBLIC, "hashCode", "java.lang.Integer hashCode()");
  }

  Method createGetter(Field field) {
    String name = String.format("get%s", modifyFirstChar(field.getName()));
    String signature = String.format("%s %s()", field.getType(), name);
    LOGGER.info(LOG_CREATE, METHOD, signature);
    return new Method(PUBLIC, name, signature);
  }

  Method createSetter(Field field) {
    String name = String.format("set%s", modifyFirstChar(field.getName()));
    String signature = String.format("void %s(%s)", name, field.getType());
    LOGGER.info(LOG_CREATE, METHOD, signature);
    return new Method(PUBLIC, name, signature);
  }

  Method createValueObjectGetter(Field field) {
    String name = field.getName();
    String signature = String.format("%s %s()", field.getType(), name);
    LOGGER.info(LOG_CREATE, METHOD, signature);
    return new Method(PUBLIC, name, signature);
  }

  Method createSideEffectFreeSetter(Field field) {
    String name = String.format("set%s", modifyFirstChar(field.getName()));
    String signature = String.format("void %s(%s)", name, field.getType());
    LOGGER.info(LOG_CREATE, METHOD, signature);
    return new Method(PRIVATE, name, signature);
  }

  private void refactorGetter(Method method, Field field) {
    if (!method.getSignature().contains(field.getType())) {
      String split = method.getSignature().split(" ")[1];
      method.setSignature(String.format("%s %s", field.getType(), split));
    }
  }

  private void refactorSideEffectFreeSetter(Method method, Field field) {
    if (method.getVisibility().equalsIgnoreCase(PUBLIC)) {
      method.setVisibility(PRIVATE);
    }
    refactorSetter(method, field);
  }

  private void refactorSetter(Method method, Field field) {
    if (!method.getSignature().contains(field.getType())) {
      String split = method.getSignature().split("[(]")[0];
      method.setSignature(String.format("%s(%s)", split, field.getType()));
    }
  }

  void copyMethods(File oldFile, File newFile) {
    oldFile.getMethods().stream()
        .parallel()
        .forEachOrdered(method -> newFile.addMethod(new Method(method)));
  }

  String modifyFirstChar(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
