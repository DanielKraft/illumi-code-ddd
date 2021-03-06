package illumi.code.ddd.service.fitness.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Method;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.fitness.DDDRating;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassFitnessServiceTest {

  private DDDStructure structure;

  @BeforeEach
  void init() {
    structure = new DDDStructure();
    structure.setPath("de.test");
  }

  @Test
  void testEvaluateEntity() {
    Class superClass = new Class("Super", "de.test.Super");
    superClass.addField(new Field("private", "id", "de.test.EntityId"));
    superClass.addField(new Field("private", "name", "string"));
    superClass.addMethod(new Method("public", "equals", "boolean equals()"));
    superClass.addMethod(new Method("public", "hashCode", "int hashCode()"));
    superClass.addMethod(new Method("public", "toString", "test toString()"));

    Class artifact = new Class("Entity", "de.test.domain.model.Entity");
    artifact.setType(DDDType.ENTITY);
    artifact.addField(new Field("private", "id", "de.test.EntityId"));
    artifact.addField(new Field("private", "names", "java.util.List"));
    artifact.addField(new Field("private", "desc", "java.lang.String"));
    artifact.addMethod(new Method("public", "getNames", "java.util.List getNames()"));
    artifact.addMethod(new Method("public", "setNames", "void setNames(java.util.List)"));
    artifact.addMethod(new Method("public", "getDesc", "java.lang.String getDesc()"));
    artifact.addMethod(new Method("public", "setDesc", "void setDesc(java.lang.String)"));
    artifact.addMethod(new Method("public", "hashCode", "int hashCode()"));
    artifact.addMethod(new Method("public", "equals", "boolean equals()"));
    artifact.addMethod(new Method("public", "hashCode", "int hashCode()"));
    artifact.addMethod(new Method("public", "toString", "test toString()"));
    artifact.addSuperClass(superClass);

    ClassFitnessService service = new ClassFitnessService(artifact, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(84.38, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.C, result.getScore(), "Rating"),
        () -> assertEquals(32, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(27, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(3, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateEmptyEntity() {
    Class superSuperClass = new Class("SuperSuper", "de.test.SuperSuper");
    superSuperClass.addField(new Field("private", "name", "string"));

    Class superClass = new Class("Super", "de.test.Super");
    superClass.addSuperClass(superSuperClass);

    Class artifact = new Class("Entity", "de.test.Entity");
    artifact.setType(DDDType.ENTITY);
    artifact.addSuperClass(superClass);

    ClassFitnessService service = new ClassFitnessService(artifact, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(9, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(3, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidEntity() {
    Class artifact = new Class("Entity", "de.test.Entity");
    artifact.setType(DDDType.ENTITY);

    ClassFitnessService service = new ClassFitnessService(artifact, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(9, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(3, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateValueObjects() {
    Class artifact = new Class("ValueObject", "de.test.ValueObject");
    artifact.setType(DDDType.VALUE_OBJECT);
    artifact.addField(new Field("private", "name", "java.lang.string"));
    artifact.addField(new Field("private", "types", "java.util.Set"));
    artifact.addField(new Field("private", "value", "de.test.Value"));
    artifact.addMethod(new Method("public", "getName", "java.lang.string getName()"));
    artifact.addMethod(new Method("public", "name", "java.lang.string name()"));
    artifact.addMethod(new Method("public", "setName", "de.test.ValueObject setName()"));
    artifact.addMethod(new Method("private", "setValue", "void setValue(de.test.Value)"));
    artifact.addMethod(new Method("public", "setValue", "void setValue(de.test.Value)"));
    artifact.addMethod(new Method("public", "toString", "test toString()"));

    ClassFitnessService service = new ClassFitnessService(artifact, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(65.91, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.D, result.getScore(), "Rating"),
        () -> assertEquals(44, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(29, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(8, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateValueObjectsWithId() {
    Class artifact = new Class("ValueObjectId", "de.test.ValueObjectId");
    artifact.setType(DDDType.VALUE_OBJECT);
    artifact.addField(new Field("private", "id", "long"));

    ClassFitnessService service = new ClassFitnessService(artifact, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(52.63, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.D, result.getScore(), "Rating"),
        () -> assertEquals(19, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(10, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(5, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidValueObjects() {
    Class artifact = new Class("ValueObject", "de.test.ValueObject");
    artifact.setType(DDDType.VALUE_OBJECT);
    artifact.addField(new Field("private", "id", "long"));

    ClassFitnessService service = new ClassFitnessService(artifact, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(19, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(6, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateAggregateRoot() {
    Package infra = new Package("infrastructure", "de.test.infrastructure");
    infra.setType(DDDType.MODULE);
    structure.addPackage(infra);

    Package domain = new Package("aggregate", "de.test.aggregate");
    domain.setType(DDDType.MODULE);
    structure.addPackage(domain);

    Class aggregate = new Class("Aggregate", "de.test.aggregate.Aggregate");
    aggregate.setType(DDDType.AGGREGATE_ROOT);
    aggregate.setDomain("aggregate");
    domain.addContains(aggregate);
    structure.addClass(aggregate);

    Class repository = new Class("AggregateRepository", "de.test.aggregate.AggregateRepository");
    repository.setType(DDDType.REPOSITORY);
    repository.setDomain("aggregate");
    domain.addContains(repository);
    structure.addClass(repository);

    Class factory = new Class("AggregateFactory", "de.test.aggregate.AggregateFactory");
    factory.setType(DDDType.FACTORY);
    factory.setDomain("aggregate");
    domain.addContains(factory);
    structure.addClass(factory);

    Class serviceClass = new Class("AggregateService", "de.test.aggregate.AggregateService");
    serviceClass.setType(DDDType.SERVICE);
    serviceClass.setDomain("aggregate");
    domain.addContains(serviceClass);
    structure.addClass(serviceClass);

    ClassFitnessService service = new ClassFitnessService(aggregate, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(50.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.D, result.getScore(), "Rating"),
        () -> assertEquals(18, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(9, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(3, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidAggregateRoot() {
    Package domain = new Package("aggregate", "de.test.aggregate");
    domain.setType(DDDType.MODULE);
    structure.addPackage(domain);

    Class aggregate = new Class("Aggregate", "de.test.aggregate.Aggregate");
    aggregate.setType(DDDType.AGGREGATE_ROOT);
    aggregate.setDomain("aggregate");
    domain.addContains(aggregate);
    structure.addClass(aggregate);

    Class repository = new Class("Repository", "de.test.aggregate.Repository");
    repository.setType(DDDType.REPOSITORY);
    repository.setDomain("aggregate");
    domain.addContains(repository);
    structure.addClass(repository);

    Class factory = new Class("Factory", "de.test.aggregate.Factory");
    factory.setType(DDDType.FACTORY);
    factory.setDomain("aggregate");
    domain.addContains(factory);
    structure.addClass(factory);

    Class serviceClass = new Class("Service", "de.test.aggregate.Service");
    serviceClass.setType(DDDType.SERVICE);
    serviceClass.setDomain("aggregate");
    domain.addContains(serviceClass);
    structure.addClass(serviceClass);

    ClassFitnessService service = new ClassFitnessService(aggregate, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(18, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(6, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateEmptyAggregateRoot() {
    Class aggregate = new Class("Aggregate", "de.test.aggregate.Aggregate");
    aggregate.setType(DDDType.AGGREGATE_ROOT);
    aggregate.setDomain("aggregate");

    ClassFitnessService service = new ClassFitnessService(aggregate, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(18, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(6, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateRepository() {
    Class repository = new Class("TestRepositoryImpl", "de.test.domain.TestRepositoryImpl");
    repository.setType(DDDType.REPOSITORY);
    repository.setDomain("domain");
    repository.addMethod(new Method("public", "nextIdentity", "test nextIdentity()"));
    repository.addMethod(new Method("public", "findById", "test findById()"));
    repository.addMethod(new Method("public", "getId", "test getId()"));
    repository.addMethod(new Method("public", "save", "test save()"));
    repository.addMethod(new Method("public", "add", "test add()"));
    repository.addMethod(new Method("public", "insert", "test insert()"));
    repository.addMethod(new Method("public", "put", "test put()"));
    repository.addMethod(new Method("public", "delete", "test delete()"));
    repository.addMethod(new Method("public", "remove", "test remove()"));
    repository.addMethod(new Method("public", "contains", "test contains()"));
    repository.addMethod(new Method("public", "exists", "test exists()"));
    repository.addMethod(new Method("public", "update", "test update()"));
    repository.addMethod(new Method("public", "toString", "test toString()"));

    Interface serviceInterface = new Interface("Service", "de.test.domain.Service");
    serviceInterface.setType(DDDType.SERVICE);
    serviceInterface.setDomain("domain");
    repository.addImplInterface(serviceInterface);

    Interface repo = new Interface("TestRepository", "de.test.domain.TestRepository");
    repo.setType(DDDType.REPOSITORY);
    repo.setDomain("domain");
    repository.addImplInterface(repo);

    ClassFitnessService service = new ClassFitnessService(repository, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(93.75, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.B, result.getScore(), "Rating"),
        () -> assertEquals(16, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(15, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(1, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidRepository() {
    Class repository = new Class("TestRepository", "de.test.domain.TestRepository");
    repository.setType(DDDType.REPOSITORY);
    repository.setDomain("domain");

    ClassFitnessService service = new ClassFitnessService(repository, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(16, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(9, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateFactory() {
    Class factoryImpl = new Class("TestFactoryImpl", "de.test.TestFactoryImpl");
    factoryImpl.setType(DDDType.FACTORY);
    factoryImpl.addField(new Field("private", "repository", "de.test.Repository"));
    factoryImpl.addField(new Field("private", "name", "string"));
    factoryImpl.addMethod(new Method("public", "create", "test create()"));
    factoryImpl.addMethod(new Method("public", "toString", "test toString()"));

    Interface serviceInterface = new Interface("Service", "de.test.domain.Service");
    serviceInterface.setType(DDDType.SERVICE);
    serviceInterface.setDomain("domain");
    factoryImpl.addImplInterface(serviceInterface);

    Interface factory = new Interface("TestFactory", "de.test.domain.TestFactory");
    factory.setType(DDDType.REPOSITORY);
    factory.setDomain("domain");
    factoryImpl.addImplInterface(factory);

    ClassFitnessService service = new ClassFitnessService(factoryImpl, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(87.5, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.C, result.getScore(), "Rating"),
        () -> assertEquals(8, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(7, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(1, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidFactory() {
    Class factoryImpl = new Class("Fac", "de.test.Fac");
    factoryImpl.setType(DDDType.FACTORY);
    factoryImpl.addField(new Field("private", "name", "string"));
    factoryImpl.addMethod(new Method("public", "toString", "test toString()"));

    ClassFitnessService service = new ClassFitnessService(factoryImpl, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(8, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(5, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateService() {
    Class serviceImpl = new Class("TestImpl", "de.test.application.domain.TestImpl");
    serviceImpl.setType(DDDType.SERVICE);
    serviceImpl.setDomain("domain");

    Interface serviceInterface = new Interface("Test", "de.test.domain.Test");
    serviceInterface.setType(DDDType.SERVICE);
    serviceInterface.setDomain("domain");
    serviceImpl.addImplInterface(serviceInterface);

    Interface repo = new Interface("Service", "de.test.domain.Service");
    repo.setType(DDDType.SERVICE);
    repo.setDomain("domain");
    serviceImpl.addImplInterface(repo);

    ClassFitnessService service = new ClassFitnessService(serviceImpl, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(100.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.A, result.getScore(), "Rating"),
        () -> assertEquals(1, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(1, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(0, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidService() {
    Class serviceImpl = new Class("Test", "de.test.Test");
    serviceImpl.setType(DDDType.SERVICE);

    ClassFitnessService service = new ClassFitnessService(serviceImpl, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(1, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(1, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateApplicationServiceWithInvalidName() {
    Class app = new Class("TestApplication", "de.test.application.TestApplication");
    app.setType(DDDType.APPLICATION_SERVICE);

    ClassFitnessService service = new ClassFitnessService(app, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(100.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.A, result.getScore(), "Rating"),
        () -> assertEquals(1, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(1, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(0, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidApplicationService() {
    Class app = new Class("Test", "de.test.Test");
    app.setType(DDDType.APPLICATION_SERVICE);

    ClassFitnessService service = new ClassFitnessService(app, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(1, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(2, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInfrastructure() {
    Class app = new Class("Test", "de.test.infrastructure.Test");
    app.setType(DDDType.INFRASTRUCTURE);

    ClassFitnessService service = new ClassFitnessService(app, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(100.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.A, result.getScore(), "Rating"),
        () -> assertEquals(1, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(1, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(0, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidInfrastructure() {
    Class app = new Class("Test", "de.test.Test");
    app.setType(DDDType.INFRASTRUCTURE);

    ClassFitnessService service = new ClassFitnessService(app, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(1, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(1, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateDomainEvent() {
    Class event = new Class("Event", "de.test.domain.Event");
    event.setType(DDDType.DOMAIN_EVENT);
    event.addField(new Field("private", "name", "java.lang.String"));
    event.addField(new Field("private", "id", "de.test.Id"));
    event.addField(new Field("private", "time", "java.lang.long"));
    event.addField(new Field("private", "date", "java.lang.long"));
    event.addField(new Field("private", "test1", "java.time."));
    event.addField(new Field("private", "test2", "de.test.Value"));
    event.addMethod(new Method("public", "getTime", "java.lang.long getTime()"));
    event.addMethod(new Method("public", "setDate", "java.lang.long setDate()"));
    event.addMethod(new Method("public", "toString", "test toString()"));

    ClassFitnessService service = new ClassFitnessService(event, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(43.75, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.E, result.getScore(), "Rating"),
        () -> assertEquals(16, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(7, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(5, result.getIssues().size(), "#Issues"));
  }

  @Test
  void testEvaluateInvalidDomainEvent() {
    Class event = new Class("Event", "de.test.domain.Event");
    event.setType(DDDType.DOMAIN_EVENT);

    ClassFitnessService service = new ClassFitnessService(event, structure);
    final DDDFitness result = service.evaluate();

    assertAll(() -> assertEquals(0.0, result.calculateFitness(), "Fitness"),
        () -> assertEquals(DDDRating.F, result.getScore(), "Rating"),
        () -> assertEquals(7, result.getNumberOfCriteria(), "Total Criteria"),
        () -> assertEquals(0, result.getNumberOfFulfilledCriteria(), "Fulfilled Criteria"),
        () -> assertEquals(3, result.getIssues().size(), "#Issues"));
  }
}
