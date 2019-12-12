package illumi.code.ddd.model.artifacts;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDType;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterfaceTest {
  private ServerControls embeddedDatabaseServer;

  @BeforeAll
  @SuppressWarnings("CheckStyle")
  void initializeNeo4j() {
    this.embeddedDatabaseServer = TestServerBuilders
        .newInProcessBuilder()
        .withFixture("CREATE(i:Java:Interface{fqn: 'de.test.Interface', name: 'Interface'})"
            // Field setup
            + "CREATE(f1:Java:Field{name: 'name', signature: 'java.lang.String name', visibility: 'public'})"
            + "CREATE(i)-[:DECLARES]->(f1)"
            + "CREATE(f2:Java:Field{name: 'interfaceId', signature: 'java.lang.Integer interfaceId', visibility: 'public'})"
            + "CREATE(i)-[:DECLARES]->(f2)"
            + "CREATE(f3:Java:Field{})"
            + "CREATE(i)-[:DECLARES]->(f3)"
            // Method setup
            + "CREATE(m1:Java:Method{name: 'init', signature: 'void init()', visibility: 'public'})"
            + "CREATE(i)-[:DECLARES]->(m1)"
            + "CREATE(m2:Java:Method{name: 'exec', signature: 'void exec(java.lang.Integer)', visibility: 'public'})"
            + "CREATE(i)-[:DECLARES]->(m2)"
            + "CREATE(m3:Java:Method{})"
            + "CREATE(i)-[:DECLARES]->(m3)"
            // Interface setup
            + "CREATE(impl:Java:Interface{fqn: 'de.test.ImplInterface', name: 'ImplInterface'})"
            + "CREATE(i)-[:IMPLEMENTS]->(impl)"
            // Annotation setup
            + "CREATE(a:Java:Annotation{name: 'Anno'})"
            + "CREATE(i)-[:ANNOTATED_BY]->(a)"
            + "CREATE(t:Type{fqn: 'de.test.Anno'})"
            + "CREATE(a)-[:OF_TYPE]->(t)"
        ).newServer();
  }

  @Test
  void testInitFactory() {
    Interface artifact = new Interface("InterfaceFactory", "de.test.InterfaceFactory");

    assertEquals(DDDType.FACTORY, artifact.getType());
  }

  @Test
  void testInitRepository() {
    Interface artifact = new Interface("InterfaceRepository", "de.test.InterfaceRepository");

    assertEquals(DDDType.REPOSITORY, artifact.getType());
  }

  @Test
  void testInitService() {
    Interface artifact = new Interface("InterfaceService", "de.test.InterfaceService");

    assertEquals(DDDType.SERVICE, artifact.getType());
  }

  @Test
  void testSetFields() {
    Interface artifact = new Interface("Interface", "de.test.Interface");
    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI())) {
      artifact.setFields(driver);

      ArrayList<Field> result = (ArrayList<Field>) artifact.getFields();

      assertAll(
          () -> assertEquals(2, result.size()),

          () -> assertEquals("name", result.get(0).getName()),
          () -> assertEquals("java.lang.String", result.get(0).getType()),
          () -> assertEquals("public", result.get(0).getVisibility()),

          () -> assertEquals("interfaceId", result.get(1).getName()),
          () -> assertEquals("java.lang.Integer", result.get(1).getType()),
          () -> assertEquals("public", result.get(1).getVisibility()));

    }
  }

  @Test
  void testSetFieldsFailed() {
    Interface artifact = new Interface("Interface", "de.test.Interface");

    artifact.setFields(null);

    ArrayList<Field> result = (ArrayList<Field>) artifact.getFields();

    assertEquals(0, result.size());
  }

  @Test
  void testSetMethods() {
    Interface artifact = new Interface("Interface", "de.test.Interface");
    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI())) {
      artifact.setMethods(driver);

      ArrayList<Method> result = (ArrayList<Method>) artifact.getMethods();

      assertAll(
          () -> assertEquals(2, result.size()),

          () -> assertEquals("init", result.get(0).getName()),
          () -> assertEquals("void init()", result.get(0).getSignature()),
          () -> assertEquals("public", result.get(0).getVisibility()),

          () -> assertEquals("exec", result.get(1).getName()),
          () -> assertEquals("void exec(java.lang.Integer)", result.get(1).getSignature()),
          () -> assertEquals("public", result.get(1).getVisibility()));
    }
  }

  @Test
  void testSetMethodsFailed() {
    Interface artifact = new Interface("Interface", "de.test.Interface");

    artifact.setMethods(null);

    ArrayList<Method> result = (ArrayList<Method>) artifact.getMethods();

    assertEquals(0, result.size());
  }

  @Test
  void testSetImplInterfaces() {
    Interface artifact = new Interface("Interface", "de.test.Interface");
    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI())) {
      ArrayList<Interface> interfaces = new ArrayList<>();
      interfaces.add(new Interface("OtherInterface", "de.other.OtherInterface"));
      interfaces.add(new Interface("ImplInterface", "de.test.ImplInterface"));

      artifact.setImplInterfaces(driver, interfaces);

      ArrayList<Interface> result = (ArrayList<Interface>) artifact.getImplInterfaces();

      assertAll(
          () -> assertEquals(1, result.size()),

          () -> assertEquals("ImplInterface", result.get(0).getName()),
          () -> assertEquals("de.test.ImplInterface", result.get(0).getPath()));
    }
  }

  @Test
  void testSetNoImplInterfaces() {
    Interface artifact = new Interface("Interface", "de.test.Interface");
    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI())) {
      ArrayList<Interface> interfaces = new ArrayList<>();

      artifact.setImplInterfaces(driver, interfaces);

      ArrayList<Interface> result = (ArrayList<Interface>) artifact.getImplInterfaces();

      assertEquals(0, result.size());
    }
  }

  @Test
  void testSetImplInterfacesFailed() {
    Interface artifact = new Interface("Interface", "de.test.Interface");

    artifact.setImplInterfaces(null, null);

    ArrayList<Interface> result = (ArrayList<Interface>) artifact.getImplInterfaces();

    assertEquals(0, result.size());
  }

  @Test
  void testSetAnnotations() {
    Interface artifact = new Interface("Interface", "de.test.Interface");
    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI())) {
      ArrayList<Annotation> annotations = new ArrayList<>();
      annotations.add(new Annotation("NoAnno", "de.other.NoAnno"));
      annotations.add(new Annotation("Anno", "de.test.Anno"));

      artifact.setAnnotations(driver, annotations);

      ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();

      assertAll(
          () -> assertEquals(1, result.size()),

          () -> assertEquals("Anno", result.get(0).getName()),
          () -> assertEquals("de.test.Anno", result.get(0).getPath()),
          () -> assertEquals(DDDType.INFRASTRUCTURE, result.get(0).getType()));
    }
  }

  @Test
  void testSetNoAnnotations() {
    Interface artifact = new Interface("Interface", "de.test.Interface");
    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI())) {
      ArrayList<Annotation> annotations = new ArrayList<>();

      artifact.setAnnotations(driver, annotations);

      ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();

      assertEquals(0, result.size());
    }
  }

  @Test
  void testSetAnnotationsFailed() {
    Interface artifact = new Interface("Interface", "de.test.Interface");

    artifact.setAnnotations(null, null);

    ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();

    assertEquals(0, result.size());
  }
}
