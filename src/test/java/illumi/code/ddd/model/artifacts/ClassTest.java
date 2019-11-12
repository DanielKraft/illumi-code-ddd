package illumi.code.ddd.model.artifacts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import illumi.code.ddd.model.DDDType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClassTest {
	private ServerControls embeddedDatabaseServer;
	
	@BeforeAll
	public void initializeNeo4j() {
		this.embeddedDatabaseServer = TestServerBuilders
	        .newInProcessBuilder()
	        .withFixture( "CREATE(c:Java:Class{fqn: 'de.test.Class', name: 'Class'})"
		        		// Field setup
		        			+ "CREATE(f1:Java:Field{name: 'name', signature: 'java.lang.String name', visibility: 'public'})"
		        			+ "CREATE(c)-[:DECLARES]->(f1)"
		        			+ "CREATE(f2:Java:Field{name: 'classId', signature: 'java.lang.Integer classId', visibility: 'private'})"
		        			+ "CREATE(c)-[:DECLARES]->(f2)"
		        			+ "CREATE(f3:Java:Field{})"
		        			+ "CREATE(c)-[:DECLARES]->(f3)"
		        		// Method setup
		        			+ "CREATE(m1:Java:Method{name: 'init', signature: 'void init()', visibility: 'private'})"
				        	+ "CREATE(c)-[:DECLARES]->(m1)"
				        	+ "CREATE(m2:Java:Method{name: 'exec', signature: 'void exec(java.lang.Integer)', visibility: 'public'})"
				        	+ "CREATE(c)-[:DECLARES]->(m2)"
		        			+ "CREATE(m3:Java:Method{})"
		        			+ "CREATE(c)-[:DECLARES]->(m3)"
		        		// Interface setup
		        			+ "CREATE(i:Java:Interface{fqn: 'de.test.Interface', name: 'Interface'})"
		        			+ "CREATE(c)-[:IMPLEMENTS]->(i)"
		        		// Superclass setup
		        			+ "CREATE(e:Java:Class{fqn: 'de.test.SuperClass', name: 'SuperClass'})"
		        			+ "CREATE(c)-[:EXTENDS]->(e)"
		        		// Annotation setup
							+ "CREATE(a:Java:Annotation{name: 'Anno'})"
							+ "CREATE(c)-[:ANNOTATED_BY]->(a)"
							+ "CREATE(t:Type{fqn: 'de.test.Anno'})"
							+ "CREATE(a)-[:OF_TYPE]->(t)"
	        ).newServer(); 
    }
	
	@Test
	public void testInitFactory() {
		Class artifact = new Class("ClassFactory", "de.test.ClassFactory");
		
		assertEquals(DDDType.FACTORY, artifact.getType());
	}
	
	@Test
	public void testInitRepository() {
		Class artifact = new Class("ClassRepository", "de.test.ClassRepository");
		
		assertEquals(DDDType.REPOSITORY, artifact.getType());
	}
	
	@Test
	public void testInitService() {
		Class artifact = new Class("ClassService", "de.test.ClassService");
		
		assertEquals(DDDType.SERVICE, artifact.getType());
	}
	
	@Test
	public void testInitApplicationService() {
		Class artifact = new Class("ClassApplicationService", "de.test.ClassApplicationService");
		
		assertEquals(DDDType.APPLICATION_SERVICE, artifact.getType());
	}
	
	@Test
	public void testInitController() {
		Class artifact = new Class("ClassController", "de.test.ClassController");
		
		assertEquals(DDDType.CONTROLLER, artifact.getType());
	}
	
	@Test
	public void testSetFields() {
		Class artifact = new Class("Class", "de.test.Class");
		try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
			artifact.setFields(driver);
			
			ArrayList<Field> result = (ArrayList<Field>) artifact.getFields();
			
			assertEquals(2, result.size());
	    	
	    	assertEquals("classId", result.get(0).getName());
	    	assertEquals("java.lang.Integer", result.get(0).getType());
	    	assertEquals("private", result.get(0).getVisibility());
	    	
	    	assertEquals("name", result.get(1).getName());
	    	assertEquals("java.lang.String", result.get(1).getType());
	    	assertEquals("public", result.get(1).getVisibility());
	    	
		}
	}
	
	@Test
	public void testSetFieldsFailed() {
		Class artifact = new Class("Class", "de.test.Class");
    	
		artifact.setFields(null);
    	
    	ArrayList<Field> result = (ArrayList<Field>) artifact.getFields();
    	
    	assertEquals(0, result.size());
	}

	@Test
	public void testSetMethods() {
		Class artifact = new Class("Class", "de.test.Class");
		try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
			artifact.setMethods(driver);
			
			ArrayList<Method> result = (ArrayList<Method>) artifact.getMethods();
			
			assertEquals(2, result.size());
	    	
	    	assertEquals("exec", result.get(0).getName());
	    	assertEquals("void exec(java.lang.Integer)", result.get(0).getSignature());
	    	assertEquals("public", result.get(0).getVisibility());
	    	
	    	assertEquals("init", result.get(1).getName());
	    	assertEquals("void init()", result.get(1).getSignature());
	    	assertEquals("private", result.get(1).getVisibility());
		}
	}
	
	@Test
	public void testSetMethodsFailed() {
		Class artifact = new Class("Class", "de.test.Class");
    	
		artifact.setMethods(null);
    	
		ArrayList<Method> result = (ArrayList<Method>) artifact.getMethods();
    	
    	assertEquals(0, result.size());
	}
	
	@Test
	public void testSetImplInterfaces() {
		Class artifact = new Class("Class", "de.test.Class");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
	    	ArrayList<Interface> interfaces = new ArrayList<>();
	    	interfaces.add(new Interface("OtherInterface", "de.other.OtherInterface"));
	    	interfaces.add(new Interface("Interface", "de.test.Interface"));
	    	
	    	artifact.setImplInterfaces(driver, interfaces);
	    	
			ArrayList<Interface> result = (ArrayList<Interface>) artifact.getInterfaces();
	    	
			assertEquals(1, result.size());
			
	    	assertEquals("Interface", result.get(0).getName());
	    	assertEquals("de.test.Interface", result.get(0).getPath());
	    }
	}
	
	@Test
	public void testSetNoImplInterfaces() {
		Class artifact = new Class("Class", "de.test.Class");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
	    	ArrayList<Interface> interfaces = new ArrayList<>();
	    	
	    	artifact.setImplInterfaces(driver, interfaces);
	    	
			ArrayList<Interface> result = (ArrayList<Interface>) artifact.getInterfaces();
	    		    	
	    	assertEquals(0, result.size());
	    }
	}
	
	@Test
	public void testSetImplInterfacesFailed() {
		Class artifact = new Class("Class", "de.test.Class");
		
		artifact.setImplInterfaces(null, null);
		
		ArrayList<Interface> result = (ArrayList<Interface>) artifact.getInterfaces();
		
		assertEquals(0, result.size());
	}
	
	@Test
	public void testSetSuperClass() {
		Class artifact = new Class("Class", "de.test.Class");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
	    	ArrayList<Class> classes = new ArrayList<>();
	    	classes.add(new Class("OtherClass", "de.test.OtherClass"));
	    	classes.add(new Class("SuperClass", "de.test.SuperClass"));
	    	
	    	artifact.setSuperClass(driver, classes);
	    	
	    	Class result = artifact.getSuperClass();
	    	
	    	assertEquals("SuperClass", result.getName());
	    	assertEquals("de.test.SuperClass", result.getPath());
	    }
	}
	
	@Test
	public void testSetNoSuperClass() {
		Class artifact = new Class("OtherClass", "de.test.OtherClass");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
	    	ArrayList<Class> classes = new ArrayList<>();
	    	
	    	artifact.setSuperClass(driver, classes);
	    	
	    	Class result = artifact.getSuperClass();
	    	
	    	assertEquals(null, result);
	    }
	}
	
	@Test
	public void testSetNoAvalibleSuperClass() {
		Class artifact = new Class("Class", "de.test.Class");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
	    	ArrayList<Class> classes = new ArrayList<>();
	    	
	    	artifact.setSuperClass(driver, classes);
	    	
	    	Class result = artifact.getSuperClass();
	    	
	    	assertEquals(null, result);
	    }
	}
	
	@Test
	public void testSetSuperClassFailed() {
		Class artifact = new Class("Class", "de.test.Class");
    	
		artifact.setSuperClass(null, null);
		
		Class result = artifact.getSuperClass();
    	
    	assertEquals(null, result);
	}
	
	@Test
	public void testSetAnnotations() {
		Class artifact = new Class("Class", "de.test.Class");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
	    	ArrayList<Annotation> annotations = new ArrayList<>();
	    	annotations.add(new Annotation("NoAnno", "de.other.NoAnno"));
	    	annotations.add(new Annotation("Anno", "de.test.Anno"));
	    	
	    	artifact.setAnnotations(driver, annotations);
	    	
	    	ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();
	    	
	    	assertEquals(1, result.size());
	    	
	    	assertEquals("Anno", result.get(0).getName());
	    	assertEquals("de.test.Anno", result.get(0).getPath());
	    	assertEquals(DDDType.INFRASTRUCTUR, result.get(0).getType());
	    }
	}
	
	@Test
	public void testSetNoAnnotations() {
		Class artifact = new Class("Class", "de.test.Class");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
	    	ArrayList<Annotation> annotations = new ArrayList<>();
	    	
	    	artifact.setAnnotations(driver, annotations);
	    	
	    	ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();
	    	
	    	assertEquals(0, result.size());
	    }
	}
	
	@Test
	public void testSetAnnotationsFailed() {
		Class artifact = new Class("Class", "de.test.Class");
    	
		artifact.setAnnotations(null, null);
		
		ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();
    	
    	assertEquals(0, result.size());
	}
}
