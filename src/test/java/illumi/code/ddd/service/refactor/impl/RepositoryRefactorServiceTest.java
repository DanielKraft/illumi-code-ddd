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
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryRefactorServiceTest {
  private RepositoryRefactorService service;
  private DDDRefactorData refactorData;

  private Class entity;

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

    entity = new Class("Entity", "de.test.Entity");
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

    service = new RepositoryRefactorService(refactorData);
  }

  @Test
  void testRefactorInvalidRepository() {
    Interface repo = new Interface("EntityTest", "de.test.domain0.EntityTest");
    repo.setType(DDDType.REPOSITORY);

    DDDFitness fitness = new DDDFitness();
    fitness.addFailedCriteria(DDDIssueType.MAJOR,
        "The Repository '%s' has no nextIdentity method.");
    fitness.addFailedCriteria(DDDIssueType.MAJOR,
        "The Repository '%s' has no findBy/get method.");
    fitness.addFailedCriteria(DDDIssueType.MAJOR,
        "The Repository '%s' has no save/add/insert/put method.");
    fitness.addFailedCriteria(DDDIssueType.MAJOR,
        "The Repository '%s' has no delete/remove method.");
    fitness.addFailedCriteria(DDDIssueType.MINOR,
        "The Repository '%s' has no contains/exists method.");
    fitness.addFailedCriteria(DDDIssueType.MINOR,
        "The Repository '%s' has no update method.");
    fitness.addFailedCriteria(DDDIssueType.INFO,
        "The invalid Repository name");

    repo.setFitness(fitness);
    refactorData.getNewStructure().addInterface(repo);
    refactorData.getModelModule().addContains(repo);

    service.refactor();

    assertAll(() -> assertEquals("de.test.domain0.EntityTestRepository", repo.getPath(), "path"),
        () -> assertEquals(6, repo.getMethods().size(), "#Method"));
  }

  @Test
  void testRefactorRepository() {
    Interface repo = new Interface("EntityRepository", "de.test.domain0.EntityRepository");
    repo.setType(DDDType.REPOSITORY);
    repo.addMethod(new Method("public", "nextIdentity", "de.test.EntityId nextIdentity()"));
    repo.addMethod(new Method("public", "findById", "de.test.Entity findById(de.test.EntityId)"));
    repo.addMethod(new Method("public", "save", "void save(de.test.Entity)"));
    repo.addMethod(new Method("public", "delete", "void delete(de.test.Entity)"));
    repo.addMethod(new Method("public", "contains", "java.lang.Boolean contains(de.test.Entity)"));
    repo.addMethod(new Method("public", "update", "void update(de.test.Entity)"));

    refactorData.getNewStructure().addInterface(repo);
    refactorData.getModelModule().addContains(repo);

    Class repoImpl = new Class("EntityRepositoryImpl", "de.test.domain0.impl.EntityRepositoryImpl");
    repoImpl.setType(DDDType.REPOSITORY);
    repoImpl.addMethod(new Method("public", "nextIdentity",
        "de.test.EntityId nextIdentity()"));
    repoImpl.addMethod(new Method("public", "findById",
        "de.test.Entity findById(de.test.EntityId)"));
    repoImpl.addMethod(new Method("public", "save",
        "void save(de.test.Entity)"));
    repoImpl.addMethod(new Method("public", "delete",
        "void delete(de.test.Entity)"));
    repoImpl.addMethod(new Method("public", "contains",
        "java.lang.Boolean contains(de.test.Entity)"));
    repoImpl.addMethod(new Method("public", "update",
        "void update(de.test.Entity)"));

    refactorData.getNewStructure().addClass(repoImpl);
    ((Package) refactorData.getModelModule().getContains().get(0)).addContains(repoImpl);

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.EntityRepository", repo.getPath(), "path"),
        () -> assertEquals(6, repo.getMethods().size(), "#Method"),
        () -> assertEquals("de.test.domain0.impl.EntityRepositoryImpl", repoImpl.getPath(), "path"),
        () -> assertEquals(6, repoImpl.getMethods().size(), "#Method"));
  }

  @Test
  void testrefactorRepositoryWithoutImpl() {
    Interface factory = new Interface("EntityFactory", "de.test.domain0.impl.EntityFactory");
    factory.setType(DDDType.FACTORY);
    refactorData.getNewStructure().addInterface(factory);
    ((Package) refactorData.getModelModule().getContains().get(0)).addContains(factory);

    Class factoryImpl = new Class("EntityFactoryImpl", "de.test.domain0.impl.EntityFactoryImpl");
    factoryImpl.setType(DDDType.FACTORY);
    refactorData.getNewStructure().addClass(factoryImpl);
    ((Package) refactorData.getModelModule().getContains().get(0)).addContains(factoryImpl);

    Class repo2 = new Class("ValueFactoryImpl", "de.test.domain0.impl.ValueFactoryImpl");
    repo2.setType(DDDType.REPOSITORY);
    refactorData.getNewStructure().addClass(repo2);
    ((Package) refactorData.getModelModule().getContains().get(0)).addContains(repo2);

    Interface repo = new Interface("EntityRepository", "de.test.domain0.EntityRepository");
    repo.setType(DDDType.REPOSITORY);
    repo.addMethod(new Method("public", "nextIdentity", "de.test.EntityId nextIdentity()"));
    repo.addMethod(new Method("public", "findById", "de.test.Entity findById(de.test.EntityId)"));
    repo.addMethod(new Method("public", "save", "void save(de.test.Entity)"));
    repo.addMethod(new Method("public", "delete", "void delete(de.test.Entity)"));
    repo.addMethod(new Method("public", "contains", "java.lang.Boolean contains(de.test.Entity)"));
    repo.addMethod(new Method("public", "update", "void update(de.test.Entity)"));

    refactorData.getNewStructure().addInterface(repo);
    refactorData.getModelModule().addContains(repo);

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.EntityRepository", repo.getPath(), "path"),
        () -> assertEquals(6, repo.getMethods().size(), "#Method"));
  }

  @Test
  void testRefactorInvalidImplRepository() {
    Class repoImpl = new Class("EntityTest", "de.test.domain0.impl.EntityTest");
    repoImpl.setType(DDDType.REPOSITORY);

    DDDFitness fitness = new DDDFitness();
    fitness.addFailedCriteria(DDDIssueType.MAJOR,
        "The Repository '%s' has no nextIdentity method.");
    fitness.addFailedCriteria(DDDIssueType.MAJOR,
        "The Repository '%s' has no findBy/get method.");
    fitness.addFailedCriteria(DDDIssueType.MAJOR,
        "The Repository '%s' has no save/add/insert/put method.");
    fitness.addFailedCriteria(DDDIssueType.MAJOR,
        "The Repository '%s' has no delete/remove method.");
    fitness.addFailedCriteria(DDDIssueType.MINOR,
        "The Repository '%s' has no contains/exists method.");
    fitness.addFailedCriteria(DDDIssueType.MINOR,
        "The Repository '%s' has no update method.");
    fitness.addFailedCriteria(DDDIssueType.INFO,
        "The invalid Repository name");

    repoImpl.setFitness(fitness);
    refactorData.getNewStructure().addClass(repoImpl);
    ((Package) refactorData.getModelModule().getContains().get(0)).addContains(repoImpl);

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.impl.EntityTestRepositoryImpl", repoImpl.getPath(),
            "path"),
        () -> assertEquals(6, repoImpl.getMethods().size(), "#Method"));
  }

  @Test
  void testFindIdOfSuperclass() {
    Class other = new Class("Other", "de.test.Other");
    other.setType(DDDType.ENTITY);
    other.setSuperClass(entity);
    refactorData.getNewStructure().addClass(other);
    refactorData.getModelModule().addContains(other);

    Interface repo = new Interface("OtherRepository", "de.test.domain0.OtherRepository");
    repo.setType(DDDType.REPOSITORY);
    repo.addMethod(new Method("public", "nextIdentity", "de.test.EntityId nextIdentity()"));
    repo.addMethod(new Method("public", "findById", "de.test.Entity findById(de.test.EntityId)"));
    repo.addMethod(new Method("public", "save", "void save(de.test.Entity)"));
    repo.addMethod(new Method("public", "delete", "void delete(de.test.Entity)"));
    repo.addMethod(new Method("public", "contains", "java.lang.Boolean contains(de.test.Entity)"));
    repo.addMethod(new Method("public", "update", "void update(de.test.Entity)"));

    refactorData.getNewStructure().addInterface(repo);
    refactorData.getModelModule().addContains(repo);

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.OtherRepository", repo.getPath(), "path"),
        () -> assertEquals(6, repo.getMethods().size(), "#Method"));
  }

  @Test
  void testRefactorWithoutId() {
    Class other = new Class("Other", "de.test.Other");
    other.setType(DDDType.ENTITY);
    refactorData.getNewStructure().addClass(other);
    refactorData.getModelModule().addContains(other);

    Interface repo = new Interface("OtherRepository", "de.test.domain0.OtherRepository");
    repo.setType(DDDType.REPOSITORY);
    repo.addMethod(new Method("public", "nextIdentity", "de.test.EntityId nextIdentity()"));
    repo.addMethod(new Method("public", "findById", "de.test.Entity findById(de.test.EntityId)"));
    repo.addMethod(new Method("public", "save", "void save(de.test.Entity)"));
    repo.addMethod(new Method("public", "delete", "void delete(de.test.Entity)"));
    repo.addMethod(new Method("public", "contains", "java.lang.Boolean contains(de.test.Entity)"));
    repo.addMethod(new Method("public", "update", "void update(de.test.Entity)"));

    refactorData.getNewStructure().addInterface(repo);
    refactorData.getModelModule().addContains(repo);

    service.refactor();

    assertAll(
        () -> assertEquals("de.test.domain0.OtherRepository", repo.getPath(), "path"),
        () -> assertEquals(6, repo.getMethods().size(), "#Method"));
  }

  @Test
  void testRefactorWithoutRepository() {
    Interface factory = new Interface("EntityFactory", "de.test.domain0.EntityFactory");
    factory.setType(DDDType.FACTORY);
    refactorData.getNewStructure().addInterface(factory);
    refactorData.getModelModule().addContains(factory);

    Class factoryImpl = new Class("EntityFactoryImpl", "de.test.domain0.impl.EntityFactoryImpl");
    factoryImpl.setType(DDDType.FACTORY);
    refactorData.getNewStructure().addClass(factoryImpl);
    ((Package) refactorData.getModelModule().getContains().get(0)).addContains(factoryImpl);

    Interface factory2 = new Interface("EntityFactory2", "de.test.domain0.impl.EntityFactory2");
    factory2.setType(DDDType.FACTORY);
    refactorData.getNewStructure().addInterface(factory2);
    ((Package) refactorData.getModelModule().getContains().get(0)).addContains(factory2);

    service.refactor();

    assertAll(
        () -> assertEquals(4, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"));
  }
}