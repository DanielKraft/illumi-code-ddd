package illumi.code.ddd.service.refactor.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Annotation;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Enum;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Method;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssignServiceTest {

  private DDDStructure structure;
  private AssignService service;
  private DDDRefactorData refactorData;

  private Package module;

  @BeforeEach
  void init() {
    structure = new DDDStructure();
    structure.setPath("de.test");

    module = new Package("test", "de.test");
    module.setType(DDDType.MODULE);

    Class root2 = new Class("Root2", "de.Root2");
    root2.setType(DDDType.AGGREGATE_ROOT);
    root2.setDomain("domain1");
    module.addContains(root2);
    structure.addClass(root2);

    Class root = new Class("Root", "de.test.Root");
    root.setType(DDDType.AGGREGATE_ROOT);
    root.setDomain("domain0");
    module.addContains(root);
    structure.addClass(root);

    ArrayList<Artifact> data = new ArrayList<>();
    data.add(module);
    structure.setStructure(data);

    refactorData = new DDDRefactorData(structure);

    new InitializeService(refactorData).initModules();

    service = new AssignService(refactorData);
  }

  @Test
  void testAssignApplicationService() {
    Class artifact = new Class("ApplicationService", "de.test.ApplicationService");
    artifact.setType(DDDType.APPLICATION_SERVICE);
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.application.ApplicationService", artifact.getPath(), "path"));
  }

  @Test
  void testAssignInfrastructure() {
    Class artifact = new Class("Infrastructure", "de.test.Infrastructure");
    artifact.setType(DDDType.INFRASTRUCTURE);
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.infrastructure.Infrastructure", artifact.getPath(), "path"));
  }

  @Test
  void testAssignService() {
    Class artifact = new Class("Domain0Service", "de.test.Domain0Service");
    artifact.setType(DDDType.SERVICE);
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.application.root.Domain0Service", artifact.getPath(), "path"));
  }

  @Test
  void testAssignRepository() {
    Class artifact = new Class("Repository", "de.test.domain0.Repository");
    artifact.setType(DDDType.REPOSITORY);
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.domain.root.model.impl.Repository", artifact.getPath(), "path"));
  }

  @Test
  void testAssignEntity() {
    Class artifact = new Class("Entity", "de.test.Entity");
    artifact.setType(DDDType.ENTITY);
    artifact.setDomain("domain0");
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.domain.root.model.Entity", artifact.getPath(), "path"));
  }

  @Test
  void testAssignModel() {
    Class artifact = new Class("Entity", "de.test.Entity");
    artifact.setType(DDDType.ENTITY);
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.domain.model.Entity", artifact.getPath(), "path"));
  }

  @Test
  void testAssignWithDependency() {
    Class artifact = new Class("Entity", "de.test.Entity");
    artifact.setType(DDDType.ENTITY);
    artifact.addDependencies("de.test.Test");
    artifact.addDependencies("de.test.Root");
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.domain.root.model.Entity", artifact.getPath(), "path"));
  }

  @Test
  void testAssignModelWithDependency() {
    Class model = new Class("Model", "de.test.Model");
    model.setType(DDDType.ENTITY);
    module.addContains(model);
    structure.addClass(model);

    Class artifact = new Class("Entity", "de.test.Entity");
    artifact.setType(DDDType.ENTITY);
    artifact.addDependencies("de.test.Model");
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(4, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.domain.model.Entity", artifact.getPath(), "path"));
  }

  @Test
  void testAssignWithFields() {
    Class artifact = new Class("Entity", "de.test.Entity");
    artifact.setType(DDDType.ENTITY);
    artifact.addField(new Field("private", "test", "de.Other"));
    artifact.addField(new Field("private", "rootId", "de.Int"));
    artifact.addMethod(new Method("private", "test", "de.Other test()"));
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.domain.root.model.Entity", artifact.getPath(), "path"));
  }

  @Test
  void testAssignModelWithFields() {
    Class model = new Class("Model", "de.test.Model");
    model.setType(DDDType.ENTITY);
    module.addContains(model);
    structure.addClass(model);

    Class artifact = new Class("Entity", "de.test.Entity");
    artifact.setType(DDDType.ENTITY);
    artifact.addField(new Field("private", "modelId", "de.test.Model"));
    module.addContains(artifact);
    structure.addClass(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(4, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals("test.domain.model.Entity", artifact.getPath(), "path"));
  }

  @Test
  void testAssignInterfaceService() {
    Interface artifact = new Interface("Service", "de.test.Service");
    artifact.setType(DDDType.SERVICE);
    artifact.setDomain("domain0");
    module.addContains(artifact);
    structure.addInterface(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(2, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(1, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals("test.application.root.Service", artifact.getPath(), "path"));
  }

  @Test
  void testAssignInterfaceController() {
    Interface artifact = new Interface("Controller", "de.test.Controller");
    artifact.setType(DDDType.INFRASTRUCTURE);
    module.addContains(artifact);
    structure.addInterface(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(2, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(1, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals("test.infrastructure.Controller", artifact.getPath(), "path"));
  }

  @Test
  void testAssignInterfaceRepositoryWithDependency() {
    Class entity = new Class("Entity", "test.domain.root.model.Entity");
    entity.setType(DDDType.ENTITY);
    entity.setDomain("domain0");
    module.addContains(entity);
    structure.addClass(entity);

    Interface artifact = new Interface("EntityRepository", "de.test.EntityRepository");
    artifact.setType(DDDType.REPOSITORY);
    module.addContains(artifact);
    structure.addInterface(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(1, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals("test.domain.root.model.EntityRepository", artifact.getPath(), "path"));
  }

  @Test
  void testAssignInterfaceModel() {
    Interface artifact = new Interface("Repository", "de.test.Repository");
    artifact.setType(DDDType.REPOSITORY);
    module.addContains(artifact);
    structure.addInterface(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(2, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(1, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals("test.domain.model.Repository", artifact.getPath(), "path"));
  }

  @Test
  void testAssignEnum() {
    Enum artifact = new Enum("Enum", "de.test.Enum");
    artifact.setType(DDDType.VALUE_OBJECT);
    artifact.setDomain("domain0");
    module.addContains(artifact);
    structure.addEnum(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(2, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(1, refactorData.getNewStructure().getEnums().size(), "#Enum"),
        () -> assertEquals("test.domain.root.model.Enum", artifact.getPath(), "path"));
  }

  @Test
  void testAssignEnumToModel() {
    Enum artifact = new Enum("Enum", "de.test.Enum");
    artifact.setType(DDDType.VALUE_OBJECT);
    module.addContains(artifact);
    structure.addEnum(artifact);

    service.assign();

    assertAll(
        () -> assertEquals(13, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(2, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(1, refactorData.getNewStructure().getEnums().size(), "#Enum"),
        () -> assertEquals("test.domain.model.Enum", artifact.getPath(), "path"));
  }

  @Test
  void testAssignAnnotation() {
    Annotation artifact = new Annotation("Annotation", "de.test.Annotation");
    artifact.setType(DDDType.INFRASTRUCTURE);
    module.addContains(artifact);
    structure.addAnnotation(artifact);

    service.assign();

    final DDDStructure result = refactorData.getNewStructure();

    assertAll(
        () -> assertEquals(13, result.getPackages().size(), "#Package"),
        () -> assertEquals(2, result.getClasses().size(), "#Class"),
        () -> assertEquals(1, result.getAnnotations().size(), "#Annotation"),
        () -> assertEquals("test.infrastructure.Annotation", artifact.getPath(), "path"));
  }

  @Test
  void testRefactorInvalidDomain() {
    refactorData.getNewStructure().addDomain("invalid");

    service.assign();

    final DDDStructure result = refactorData.getNewStructure();

    assertAll(
        () -> assertEquals(13, result.getPackages().size(), "#Package"),
        () -> assertEquals(2, result.getClasses().size(), "#Class"));
  }
}
