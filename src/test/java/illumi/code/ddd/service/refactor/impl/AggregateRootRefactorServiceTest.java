package illumi.code.ddd.service.refactor.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AggregateRootRefactorServiceTest {

  private AggregateRootRefactorService service;
  private DDDRefactorData refactorData;

  private Package model;
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

    model = (Package) ((Package) refactorData.getDomainModule().getContains().get(0))
        .getContains().get(0);
    impl = (Package) model.getContains().get(0);

    service = new AggregateRootRefactorService(refactorData);
  }

  @Test
  void testRefactorAggregateRoot() {

    Interface repository =
        new Interface("RootRepository", "de.test.domain.root.model.RootRepository");
    repository.setType(DDDType.REPOSITORY);
    model.addContains(repository);
    refactorData.getNewStructure().addInterface(repository);

    Class repositoryImpl =
        new Class("RootRepositoryImpl", "de.test.domain.root.model.impl.RootRepositoryImpl");
    repositoryImpl.setType(DDDType.REPOSITORY);
    impl.addContains(repositoryImpl);
    refactorData.getNewStructure().addClass(repositoryImpl);

    Interface factory =
        new Interface("RootFactory", "de.test.domain.root.model.RootFactory");
    factory.setType(DDDType.FACTORY);
    model.addContains(factory);
    refactorData.getNewStructure().addInterface(factory);

    Class factoryImpl =
        new Class("RootFactoryImpl", "de.test.domain.root.model.impl.RootFactoryImpl");
    factoryImpl.setType(DDDType.FACTORY);
    impl.addContains(factoryImpl);
    refactorData.getNewStructure().addClass(factoryImpl);

    assertAll(
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"));

    service.refactor();

    assertAll(
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"));
  }

  @Test
  void testRefactorInvalidAggregateRoot() {
    assertAll(
        () -> assertEquals(1, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(0, refactorData.getNewStructure().getInterfaces().size(), "#Interface"));

    service.refactor();

    assertAll(
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"));
  }

  @Test
  void testRefactorInvalidAggregateRootWithEntity() {
    Class entity = new Class("Entity", "de.test.domain.root.model.Entity");
    entity.setType(DDDType.ENTITY);
    model.addContains(entity);
    refactorData.getNewStructure().addClass(entity);

    assertAll(
        () -> assertEquals(2, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(0, refactorData.getNewStructure().getInterfaces().size(), "#Interface"));

    service.refactor();

    assertAll(
        () -> assertEquals(4, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"));
  }

  @Test
  void testRefactorAggregateRootWithOtherInterfaces() {
    Interface repository =
        new Interface("OtherRepository", "de.test.domain.root.model.OtherRepository");
    repository.setType(DDDType.REPOSITORY);
    model.addContains(repository);
    refactorData.getNewStructure().addInterface(repository);

    Class repositoryImpl =
        new Class("OtherRepositoryImpl", "de.test.domain.root.model.impl.OtherRepositoryImpl");
    repositoryImpl.setType(DDDType.REPOSITORY);
    impl.addContains(repositoryImpl);
    refactorData.getNewStructure().addClass(repositoryImpl);

    Interface factory =
        new Interface("OtherFactory", "de.test.domain.root.model.OtherFactory");
    factory.setType(DDDType.FACTORY);
    model.addContains(factory);
    refactorData.getNewStructure().addInterface(factory);

    Class factoryImpl =
        new Class("OtherFactoryImpl", "de.test.domain.root.model.impl.OtherFactoryImpl");
    factoryImpl.setType(DDDType.FACTORY);
    impl.addContains(factoryImpl);
    refactorData.getNewStructure().addClass(factoryImpl);

    assertAll(
        () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(2, refactorData.getNewStructure().getInterfaces().size(), "#Interface"));

    service.refactor();

    assertAll(
        () -> assertEquals(5, refactorData.getNewStructure().getClasses().size(), "#Class"),
        () -> assertEquals(4, refactorData.getNewStructure().getInterfaces().size(), "#Interface"));
  }
}
