package illumi.code.ddd.service.refactor.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Method;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.fitness.DDDIssueType;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class FactoryRefactorServiceTest {
  private FactoryRefactorService service;
  private DDDRefactorData refactorData;

  private Package impl;

  @BeforeEach
  void init() {
    DDDStructure structure = new DDDStructure();
    structure.setPath("de.test");

    Package module = new Package("test", "de.test");
    module.setType(DDDType.MODULE);

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

    impl = ((Package) refactorData.getModelModule().getContains().get(0));

    Class entity = new Class("Entity", "de.test.Entity");
    entity.setType(DDDType.ENTITY);
    entity.addField(new Field("private", "desc", "java.lang.String"));
    entity.addField(new Field("private", "id", "de.test.EntityId"));
    refactorData.getNewStructure().addClass(entity);
    refactorData.getModelModule().addContains(entity);

    Class id = new Class("EntityId", "de.test.EntityId");
    id.setType(DDDType.VALUE_OBJECT);
    id.addField(new Field("private", "desc", "java.lang.String"));
    id.addField(new Field("private", "id", "java.lang.Long"));
    id.addMethod(new Method("public", "equals", "java.lang.Boolean equals(Object)"));
    id.addMethod(new Method("public", "hashCode", "java.lang.Integer hashCode()"));
    id.addMethod(new Method("public", "id", "java.lang.Long id()"));
    id.addMethod(new Method("public", "setId", "void setId(java.lang.Long)"));
    refactorData.getNewStructure().addClass(id);
    refactorData.getModelModule().addContains(id);

    Interface repo = new Interface("EntityRepository", "de.test.domain0.EntityRepository");
    repo.setType(DDDType.REPOSITORY);
    refactorData.getNewStructure().addInterface(repo);
    refactorData.getModelModule().addContains(repo);

    Class repoImpl = new Class("EntityRepositoryImpl", "de.test.domain0.impl.EntityRepositoryImpl");
    repoImpl.setType(DDDType.REPOSITORY);
    refactorData.getNewStructure().addClass(repoImpl);
    impl.addContains(repoImpl);

    service = new FactoryRefactorService(refactorData);
  }

  @Test
  void testRefactorFactory() {
    Interface artifact = new Interface("EntityFactory", "de.test.domain0.EntityFactory");
    artifact.setType(DDDType.FACTORY);
    artifact.setDomain("domain0");
    artifact.addMethod(new Method("public", "create", "void create(...)"));
    refactorData.getNewStructure().addInterface(artifact);
    refactorData.getModelModule().addContains(artifact);

    Class artifactImpl = new Class("EntityFactoryImpl", "de.test.domain0.impl.EntityFactoryImpl");
    artifactImpl.setType(DDDType.FACTORY);
    artifact.setDomain("domain0");
    artifactImpl.addImplInterface(artifact);
    artifactImpl.addMethod(new Method("public", "create", "void create(...)"));
    refactorData.getNewStructure().addClass(artifactImpl);
    impl.addContains(artifactImpl);

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.EntityFactory", artifact.getPath(), "path"),
        () -> assertEquals(1, artifact.getMethods().size(), "#Method"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals(5, refactorData.getNewStructure().getClasses().size(), "#Class"));
  }

  @Test
  void testRefactorFactoryWithoutImpl() {
    Interface artifact = new Interface("EntityFactory", "de.test.domain0.EntityFactory");
    artifact.setType(DDDType.FACTORY);
    artifact.addMethod(new Method("public", "create", "void create(...)"));
    refactorData.getNewStructure().addInterface(artifact);
    refactorData.getModelModule().addContains(artifact);

    assertEquals(4, refactorData.getNewStructure().getClasses().size(), "#Class");

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.EntityFactory", artifact.getPath(), "path"),
        () -> assertEquals(1, artifact.getMethods().size(), "#Method"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals(5, refactorData.getNewStructure().getClasses().size(), "#Class"));
  }

  @Test
  void testRefactorInvalidFactoryWithoutImpl() {
    Interface artifact = new Interface("EntityTest", "de.test.domain0.EntityTest");
    artifact.setType(DDDType.FACTORY);

    DDDFitness fitness =  new DDDFitness();
    fitness.addFailedCriteria(DDDIssueType.MAJOR, "The Factory '%s' has no create method.");
    fitness.addFailedCriteria(DDDIssueType.INFO, "The invalid Factory name");

    artifact.setFitness(fitness);
    refactorData.getNewStructure().addInterface(artifact);
    refactorData.getModelModule().addContains(artifact);

    assertEquals(4, refactorData.getNewStructure().getClasses().size(), "#Class");

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.EntityTestFactory", artifact.getPath(), "path"),
        () -> assertEquals(1, artifact.getMethods().size(), "#Method"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals(5, refactorData.getNewStructure().getClasses().size(), "#Class"));
  }

  @Test
  void testRefactorFactoryImpl() {
    Class artifact = new Class("EntityFactoryImpl", "de.test.domain0.impl.EntityFactoryImpl");
    artifact.setType(DDDType.FACTORY);
    artifact.setDomain("domain0");
    artifact.addMethod(new Method("public", "create", "void create(...)"));
    refactorData.getNewStructure().addClass(artifact);
    impl.addContains(artifact);

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.impl.EntityFactoryImpl", artifact.getPath(), "path"),
        () -> assertEquals(1, artifact.getMethods().size(), "#Method"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals(5, refactorData.getNewStructure().getClasses().size(), "#Class"));
  }

  @Test
  void testRefactorInvalidFactoryImplWithoutInterface() {
    Class artifact = new Class("EntityTest", "de.test.domain0.EntityTest");
    artifact.setType(DDDType.FACTORY);

    DDDFitness fitness =  new DDDFitness();
    fitness.addFailedCriteria(DDDIssueType.MAJOR, "The Factory '%s' has no create method.");
    fitness.addFailedCriteria(DDDIssueType.INFO, "The invalid Factory name");

    artifact.setFitness(fitness);
    refactorData.getNewStructure().addClass(artifact);
    impl.addContains(artifact);

    assertEquals(1, refactorData.getNewStructure().getInterfaces().size(), "#Interface");

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.EntityTestFactoryImpl", artifact.getPath(), "path"),
        () -> assertEquals(1, artifact.getMethods().size(), "#Method"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals(5, refactorData.getNewStructure().getClasses().size(), "#Class"));
  }

  @Test
  void testInvalidStructure() {
    Class artifact = new Class("TestFactoryImpl", "de.test.domain0.impl.TestFactoryImpl");
    artifact.setType(DDDType.FACTORY);
    refactorData.getNewStructure().addClass(artifact);
    impl.addContains(artifact);

    Interface repo2 = new Interface("EntityRepository2", "de.test.domain0.impl.EntityRepository2");
    repo2.setType(DDDType.REPOSITORY);
    refactorData.getNewStructure().addInterface(repo2);
    impl.addContains(repo2);

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.impl.TestFactoryImpl", artifact.getPath(), "path"),
        () -> assertEquals(0, artifact.getMethods().size(), "#Method"),
        () -> assertEquals(3, refactorData.getNewStructure().getInterfaces().size(), "#Interface"),
        () -> assertEquals(5, refactorData.getNewStructure().getClasses().size(), "#Class"));
  }
}