package illumi.code.ddd.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import io.netty.handler.codec.json.JsonObjectDecoder;

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
	
		Class aggregateRoot = new Class("Test1", "de.test.domain.test1.Test1");
		aggregateRoot.setType(DDDType.AGGREGATE_ROOT);
		aggregateRoot.setDomain("test1");
		domain1.addConataints(aggregateRoot);
		structure.addClasses(aggregateRoot);
		
		Class valueObject = new Class("Test1Id", "de.test.domain.test1.Test1Id");
		valueObject.setType(DDDType.VALUE_OBJECT);
		valueObject.setDomain("test1");
		domain1.addConataints(valueObject);
		structure.addClasses(valueObject);
		
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
		
		System.out.println(result.toString());
		
		JSONObject domain1 = result.getJSONObject(0).getJSONArray("contains").getJSONObject(0);
		JSONObject application = result.getJSONObject(1);
		JSONObject infrastructure = result.getJSONObject(2);
		JSONObject domain2 = result.getJSONObject(3);
		
		assertAll("Should return fitness of modules",
				 () -> assertEquals(100.0 ,domain1.getDouble("fitness")),
				 () -> assertEquals(100.0 ,application.getDouble("fitness")),
				 () -> assertEquals(100.0 ,infrastructure.getDouble("fitness")),
				 () -> assertEquals(33.33 ,domain2.getDouble("fitness")),
				 () -> assertEquals(50.0 ,domain2.getJSONArray("contains").getJSONObject(1).getDouble("fitness")),
				 () -> assertEquals(50.0 ,domain2.getJSONArray("contains").getJSONObject(2).getDouble("fitness")));
	}
}
