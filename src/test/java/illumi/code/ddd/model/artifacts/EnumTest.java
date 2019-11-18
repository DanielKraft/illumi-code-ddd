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
class EnumTest {
	private ServerControls embeddedDatabaseServer;
		
	@BeforeAll
	void initializeNeo4j() {
		this.embeddedDatabaseServer = TestServerBuilders
	        .newInProcessBuilder()
	        .withFixture( "CREATE(e:Java:Enum{fqn: 'de.test.Type', name: 'Type'})"
	        			+ "CREATE(f1:Java:Field{name: 'ONE', signature: 'de.test.Type ONE', visibility: 'public'})"
	        			+ "CREATE(e)-[:DECLARES]->(f1)"
	        			+ "CREATE(f2:Java:Field{name: 'TWO', signature: 'de.test.Type TWO', visibility: 'public'})"
	        			+ "CREATE(e)-[:DECLARES]->(f2)"
	        			+ "CREATE(f3:Java:Field{})"
	        			+ "CREATE(e)-[:DECLARES]->(f3)"
	        			+ "CREATE(a:Java:Annotation{name: 'Anno'})"
	        			+ "CREATE(e)-[:ANNOTATED_BY]->(a)"
	        			+ "CREATE(t:Type{fqn: 'de.test.Anno'})"
	        			+ "CREATE(a)-[:OF_TYPE]->(t)"
	        ).newServer(); 
    }
	
	@Test
	void testSetFields() {
		Enum artifact = new Enum("Type", "de.test.Type");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI())) {
	    	artifact.setFields(driver);
	    	
	    	ArrayList<Field> result = (ArrayList<Field>) artifact.getFields();
	    	
	    	assertEquals(2, result.size());
	    	
	    	assertEquals("TWO", result.get(0).getName());
	    	assertEquals("de.test.Type", result.get(0).getType());
	    	assertEquals("public", result.get(0).getVisibility());
	    	
	    	assertEquals("ONE", result.get(1).getName());
	    	assertEquals("de.test.Type", result.get(1).getType());
	    	assertEquals("public", result.get(1).getVisibility());
	    }
	}
	
	@Test
	void testSetFieldsFailed() {
		Enum artifact = new Enum("Type", "de.test.Type");
    	
		artifact.setFields(null);
    	
    	ArrayList<Field> result = (ArrayList<Field>) artifact.getFields();
    	
    	assertEquals(0, result.size());
	}

	@Test
	void testSetAnnotations() {
		Enum artifact = new Enum("Type", "de.test.Type");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI())) {
	    	ArrayList<Annotation> annotations = new ArrayList<>();
	    	annotations.add(new Annotation("NoAnno", "de.other.NoAnno"));
	    	annotations.add(new Annotation("Anno", "de.test.Anno"));
	    	
	    	artifact.setAnnotations(driver, annotations);
	    	
	    	ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();
	    	
	    	assertEquals(1, result.size());
	    	
	    	assertEquals("Anno", result.get(0).getName());
	    	assertEquals("de.test.Anno", result.get(0).getPath());
	    	assertEquals(DDDType.INFRASTRUCTURE, result.get(0).getType());
	    }
	}
	
	@Test
	void testSetNoAnnotations() {
		Enum artifact = new Enum("Type", "de.test.Type");
	    try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI())) {
	    	ArrayList<Annotation> annotations = new ArrayList<>();
	    	
	    	artifact.setAnnotations(driver, annotations);
	    	
	    	ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();
	    	
	    	assertEquals(0, result.size());
	    }
	}
	
	@Test
	void testSetNoAnnotationsFailed() {
		Enum artifact = new Enum("Type", "de.test.Type");
    	
		artifact.setAnnotations(null, null);
		
		ArrayList<Annotation> result = (ArrayList<Annotation>) artifact.getAnnotations();
    	
    	assertEquals(0, result.size());
	}
}
