package illumi.code.ddd.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Method;
import illumi.code.ddd.model.artifacts.Package;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FitnessServiceImplTest {

private FitnessServiceImpl service;
	
	@BeforeAll
	public void init() {
		StructureService structure = new StructureService();
		structure.setPath("de.test");
		
		Package domain = new Package("domain", "de.test.domain");
		domain.setType(DDDType.MODULE);
		structure.addPackage(domain);
		
		Package domain1 = new Package("test1", "de.test.domain.test1");
		domain1.setType(DDDType.MODULE);
		domain.addConataints(domain1);
		structure.addPackage(domain1);
		structure.addDomain("test1");
	
		Class superClass = new Class("Super", "de.test.domain.test1.Super");
		superClass.setType(DDDType.ENTITY); 
		superClass.setDomain("test1"); 
		superClass.addField(new Field("private", "type", "String"));
		superClass.addField(new Field("private", "id", "long"));
		superClass.addSuperClass(new Class("Default", "de.Default"));
		domain1.addConataints(superClass);
		structure.addClasses(superClass);
		
		Class aggregateRoot = new Class("Test1", "de.test.domain.test1.Test1");
		aggregateRoot.setType(DDDType.AGGREGATE_ROOT); 
		aggregateRoot.setDomain("test1"); 
		aggregateRoot.addField(new Field("private", "name", "String"));
		aggregateRoot.addField(new Field("private", "id", "de.test.domain.test1.Test1Id"));
		aggregateRoot.addMethod(new Method("public", "getId", "de.test.domain.test1.Test1Id getId()"));
		aggregateRoot.addMethod(new Method("public", "equals", "boolean equals()"));
		aggregateRoot.addMethod(new Method("public", "hashCode", "int hashCode()"));
		aggregateRoot.addSuperClass(superClass);
		domain1.addConataints(aggregateRoot);
		structure.addClasses(aggregateRoot);
		
		Class valueObject = new Class("Test1Id", "de.test.domain.test1.Test1Id");
		valueObject.setType(DDDType.VALUE_OBJECT); 
		valueObject.setDomain("test1"); 
		valueObject.addField(new Field("private", "something", "org.Something"));
		valueObject.addField(new Field("private", "desc", "java.lang.String"));
		valueObject.addField(new Field("private", "id", "java.lang.long"));
		valueObject.addMethod(new Method("public", "setDesc", "de.test.domain.test1.Test1Id setDesc(java.lang.String)"));
		valueObject.addMethod(new Method("public", "getDesc", "java.lang.String getDesc()"));
		valueObject.addMethod(new Method("public", "setId", "void setId(java.lang.long)"));
		valueObject.addMethod(new Method("public", "getId", "java.lang.long getId()"));
		valueObject.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		domain1.addConataints(valueObject); 
		structure.addClasses(valueObject);
		
		Interface repositoryInterface = new Interface("Test1Repository", "de.test.domain.test1.Test1Repository");
		repositoryInterface.setType(DDDType.REPOSITORY); 
		repositoryInterface.setDomain("test1"); 
		repositoryInterface.addMethod(new Method("public", "add", "void add(de.test.domain.test1.Test1)"));
		repositoryInterface.addMethod(new Method("public", "insert", "void insert(de.test.domain.test1.Test1)"));
		repositoryInterface.addMethod(new Method("public", "remove", "void remove(de.test.domain.test1.Test1)"));
		repositoryInterface.addMethod(new Method("public", "exists", "boolean exists(de.test.domain.test1.Test1)"));
		domain1.addConataints(repositoryInterface); 
		structure.addInterfaces(repositoryInterface);
		
		Class repository = new Class("Test1RepositoryImpl", "de.test.domain.test1.Test1RepositoryImpl");
		repository.setType(DDDType.REPOSITORY); 
		repository.setDomain("test1"); 
		repository.addMethod(new Method("public", "findById", "de.test.domain.test1.Test1 findById(de.test.domain.test1.Test1Id)"));
		repository.addMethod(new Method("public", "getName", "de.test.domain.test1.Test1 findById(java.lang.String)"));
		repository.addMethod(new Method("public", "save", "void save(de.test.domain.test1.Test1)"));
		repository.addMethod(new Method("public", "delete", "void delete(de.test.domain.test1.Test1)"));
		repository.addMethod(new Method("public", "contains", "boolean contains(de.test.domain.test1.Test1)"));
		repository.addMethod(new Method("public", "update", "void update(de.test.domain.test1.Test1)"));
		repository.addMethod(new Method("public", "toString", "java.lang.String toString(de.test.domain.test1.Test1)"));
		repository.addImplInterface(repositoryInterface);
		domain1.addConataints(repository); 
		structure.addClasses(repository);
		
		Interface factoryInterface = new Interface("Test1Factory", "de.test.domain.test1.Test1Factory");
		factoryInterface.setType(DDDType.FACTORY); 
		factoryInterface.setDomain("test1"); 
		factoryInterface.addField(new Field("private", "CONST", "java.lang.Long"));
		factoryInterface.addField(new Field("private", "repository", "de.test.domain.test1.Test1Repository"));
		factoryInterface.addMethod(new Method("public", "create", "de.test.domain.test1.Test1 create()"));
		factoryInterface.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		domain1.addConataints(factoryInterface); 
		structure.addInterfaces(factoryInterface);
		
		Class factory = new Class("Test1FactoryImpl", "de.test.domain.test1.Test1FactoryImpl");
		factory.setType(DDDType.FACTORY); 
		factory.setDomain("test1");  
		factory.addField(new Field("private", "CONST", "java.lang.Long"));
		factory.addField(new Field("private", "repository", "de.test.domain.test1.Test1Repository"));
		factory.addMethod(new Method("public", "create", "de.test.domain.test1.Test1 create()"));
		factory.addMethod(new Method("public", "toString", "java.lang.String toString()"));
		factory.addImplInterface(factoryInterface);
		domain1.addConataints(factory); 
		structure.addClasses(factory);
		
		Interface domainServiceInterface = new Interface("Test1Service", "de.test.domain.test1.Test1Service");
		domainServiceInterface.setType(DDDType.SERVICE); 
		domainServiceInterface.setDomain("test1"); 
		domain1.addConataints(domainServiceInterface); 
		structure.addInterfaces(domainServiceInterface);
		
		Class domainService = new Class("Test1ServiceImpl", "de.test.domain.test1.Test1ServiceImpl");
		domainService.setType(DDDType.SERVICE); 
		domainService.setDomain("test1"); 
		domainService.addImplInterface(domainServiceInterface);
		domain1.addConataints(domainService); 
		structure.addClasses(domainService);
		
		Interface repositorInterface2 = new Interface("Other1", "de.test.domain.test1.Other1");
		repositorInterface2.setType(DDDType.REPOSITORY); 
		repositorInterface2.setDomain("test1"); 
		domain1.addConataints(repositorInterface2); 
		structure.addInterfaces(repositorInterface2);
		
		Class repository2 = new Class("OtherRepository", "de.test.domain.test1.OtherRepository");
		repository2.setType(DDDType.REPOSITORY); 
		repository2.setDomain("test1"); 
		domain1.addConataints(repository2); 
		structure.addClasses(repository2);
		
		Interface factoryInterface2 = new Interface("Other2", "de.test.domain.test1.Other2");
		factoryInterface2.setType(DDDType.FACTORY); 
		factoryInterface2.setDomain("test1"); 
		domain1.addConataints(factoryInterface2); 
		structure.addInterfaces(factoryInterface2);
		
		Class factory2 = new Class("OtherFactory", "de.test.domain.test1.OtherFactory");
		factory2.setType(DDDType.FACTORY); 
		factory2.setDomain("test1"); 
		domain1.addConataints(factory2); 
		structure.addClasses(factory2);
		
		Interface domainServiceInterface2 = new Interface("Other3", "de.test.domain.test1.Other3");
		domainServiceInterface2.setType(DDDType.SERVICE); 
		domainServiceInterface2.setDomain("test1"); 
		domain1.addConataints(domainServiceInterface2); 
		structure.addInterfaces(domainServiceInterface2);
		
		Class domainService2 = new Class("OtherService", "de.test.domain.test1.OtherService");
		domainService2.setType(DDDType.SERVICE); 
		domainService2.setDomain("test1"); 
		domain1.addConataints(domainService2); 
		structure.addClasses(domainService2);
		
		Class domainEvent = new Class("Ordered", "de.test.domain.test1.Ordered");
		domainEvent.setType(DDDType.DOMAIN_EVENT); 
		domainEvent.setDomain("test1"); 
		domainEvent.addField(new Field("private", "timestamp", "java.lang.Long timestamp"));
		domainEvent.addField(new Field("private", "test1", "de.test.domain.test1.Test1 test1"));
		domainEvent.addMethod(new Method("public", "getTimestamp", "java.lang.Long getTimestamp()"));
		domainEvent.addMethod(new Method("public", "getTest", "de.test.domain.test1.Test1 test1 getTimestamp()"));
		domainEvent.addMethod(new Method("public", "setTest", "void getTimestamp(de.test.domain.test1.Test1 test1)"));
		domain1.addConataints(domainEvent); 
		structure.addClasses(domainEvent);
		
		Package infrastructure = new Package("infrastructure", "de.test.infrastructure");
		infrastructure.setType(DDDType.MODULE);
		structure.addPackage(infrastructure);
		
		Class controller = new Class("Controller", "de.test.infrastructure.Controller");
		controller.setType(DDDType.CONTROLLER);
		infrastructure.addConataints(controller);
		structure.addClasses(controller);
		
		Package application = new Package("application", "de.test.application");
		application.setType(DDDType.MODULE);
		structure.addPackage(application);
		
		Class appService = new Class("ApplicationService", "de.test.application.ApplicationService");
		appService.setType(DDDType.APPLICATION_SERVICE);
		application.addConataints(appService);
		structure.addClasses(appService);
		
		Package domain2 = new Package("test2", "de.test.test2");
		domain2.setType(DDDType.MODULE);
		structure.addPackage(domain2);
		structure.addDomain("test2");
		
		Class entity = new Class("Entity", "de.test.test2.Entity");
		entity.setType(DDDType.ENTITY);
		entity.setDomain("test2");
		domain2.addConataints(entity);
		structure.addClasses(entity);
	
		Package falseInfrastructure = new Package("infra", "de.test.test2.infra");
		falseInfrastructure.setType(DDDType.MODULE);
		domain2.addConataints(falseInfrastructure);
		structure.addPackage(falseInfrastructure);
		
		Class infra = new Class("InfraService", "de.test.test2.infrastructure.InfraService");
		infra.setType(DDDType.INFRASTRUCTUR);
		falseInfrastructure.addConataints(infra);
		structure.addClasses(infra);
		
		Package falseApplication = new Package("application", "de.test.test2.application");
		falseApplication.setType(DDDType.MODULE);
		domain2.addConataints(falseApplication);
		structure.addPackage(falseApplication);
		
		Class app = new Class("AppService", "de.test.test2.application.AppService");
		app.setType(DDDType.APPLICATION_SERVICE);
		falseApplication.addConataints(app);
		structure.addClasses(app);
			
		ArrayList<Artifact> data = new ArrayList<>();
		data.add(domain);
		data.add(application);
		data.add(infrastructure);
		data.add(domain2);
		
		structure.setStructure(data);
		
		service = new FitnessServiceImpl();
		service.setStructureService(structure);
	}
	
	@Test
	public void testFitnessOfModules() {
		final JSONArray result = service.getStructureWithFitness();
				
		JSONObject domain1 = result.getJSONObject(0).getJSONArray("contains").getJSONObject(0);
		JSONObject application = result.getJSONObject(1);
		JSONObject infrastructure = result.getJSONObject(2);
		JSONObject domain2 = result.getJSONObject(3);
		
		assertAll("Should return fitness of modules",
				 () -> assertEquals(100.0, domain1.getDouble("fitness"), domain1.getString("name")),
				 () -> assertEquals(100.0, application.getDouble("fitness"), application.getString("name")),
				 () -> assertEquals(100.0, infrastructure.getDouble("fitness"), infrastructure.getString("name")),
				 () -> assertEquals(33.33,  domain2.getDouble("fitness"), domain2.getString("name")),
				 () -> assertEquals(50.0, domain2.getJSONArray("contains").getJSONObject(1).getDouble("fitness"), domain2.getJSONArray("contains").getJSONObject(1).getString("name")),
				 () -> assertEquals(50.0, domain2.getJSONArray("contains").getJSONObject(2).getDouble("fitness"), domain2.getJSONArray("contains").getJSONObject(2).getString("name")));
	}
	
	@Test
	public void testFitnessOfClasses() {
		final JSONArray result = service.getStructureWithFitness();		

		JSONArray domain = result.getJSONObject(0).getJSONArray("contains").getJSONObject(0).getJSONArray("contains");
		
		assertAll("Should return fitness of classes",
				 () -> assertEquals(20.0, domain.getJSONObject(0).getDouble("fitness"), domain.getJSONObject(0).getString("name")),
				 () -> assertEquals(100.0, domain.getJSONObject(1).getDouble("fitness"), domain.getJSONObject(1).getString("name")),
				 () -> assertEquals(53.85, domain.getJSONObject(2).getDouble("fitness"), domain.getJSONObject(2).getString("name")),
				 () -> assertEquals(100.0, domain.getJSONObject(4).getDouble("fitness"), domain.getJSONObject(4).getString("name")),
				 () -> assertEquals(100.0, domain.getJSONObject(6).getDouble("fitness"), domain.getJSONObject(6).getString("name")),
				 () -> assertEquals(100.0, domain.getJSONObject(8).getDouble("fitness"), domain.getJSONObject(8).getString("name")),
				 () -> assertEquals(0.0, domain.getJSONObject(10).getDouble("fitness"), domain.getJSONObject(10).getString("name")),
				 () -> assertEquals(0.0, domain.getJSONObject(12).getDouble("fitness"), domain.getJSONObject(12).getString("name")),
				 () -> assertEquals(0.0, domain.getJSONObject(14).getDouble("fitness"), domain.getJSONObject(14).getString("name")),
				 () -> assertEquals(71.43, domain.getJSONObject(15).getDouble("fitness"), domain.getJSONObject(15).getString("name")));
	}
	
	@Test
	public void testFitnessOfInterfaces() {
		final JSONArray result = service.getStructureWithFitness();		

		JSONArray domain = result.getJSONObject(0).getJSONArray("contains").getJSONObject(0).getJSONArray("contains");
		
		assertAll("Should return fitness of classes",
				 () -> assertEquals(83.33, domain.getJSONObject(3).getDouble("fitness"), domain.getJSONObject(3).getString("name")),
				 () -> assertEquals(100.0, domain.getJSONObject(5).getDouble("fitness"), domain.getJSONObject(5).getString("name")),
				 () -> assertEquals(100.0, domain.getJSONObject(7).getDouble("fitness"), domain.getJSONObject(7).getString("name")),
				 () -> assertEquals(0.0, domain.getJSONObject(9).getDouble("fitness"), domain.getJSONObject(9).getString("name")),
				 () -> assertEquals(0.0, domain.getJSONObject(11).getDouble("fitness"), domain.getJSONObject(11).getString("name")),
				 () -> assertEquals(100.0, domain.getJSONObject(13).getDouble("fitness"), domain.getJSONObject(13).getString("name")));
	}
}
