package illumi.code.ddd.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import illumi.code.ddd.model.DDDType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AnalyseServiceImplTest {
	private ServerControls embeddedDatabaseServer;
	
	@BeforeAll
	public void initializeNeo4j() {
		this.embeddedDatabaseServer = TestServerBuilders
	        .newInProcessBuilder()
	        .withFixture( "CREATE(root:Java:Package{fqn: 'de.test', name: 'test'})"
	        		
		        			+ "CREATE(domain:Java:Package{fqn: 'de.test.domain', name: 'domain'})"
		        			+ "CREATE(root)-[:CONTAINS]->(domain)"
			        			+ "CREATE(person:Java:Package{fqn: 'de.test.domain.person', name: 'person'})"
			        			+ "CREATE(domain)-[:CONTAINS]->(person)"
									
									+ "CREATE(ar:Java:Class{fqn: 'de.test.domain.person.Person', name: 'Person'})"
									+ "CREATE(person)-[:CONTAINS]->(ar)"
										+ "CREATE(pId:Java:Field{name: 'personId', signature: 'de.test.domain.person.PersonId personId', visibility: 'private'})"
										+ "CREATE(ar)-[:DECLARES]->(pId)"
										
									+ "CREATE(customer:Java:Class{fqn: 'de.test.domain.person.Customer', name: 'Customer'})"
									+ "CREATE(person)-[:CONTAINS]->(customer)"
										+ "CREATE(name:Java:Field{name: 'name', signature: 'java.lang.String name', visibility: 'private'})"
										+ "CREATE(customer)-[:DECLARES]->(name)"
										+ "CREATE(type:Java:Field{name: 'type', signature: 'de.test.domain.person.CustomerType type', visibility: 'private'})"
										+ "CREATE(customer)-[:DECLARES]->(type)"	
									+ "CREATE(customer)-[:EXTENDS]->(ar)"								

									+ "CREATE(value:Java:Class{fqn: 'de.test.domain.person.PersonId', name: 'PersonId'})"
									+ "CREATE(person)-[:CONTAINS]->(value)"
										+ "CREATE(id:Java:Field{name: 'id', signature: 'java.lang.Integer id', visibility: 'private'})"
										+ "CREATE(value)-[:DECLARES]->(id)"
					        			+ "CREATE(toString:Java:Method{name: 'toString', signature: 'java.lang.String toString()', visibility: 'public'})"
							        	+ "CREATE(value)-[:DECLARES]->(toString)"
							        	+ "CREATE(get:Java:Method{name: 'getPersonId', signature: 'java.lang.Integer getPersonId()', visibility: 'public'})"
							        	+ "CREATE(value)-[:DECLARES]->(get)"
					        			+ "CREATE(set:Java:Method{name: 'setPersonId', signature: 'void setPersonId(java.lang.Integer)', visibility: 'public'})"
							        	+ "CREATE(value)-[:DECLARES]->(set)"
										
				        			
					        		+ "CREATE(entity:Java:Class{fqn: 'de.test.domain.person.Entity', name: 'Entity'})"
									+ "CREATE(person)-[:CONTAINS]->(entity)"
			        					+ "CREATE(f1:Java:Field{name: 'other', signature: 'org.other.Other other', visibility: 'private'})"
					        			+ "CREATE(entity)-[:DECLARES]->(f1)"
			        					+ "CREATE(f2:Java:Field{name: 'value', signature: 'org.other.Value other', visibility: 'private'})"
					        			+ "CREATE(entity)-[:DECLARES]->(f2)"
					        			+ "CREATE(m1:Java:Method{name: 'getOther', signature: 'org.other.Other getOther()', visibility: 'public'})"
							        	+ "CREATE(entity)-[:DECLARES]->(m1)"
							        	+ "CREATE(m2:Java:Method{name: 'getValue', signature: 'org.other.Value getValue()', visibility: 'public'})"
							        	+ "CREATE(entity)-[:DECLARES]->(m2)"
									
									+ "CREATE(factory:Java:Interface{fqn: 'de.test.domain.person.CustomerFactory', name: 'CustomerFactory'})"
									+ "CREATE(person)-[:CONTAINS]->(factory)"
									
									+ "CREATE(factoryImpl:Java:Class{fqn: 'de.test.domain.person.CustomerFactoryImpl', name: 'CustomerFactoryImpl'})"
									+ "CREATE(person)-[:CONTAINS]->(factoryImpl)"
				        			+ "CREATE(factoryImpl)-[:IMPLEMENTS]->(factory)"
				        			
				        			+ "CREATE(repo:Java:Interface{fqn: 'de.test.domain.person.CustomerRepository', name: 'CustomerRepository'})"
				        			+ "CREATE(person)-[:CONTAINS]->(repo)"
				        			
				        			+ "CREATE(repoImpl:Java:Class{fqn: 'de.test.domain.person.CustomerRepositoryImpl', name: 'CustomerRepositoryImpl'})"
				        			+ "CREATE(person)-[:CONTAINS]->(repoImpl)"
				        			+ "CREATE(repoImpl)-[:IMPLEMENTS]->(repo)"
				        			
									+ "CREATE(infService:Java:Class{fqn: 'de.test.domain.person.CacheWorker', name: 'CacheWorker'})"
									+ "CREATE(person)-[:CONTAINS]->(infService)"
									
									+ "CREATE(service:Java:Class{fqn: 'de.test.domain.person.CustomerFormator', name: 'CustomerFormator'})"
									+ "CREATE(person)-[:CONTAINS]->(service)"
										+ "CREATE(const:Java:Field{name: 'CONST', signature: 'java.lang.String CONST', visibility: 'private'})"
					        			+ "CREATE(service)-[:DECLARES]->(const)"
									
									+ "CREATE(serviceInter:Java:Interface{fqn: 'de.test.domain.person.CustomerValidator', name: 'CustomerValidator'})"
									+ "CREATE(person)-[:CONTAINS]->(serviceInter)"
									
									+ "CREATE(serviceImpl:Java:Class{fqn: 'de.test.domain.person.CustomerValidatorImpl', name: 'CustomerValidatorImpl'})"
									+ "CREATE(person)-[:CONTAINS]->(serviceImpl)"
				        				+ "CREATE(f_repo:Java:Field{name: 'repo', signature: 'de.test.domain.person.CustomerRepository repo', visibility: 'private'})"
					        			+ "CREATE(serviceImpl)-[:DECLARES]->(f_repo)"
					        		+ "CREATE(serviceImpl)-[:IMPLEMENTS]->(serviceInter)"
					        			
				        			+ "CREATE(enum:Java:Enum{fqn: 'de.test.domain.person.CustomerType', name: 'CustomerType'})"
				        			+ "CREATE(person)-[:CONTAINS]->(enum)"
				        			
									+ "CREATE(event1:Java:Class{fqn: 'de.test.domain.person.Order', name: 'OrderActivated'})"
									+ "CREATE(person)-[:CONTAINS]->(event1)"
										+ "CREATE(time1:Java:Field{name: 'timestamp', signature: 'java.lang.Long timestamp', visibility: 'private'})"
										+ "CREATE(event1)-[:DECLARES]->(time1)"
										+ "CREATE(identity1:Java:Field{name: 'customer', signature: 'de.test.domain.person.Customer customer', visibility: 'private'})"
										+ "CREATE(event1)-[:DECLARES]->(identity1)"
										+ "CREATE(m3:Java:Method{name: 'getTimestamp', signature: 'java.lang.Long getTimestamp()', visibility: 'public'})"
							        	+ "CREATE(event1)-[:DECLARES]->(m3)"
										
									+ "CREATE(event2:Java:Class{fqn: 'de.test.domain.person.OrderDelivered', name: 'OrderDelivered'})"
									+ "CREATE(person)-[:CONTAINS]->(event2)"
										+ "CREATE(time2:Java:Field{name: 'start', signature: 'java.time.Date start', visibility: 'private'})"
										+ "CREATE(event2)-[:DECLARES]->(time2)"
										+ "CREATE(identity2:Java:Field{name: 'customerId', signature: 'java.lang.Long customerId', visibility: 'private'})"
										+ "CREATE(event2)-[:DECLARES]->(identity2)"
										+ "CREATE(m4:Java:Method{name: 'getStart', signature: 'java.lang.Long getStart()', visibility: 'public'})"
							        	+ "CREATE(event2)-[:DECLARES]->(m4)"
			        			
			        			+ "CREATE(product:Java:Package{fqn: 'de.test.domain.product', name: 'product'})"
			        			+ "CREATE(domain)-[:CONTAINS]->(product)"	
			        			
		        			+ "CREATE(infra:Java:Package{fqn: 'de.test.infrastructure', name: 'infrastructure'})"
		        			+ "CREATE(root)-[:CONTAINS]->(infra)"
			        			+ "CREATE(control:Java:Class{fqn: 'de.test.infrastructure.CustomerController', name: 'CustomerController'})"
			        			+ "CREATE(infra)-[:CONTAINS]->(control)"
			        			
								+ "CREATE(anno:Java:Annotation{name: 'Anno', fqn: 'de.test.infrastructure.Anno'})"
								+ "CREATE(infra)-[:CONTAINS]->(anno)"
								
								+ "CREATE(unused:Java{name: 'Unused', fqn: 'de.test.infrastructure.Unused'})"
								+ "CREATE(infra)-[:CONTAINS]->(unused)"
		        			
		        			+ "CREATE(application:Java:Package{fqn: 'de.test.application', name: 'application'})"
		        			+ "CREATE(root)-[:CONTAINS]->(application)"
			        			+ "CREATE(app:Java:Class{fqn: 'de.test.application.ApplicationService', name: 'ApplicationService'})"
			        			+ "CREATE(application)-[:CONTAINS]->(app)"
	        ).newServer(); 
    }
	
	@Test
	public void readArtifacts() {
		try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI());) {
			AnalyseService service = new AnalyseServiceImpl(driver);
			service.setStructureService(new StructureService());
			
			JSONArray result = service.analyzeStructure("de.test");
			
			JSONArray infrastructure = result.getJSONObject(0).getJSONArray("contains");
			assertAll("Should return DDD-Types of infrastructure",
				() -> assertEquals(DDDType.CONTROLLER, (DDDType) infrastructure.getJSONObject(0).get("DDD")),
				() -> assertEquals(DDDType.INFRASTRUCTUR, (DDDType) infrastructure.getJSONObject(1).get("DDD")));
			
			
			JSONArray application = result.getJSONObject(1).getJSONArray("contains");
			assertAll("Should return DDD-Types of application",
				() -> assertEquals(DDDType.APPLICATION_SERVICE, (DDDType) application.getJSONObject(0).get("DDD")));
			
			
			JSONArray domain = result.getJSONObject(2).getJSONArray("contains");
			
			JSONArray productDomain = domain.getJSONObject(0).getJSONArray("contains");
			assertEquals(true, productDomain.isEmpty());
			
			JSONArray personDomain = domain.getJSONObject(1).getJSONArray("contains");
			
			System.out.println(personDomain.toString());
			
			assertAll("Should return DDD-Types of domain",
			    () -> assertEquals(DDDType.DOMAIN_EVENT, 	(DDDType) personDomain.getJSONObject(0).get("DDD"), 	personDomain.getJSONObject(0).getString("name")),
			    () -> assertEquals(DDDType.DOMAIN_EVENT, 	(DDDType) personDomain.getJSONObject(1).get("DDD"), 	personDomain.getJSONObject(1).getString("name")),
			    () -> assertEquals(DDDType.VALUE_OBJECT, 	(DDDType) personDomain.getJSONObject(2).get("DDD"), 	personDomain.getJSONObject(2).getString("name")),
			    () -> assertEquals(DDDType.SERVICE, 		(DDDType) personDomain.getJSONObject(3).get("DDD"), 	personDomain.getJSONObject(3).getString("name")),
			    () -> assertEquals(DDDType.SERVICE, 		(DDDType) personDomain.getJSONObject(4).get("DDD"), 	personDomain.getJSONObject(4).getString("name")),
			    () -> assertEquals(DDDType.SERVICE, 		(DDDType) personDomain.getJSONObject(5).get("DDD"), 	personDomain.getJSONObject(5).getString("name")),
			    () -> assertEquals(DDDType.INFRASTRUCTUR,	(DDDType) personDomain.getJSONObject(6).get("DDD"), 	personDomain.getJSONObject(6).getString("name")),
			    () -> assertEquals(DDDType.REPOSITORY, 		(DDDType) personDomain.getJSONObject(7).get("DDD"), 	personDomain.getJSONObject(7).getString("name")),
			    () -> assertEquals(DDDType.REPOSITORY, 		(DDDType) personDomain.getJSONObject(8).get("DDD"), 	personDomain.getJSONObject(8).getString("name")),
			    () -> assertEquals(DDDType.FACTORY, 		(DDDType) personDomain.getJSONObject(9).get("DDD"), 	personDomain.getJSONObject(9).getString("name")),
			    () -> assertEquals(DDDType.FACTORY, 		(DDDType) personDomain.getJSONObject(10).get("DDD"), 	personDomain.getJSONObject(10).getString("name")),
			    () -> assertEquals(DDDType.ENTITY, 			(DDDType) personDomain.getJSONObject(11).get("DDD"), 	personDomain.getJSONObject(11).getString("name")),
			    () -> assertEquals(DDDType.VALUE_OBJECT, 	(DDDType) personDomain.getJSONObject(12).get("DDD"), 	personDomain.getJSONObject(12).getString("name")),
			    () -> assertEquals(DDDType.ENTITY, 			(DDDType) personDomain.getJSONObject(13).get("DDD"), 	personDomain.getJSONObject(13).getString("name")),
			    () -> assertEquals(DDDType.AGGREGATE_ROOT, 	(DDDType) personDomain.getJSONObject(14).get("DDD"),	personDomain.getJSONObject(14).getString("name")));
			
		}
	}
	
	@Test
	public void readArtifactsWithoutDriver() {
		AnalyseService service = new AnalyseServiceImpl(null);
		service.setStructureService(new StructureService());
		
		JSONArray result = service.analyzeStructure("de.test");
		
		assertEquals(true, result.isEmpty());
	}
}
