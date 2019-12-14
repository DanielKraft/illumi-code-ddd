package illumi.code.ddd.service.analyse.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClassAnalyseServiceTest {

  private DDDStructure structure;

  @BeforeEach
  void init() {
    structure = new DDDStructure();
    structure.setPath("de.test");
  }

  @Test
  void testSetTypeWithAlreadySet() {
    Class artifact = new Class("Value", "de.test.domain.Value");
    artifact.setType(DDDType.VALUE_OBJECT);
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
  }

  @Test
  void testSetTypeToValueObject() {
    Class artifact = new Class("Value", "de.test.domain.Value");
    artifact.addField(new Field("private", "name", "java.lang.String"));
    artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
    artifact.addField(new Field("private", "values", "other.List"));
    artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
    artifact.addMethod(new Method("public", "getName", "java.lang.String getName()"));
    artifact.addMethod(new Method("public", "equals", "java.lang.String equals()"));
    artifact.addMethod(new Method("public", "hashCode", "java.lang.String equals()"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
  }

  @Test
  void testSetTypeToValueObjectWithConstructors() {
    Class artifact = new Class("Value", "de.test.domain.Value");
    artifact.addField(new Field("private", "name", "java.lang.String"));
    artifact.addField(new Field("private", "values", "other.List"));
    artifact.addMethod(new Method("public", "<init>", "void <init>()"));
    artifact.addMethod(new Method("public", "<init>", "void <init>(java.lang.String)"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
  }

  @Test
  void testSetTypeToValueObjectWithId() {
    Class artifact = new Class("ValueId", "de.test.domain.ValueId");
    artifact.addField(new Field("private", "id", "java.lang.long"));
    artifact.addMethod(new Method("public", "getId", "java.lang.long getId()"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
  }

  @Test
  void testSetTypeToValueObjectWithGetterAndSetter() {
    Class artifact = new Class("Value", "de.test.domain.Value");
    artifact.addField(new Field("private", "name", "java.lang.String"));
    artifact.addField(new Field("private", "values", "other.List"));
    artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
    artifact.addMethod(new Method("public", "getName", "de.test.domain.Nachname getName()"));
    artifact.addMethod(new Method("public", "setName", "void setName(de.test.domain.Nachname)"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
  }

  @Test
  void testSetTypeToValueObjectWithUnconventionalGetter() {
    Class artifact = new Class("Value", "de.test.domain.Value");
    artifact.addField(new Field("private", "name", "java.lang.String"));
    artifact.addField(new Field("private", "values", "other.List"));
    artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
    artifact.addMethod(new Method("public", "nachname", "de.test.domain.Nachname nachname()"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
  }

  @Test
  void testSetTypeToValueObjectWithoutGetterAndSetter() {
    Class artifact = new Class("Value", "de.test.domain.Value");
    artifact.addField(new Field("private", "name", "java.lang.String"));
    artifact.addField(new Field("private", "nachname", "de.test.domain.Nachname"));
    artifact.addField(new Field("private", "values", "other.List"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
  }

  @Test
  void testSetTypeToEntity() {
    Class entity = new Class("Entity", "de.test.domain.Entity");
    structure.addClass(entity);

    Class artifact = new Class("Entities", "de.test.domain.Entities");
    artifact.addField(new Field("private", "id", "de.Id"));
    artifact.addField(new Field("private", "name", "java.lang.String"));
    artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
    artifact.addMethod(new Method("public", "setName", "void setName(java.lang.String)"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.ENTITY, artifact.getType());
  }

  @Test
  void testSetTypeToEntityWithId() {
    Class artifact = new Class("Entity", "de.test.domain.Entity");
    artifact.addField(new Field("private", "other", ".de.Other"));
    artifact.addField(new Field("private", "id", "de.test.domain.EntityId"));
    artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
    artifact.addMethod(new Method("public", "setName", "void setName(java.lang.String)"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.ENTITY, artifact.getType());
  }

  @Test
  void testSetTypeToEntityWithPluralEntityName() {
    Class artifact = new Class("Names", "de.test.domain.Names");
    artifact.addField(new Field("private", "nameRepository", "de.test.domain.NameRepository"));
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    artifact.addMethod(new Method("public", "getNameRepository",
        "de.test.domain.NameRepository getNameRepository()"));
    structure.addClass(artifact);

    Class entity = new Class("Name", "de.test.domain.Name");
    entity.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.ENTITY, artifact.getType());
  }

  @Test
  void testSetTypeToApplicationService() {
    Class artifact = new Class("Main", "de.test.domain.Main");
    artifact.addMethod(new Method("public", "main", "void main(java.lang.String[])"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.APPLICATION_SERVICE, artifact.getType());
  }

  @Test
  void testSetTypeToService() {
    Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
    artifact.addField(new Field("private", "nameRepository", "de.test.domain.NameRepository"));
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    structure.addClass(artifact);

    Class entity1 = new Class("Entity", "de.test.domain.Entity");
    entity1.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity1);

    Class entity2 = new Class("Name", "de.test.domain.Name");
    entity2.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity2);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.SERVICE, artifact.getType());
  }

  @Test
  void testSetTypeToServiceWithModelMethods() {
    Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
    artifact.addField(new Field("private", "nameRepository", "de.test.domain.NameRepository"));
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    artifact.addMethod(new Method("public", "getNameRepository",
        "de.test.domain.NameRepository getNameRepository()"));
    structure.addClass(artifact);

    Class entity = new Class("Name", "de.test.domain.Name");
    entity.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.SERVICE, artifact.getType());
  }

  @Test
  void testSetTypeToServiceWithoutFields() {
    Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    structure.addClass(artifact);

    Class entity1 = new Class("Entity", "de.test.domain.Entity");
    entity1.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity1);

    Class entity2 = new Class("Name", "de.test.domain.Name");
    entity2.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity2);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.SERVICE, artifact.getType());
  }

  @Test
  void testSetTypeToServiceWithConstant() {
    Class entity = new Class("Name", "de.test.domain.Name");
    entity.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity);

    Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
    artifact.addField(new Field("private", "CONST", "java.lang.String"));
    artifact.addField(new Field("private", "nameRepository", "de.test.domain.NameRepository"));
    artifact.addMethod(new Method("public", "toString", "java.lang.String toString()"));
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.SERVICE, artifact.getType());
  }

  @Test
  void testSetTypeToServiceWithFactoryType() {
    Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
    artifact.addField(new Field("private", "something", "de.test.domain.EntityFactory"));
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    structure.addClass(artifact);

    Class entity1 = new Class("Entity", "de.test.domain.Entity");
    entity1.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity1);

    Class entity2 = new Class("Name", "de.test.domain.Name");
    entity2.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity2);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.SERVICE, artifact.getType());
  }

  @Test
  void testSetTypeToServiceWithFactoryName() {
    Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
    artifact.addField(new Field("private", "factory", "de.test.domain.Something"));
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    structure.addClass(artifact);

    Class entity1 = new Class("Entity", "de.test.domain.Entity");
    entity1.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity1);

    Class entity2 = new Class("Name", "de.test.domain.Name");
    entity2.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity2);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.SERVICE, artifact.getType());
  }

  @Test
  void testSetTypeToServiceWithRepositoryType() {
    Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
    artifact.addField(new Field("private", "something", "de.test.domain.EntityRepository"));
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    structure.addClass(artifact);

    Class entity1 = new Class("Entity", "de.test.domain.Entity");
    entity1.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity1);

    Class entity2 = new Class("Name", "de.test.domain.Name");
    entity2.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity2);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.SERVICE, artifact.getType());
  }

  @Test
  void testSetTypeToServiceWithRepositoryName() {
    Class artifact = new Class("NameGenerator", "de.test.domain.NameGenerator");
    artifact.addField(new Field("private", "repository", "de.test.domain.Something"));
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    structure.addClass(artifact);

    Class entity1 = new Class("Entity", "de.test.domain.Entity");
    entity1.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity1);

    Class entity2 = new Class("Name", "de.test.domain.Name");
    entity2.addField(new Field("private", "name", "java.lang.String"));
    structure.addClass(entity2);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.SERVICE, artifact.getType());
  }

  @Test
  void testSetTypeToInfrastructure() {
    Class artifact = new Class("Infra", "de.test.domain.Infra");
    artifact.addField(new Field("private", "name", "java.lang.String"));
    artifact.addField(new Field("private", "values", "other.List"));
    artifact.addMethod(new Method("public", "generate", "void generate()"));
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
  }

  @Test
  @SuppressWarnings("CheckStyle")
  void testSetTypeToInfrastructureWithJPA() {
    Class artifact = new Class("InfraJPA", "de.test.domain.InfraJPA");
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
  }

  @Test
  @SuppressWarnings("CheckStyle")
  void testSetTypeToInfrastructureWithCRUD() {
    Class artifact = new Class("InfraCRUD", "de.test.domain.InfraCRUD");
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
  }

  @Test
  void testSetInfrastructureToInfrastructure() {
    Class infra = new Class("Infra", "de.test.domain.Infra");
    infra.setType(DDDType.INFRASTRUCTURE);
    infra.addDependencies("de.test.domain.Entity");
    structure.addClass(infra);

    ClassAnalyseService service = new ClassAnalyseService(infra, structure);
    service.setInfrastructure();

    assertEquals(DDDType.INFRASTRUCTURE, infra.getType());
  }

  @Test
  void testSetTypeToInfrastructureWithDependency() {
    Class infra = new Class("Infra", "de.test.domain.Infra");
    infra.setType(DDDType.INFRASTRUCTURE);
    infra.addDependencies("de.test.domain.Entity");
    structure.addClass(infra);

    Class artifact = new Class("Entity", "de.test.domain.Entity");
    artifact.setType(DDDType.ENTITY);
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setInfrastructure();

    assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
  }

  @Test
  void testSetTypeToInfrastructureWithSuperclass() {
    Class artifact = new Class("Entity", "de.test.domain.Entity");
    artifact.setType(DDDType.ENTITY);
    structure.addClass(artifact);

    Class infra = new Class("Infra", "de.test.domain.Infra");
    infra.setType(DDDType.INFRASTRUCTURE);
    infra.setSuperClass(artifact);
    structure.addClass(infra);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setInfrastructure();

    assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
  }

  @Test
  void testSetTypeNotToInfrastructure() {
    Class infra = new Class("ValueObject", "de.test.domain.ValueObject");
    infra.setType(DDDType.VALUE_OBJECT);
    infra.addDependencies("de.test.domain.Entity");
    structure.addClass(infra);

    Class artifact = new Class("Entity", "de.test.domain.Entity");
    artifact.setType(DDDType.ENTITY);
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setInfrastructure();

    assertEquals(DDDType.ENTITY, artifact.getType());
  }

  @Test
  void testSetTypeToInfrastructureEmpty() {
    Class artifact = new Class("Infra", "de.test.domain.Infra");
    structure.addClass(artifact);

    ClassAnalyseService service = new ClassAnalyseService(artifact, structure);
    service.setType();

    assertEquals(DDDType.INFRASTRUCTURE, artifact.getType());
  }

  @Test
  void testSetDomainEvent() {
    Class artifact = new Class("Event", "de.test.domain.Event");
    artifact.setType(DDDType.ENTITY);
    artifact.addField(new Field("private", "timestamp", "java.lang.long"));
    artifact.addField(new Field("private", "date", "java.lang.String"));
    artifact.addField(new Field("private", "stamp", "java.time.Time"));
    artifact.addField(new Field("private", "EntityId", "java.lang.long"));
    artifact.addField(new Field("private", "desc", "java.lang.String"));

    ClassAnalyseService service = new ClassAnalyseService(artifact);
    service.setDomainEvent();

    assertEquals(DDDType.DOMAIN_EVENT, artifact.getType());
  }

  @Test
  void testSetDomainEventWithId() {
    Class artifact = new Class("Entity", "de.test.domain.Entity");
    artifact.setType(DDDType.AGGREGATE_ROOT);
    artifact.addField(new Field("private", "id", "java.lang.long"));

    ClassAnalyseService service = new ClassAnalyseService(artifact);
    service.setDomainEvent();

    assertEquals(DDDType.AGGREGATE_ROOT, artifact.getType());
  }

  @Test
  void testSetDomainEventWithDate() {
    Class artifact = new Class("Event", "de.test.domain.Event");
    artifact.setType(DDDType.VALUE_OBJECT);
    artifact.addField(new Field("private", "birth", "java.time.Time"));

    ClassAnalyseService service = new ClassAnalyseService(artifact);
    service.setDomainEvent();

    assertEquals(DDDType.VALUE_OBJECT, artifact.getType());
  }

  @Test
  void testSetDomainEventOfOther() {
    Class artifact = new Class("Event", "de.test.domain.Event");
    artifact.setType(DDDType.FACTORY);

    ClassAnalyseService service = new ClassAnalyseService(artifact);
    service.setDomainEvent();

    assertEquals(DDDType.FACTORY, artifact.getType());
  }
}
