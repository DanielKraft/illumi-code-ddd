package illumi.code.ddd.service.analyse.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Package;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PackageAnalyseServiceTest {

  private DDDStructure structure;

  @BeforeEach
  void init() {

    structure = new DDDStructure();
    structure.setPath("de.test");
    structure.addDomain("domain");
  }

  @Test
  void testSetAggregateRoot() {
    final Package module = new Package("domain", "de.test.domain");

    Class entity1 = new Class("Entity1", "de.test.domain.Entity1");
    entity1.setType(DDDType.ENTITY);
    entity1.addDependencies("de.test.domain.Entity2");
    entity1.addDependencies("de.test.domain.Entity4");
    module.addContains(entity1);

    Class entity2 = new Class("Entity2", "de.test.domain.Entity2");
    entity2.setType(DDDType.ENTITY);
    module.addContains(entity2);

    Class entity3 = new Class("Entity3", "de.test.domain.Entity3");
    entity3.setType(DDDType.ENTITY);
    entity3.addDependencies("de.test.domain.Entity2");
    entity3.addDependencies("de.test.domain.ValueObject");
    module.addContains(entity3);

    Class entity4 = new Class("Entity4", "de.test.domain.Entity4");
    entity4.setType(DDDType.ENTITY);
    module.addContains(entity4);

    Class value = new Class("ValueObject", "de.test.domain.ValueObject");
    value.setType(DDDType.VALUE_OBJECT);
    module.addContains(value);

    Class root = new Class("Root", "de.test.domain.Root");
    root.setType(DDDType.ENTITY);
    root.addDependencies("de.test.domain.Entity1");
    root.addDependencies("de.test.domain.Entity3");
    module.addContains(root);

    PackageAnalyseService service = new PackageAnalyseService(module, structure);
    service.setAggregateRoot();

    assertAll("Should find an aggregate root",
        () -> assertEquals(DDDType.VALUE_OBJECT, value.getType(), "Value Object"),
        () -> assertEquals(DDDType.ENTITY, entity1.getType(), "Entity 1"),
        () -> assertEquals(DDDType.ENTITY, entity2.getType(), "Entity 1"),
        () -> assertEquals(DDDType.ENTITY, entity3.getType(), "Entity 1"),
        () -> assertEquals(DDDType.ENTITY, entity4.getType(), "Entity 1"),
        () -> assertEquals(DDDType.AGGREGATE_ROOT, root.getType(), "Aggregate Root"));
  }

  @Test
  void testSetAggregateRootWithMultipleCandidates() {
    Package module = new Package("domain", "de.test.domain");

    Class entity = new Class("Entity", "de.test.domain.Entity");
    entity.setType(DDDType.ENTITY);
    module.addContains(entity);

    Class root = new Class("Domain", "de.test.domain.Domain");
    root.setType(DDDType.ENTITY);
    module.addContains(root);

    PackageAnalyseService service = new PackageAnalyseService(module, structure);
    service.setAggregateRoot();

    assertAll("Should find an aggregate root",
        () -> assertEquals(DDDType.ENTITY, entity.getType(), "Entity"),
        () -> assertEquals(DDDType.AGGREGATE_ROOT, root.getType(), "Aggregate Root"));
  }

  @Test
  void testSetAggregateRootWithOneCandidate() {
    Package module = new Package("domain", "de.test.domain");

    Class root = new Class("Root", "de.test.domain.Root");
    root.setType(DDDType.ENTITY);
    module.addContains(root);

    System.out.println(module.getContains().size());

    PackageAnalyseService service = new PackageAnalyseService(module, structure);
    service.setAggregateRoot();

    assertAll("Should find an aggregate root",
        () -> assertEquals(DDDType.AGGREGATE_ROOT, root.getType()));
  }

  @Test
  void testSetAggregateRootWithNoEntities() {
    Package module = new Package("domain", "de.test.domain");

    PackageAnalyseService service = new PackageAnalyseService(module, structure);
    service.setAggregateRoot();

    assertAll("Should find no aggregate root",
        () -> assertEquals(0, module.getContains().size()));
  }

  @Test
  void testSetAggregateRootOfNoDomain() {
    Package module = new Package("infra", "de.test.infra");

    Class root = new Class("Root", "de.test.infra.Root");
    root.setType(DDDType.ENTITY);
    module.addContains(root);

    PackageAnalyseService service = new PackageAnalyseService(module, structure);
    service.setAggregateRoot();

    assertAll("Should find no aggregate root",
        () -> assertEquals(DDDType.ENTITY, root.getType()));
  }

  @Test
  void testSetAggregateRootByDependencies1() {
    Package module = new Package("domain", "de.test.domain");

    Class entity = new Class("Entity", "de.test.domain.Entity");
    entity.addField(new Field("private", "root", "de.test.domain.Root"));
    entity.setType(DDDType.ENTITY);
    module.addContains(entity);

    Class root = new Class("Root", "de.test.domain.Root");
    root.setType(DDDType.ENTITY);
    root.addField(new Field("private", "entitys", "java.util.List"));
    module.addContains(root);

    PackageAnalyseService service = new PackageAnalyseService(module, structure);
    service.setAggregateRoot();

    assertAll("Should find an aggregate root",
        () -> assertEquals(DDDType.ENTITY, entity.getType(), "Entity"),
        () -> assertEquals(DDDType.AGGREGATE_ROOT, root.getType(), "Aggregate Root"));
  }

  @Test
  void testSetAggregateRootByDependencies2() {
    Package module = new Package("domain", "de.test.domain");

    Class root = new Class("Root", "de.test.domain.Root");
    root.setType(DDDType.ENTITY);
    root.addField(new Field("private", "entities", "java.util."));
    module.addContains(root);

    Class entity = new Class("Entity", "de.test.domain.Entity");
    entity.addField(new Field("private", "root", "de.test.domain.Root"));
    entity.setType(DDDType.ENTITY);
    module.addContains(entity);

    PackageAnalyseService service = new PackageAnalyseService(module, structure);
    service.setAggregateRoot();

    assertAll("Should find an aggregate root",
        () -> assertEquals(DDDType.ENTITY, entity.getType(), "Entity"),
        () -> assertEquals(DDDType.AGGREGATE_ROOT, root.getType(), "Aggregate Root"));
  }

  @Test
  void testSetNoAggregateRootByDependencies() {
    Package module = new Package("domain", "de.test.domain");

    Class root = new Class("Root", "de.test.domain.Root");
    root.setType(DDDType.ENTITY);
    root.addField(new Field("private", "entity", "de.test.domain.Entity"));
    module.addContains(root);

    Class entity = new Class("Entity", "de.test.domain.Entity");
    entity.addField(new Field("private", "root", "de.test.domain.Root"));
    entity.addField(new Field("private", "name", "java.lang.String"));
    entity.addField(new Field("private", "desc", "java.util.List"));
    entity.setType(DDDType.ENTITY);
    module.addContains(entity);

    PackageAnalyseService service = new PackageAnalyseService(module, structure);
    service.setAggregateRoot();

    assertAll("Should find an aggregate root",
        () -> assertEquals(DDDType.ENTITY, entity.getType(), "Entity"),
        () -> assertEquals(DDDType.ENTITY, root.getType(), "Entity"));
  }

  @Test
  void testSetAggregateRootByDependencies() {
    Package module = new Package("domain", "de.test.domain");

    Class root = new Class("RootEntity", "de.test.domain.RootEntity");
    root.setType(DDDType.ENTITY);
    root.addField(new Field("private", "entity", "de.test.domain.Entity"));
    module.addContains(root);

    Class entity = new Class("Entity", "de.test.domain.Entity");
    entity.addField(new Field("private", "roots", "java.util.List"));
    entity.setType(DDDType.ENTITY);
    module.addContains(entity);

    PackageAnalyseService service = new PackageAnalyseService(module, structure);
    service.setAggregateRoot();

    assertAll("Should find an aggregate root",
        () -> assertEquals(DDDType.AGGREGATE_ROOT, entity.getType(), "Aggregate Root"),
        () -> assertEquals(DDDType.ENTITY, root.getType(), "Entity"));
  }

}
