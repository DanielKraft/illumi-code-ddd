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
public class AnnotationCreationTest {
	
	private ServerControls embeddedDatabaseServer;
	
	@BeforeAll
	public void initializeNeo4j() {
		this.embeddedDatabaseServer = TestServerBuilders
	        .newInProcessBuilder()
	        .withFixture( "CREATE(a:Java:Annotation{fqn: 'de.test.Annotation', name: 'Annotation'})"
		        		// Field setup
		        			+ "CREATE(f1:Java:Field{name: 'name', signature: 'java.lang.String name', visibility: 'public'})"
		        			+ "CREATE(a)-[:DECLARES]->(f1)"
		        			+ "CREATE(f2:Java:Field{name: 'annotationId', signature: 'java.lang.Integer annotationId', visibility: 'private'})"
		        			+ "CREATE(a)-[:DECLARES]->(f2)"
		        			+ "CREATE(f3:Java:Field{})"
		        			+ "CREATE(a)-[:DECLARES]->(f3)"
		        		// Method setup
		        			+ "CREATE(m1:Java:Method{name: 'init', signature: 'void init()', visibility: 'private'})"
				        	+ "CREATE(a)-[:DECLARES]->(m1)"
				        	+ "CREATE(m2:Java:Method{name: 'exec', signature: 'void exec(java.lang.Integer)', visibility: 'public'})"
				        	+ "CREATE(a)-[:DECLARES]->(m2)"
		        			+ "CREATE(m3:Java:Method{})"
		        			+ "CREATE(a)-[:DECLARES]->(m3)"
		        		// Annotation setup
							+ "CREATE(anno:Java:Annotation{name: 'Anno'})"
							+ "CREATE(a)-[:ANNOTATED_BY]->(anno)"
							+ "CREATE(t:Type{fqn: 'de.test.Anno'})"
							+ "CREATE(anno)-[:OF_TYPE]->(t)"
	        ).newServer(); 
    }
	
	@Test
	public void testSetFields() {
		Annotation artifact = new Annotation("Annotation", "de.test.Annotation");
		try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
			artifact.setFields(driver);
			
			ArrayList<Field> result = (ArrayList<Field>) artifact.getFields();
			
			assertEquals(2, result.size());
	    	
	    	assertEquals("annotationId", result.get(0).getName());
	    	assertEquals("java.lang.Integer", result.get(0).getType());
	    	assertEquals("private", result.get(0).getVisibility());
	    	
	    	assertEquals("name", result.get(1).getName());
	    	assertEquals("java.lang.String", result.get(1).getType());
	    	assertEquals("public", result.get(1).getVisibility());
	    	
		}
	}
	
	@Test
	public void testSetFieldsFailed() {
		Annotation artifact = new Annotation("Annotation", "de.test.Annotation");
    	
		artifact.setFields(null);
    	
    	ArrayList<Field> result = (ArrayList<Field>) artifact.getFields();
    	
    	assertEquals(0, result.size());
	}
	
	@Test
	public void testSetMethods() {
		Annotation artifact = new Annotation("Annotation", "de.test.Annotation");
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
		Annotation artifact = new Annotation("Annotation", "de.test.Annotation");
    	
		artifact.setMethods(null);
    	
		ArrayList<Method> result = (ArrayList<Method>) artifact.getMethods();
    	
    	assertEquals(0, result.size());
	}
	
	@Test
	public void testSetAnnotations() {
		Annotation artifact = new Annotation("Annotation", "de.test.Annotation");
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
		Annotation artifact = new Annotation("Annotation", "de.test.Annotation");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
	    	ArrayList<Annotation> annotations = new ArrayList<>();
	    	
	    	artifact.setAnnotations(driver, annotations);
	    	
	    	ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();
	    	
	    	assertEquals(0, result.size());
	    }
	}
	
	@Test
	public void testSetAnnotationsFailed() {
		Annotation artifact = new Annotation("Annotation", "de.test.Annotation");
    	
		artifact.setAnnotations(null, null);
		
		ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();
    	
    	assertEquals(0, result.size());
	}
}
