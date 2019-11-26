package illumi.code.ddd.service.metric.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import illumi.code.ddd.model.Structure;
import illumi.code.ddd.service.metric.impl.MetricServiceImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDRating;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetricServiceImplTest {
	
	private MetricServiceImpl service;
	
	@BeforeAll
	void init() {
		Structure structure = new Structure();
		Package domain = new Package("domain", "de.test.domain");
		domain.setType(DDDType.MODULE);
		domain.setFitness(new DDDFitness(3, 2));
		structure.addPackage(domain);
		Class entity = new Class("Entity", "de.test.domain.Entity");
		entity.setType(DDDType.ENTITY);
		entity.setDomain("domain");
		entity.setFitness(new DDDFitness(15, 9));
		domain.addContains(entity);
		structure.addClass(entity);
		Class repo = new Class("EntityRepository", "de.test.domain.EntityRepository");
		repo.setType(DDDType.REPOSITORY);
		repo.setDomain("domain");
		repo.setFitness(new DDDFitness(5, 4));
		domain.addContains(repo);
		structure.addClass(repo);
		
		Package infrastructure = new Package("infrastructure", "de.test.infrastructure");
		infrastructure.setType(DDDType.MODULE);
		infrastructure.setFitness(new DDDFitness(5, 3));
		structure.addPackage(infrastructure);
		Class contoller = new Class("EntityController", "de.test.infrastructure.EntityController");
		contoller.setType(DDDType.CONTROLLER);
		contoller.setFitness(new DDDFitness(12, 7));
		infrastructure.addContains(contoller);
		structure.addClass(contoller);
		
		ArrayList<Artifact> data = new ArrayList<>();
		data.add(domain);
		data.add(infrastructure);
		
		structure.setStructure(data);
		
		service  = new MetricServiceImpl();
		service.setStructure(structure);
	}

	@Test
	void testGetMetric() {
		final JSONObject expected = new JSONObject()
				.put("metric", new JSONObject()
						.put("score", DDDRating.D)
						.put("criteria", new JSONObject()
								.put("total", 40)
								.put("fulfilled", 25))
						.put("fitness", 62.5)
						.put("#Issues", 0))
				.put("DDD", new JSONObject()
				        .put("#APPLICATION_SERVICE",	0)
				        .put("#VALUE_OBJECT", 			0)
				        .put("#CONTROLLER", 			1)
				        .put("#MODULE", 				2)
				        .put("#FACTORY",				0)
				        .put("#REPOSITORY", 			1)
				        .put("#INFRASTRUCTUR", 			0)
				        .put("#SERVICE", 				0)
				        .put("#ENTITY", 				1)
				        .put("#AGGREGATE_ROOT", 		0))
				.put("rating", new JSONArray()
						.put(new JSONObject()
								.put("DDD", 	"REPOSITORY")
								.put("fitness", 80.0)
								.put("domain", "domain")
								.put("name", 	"EntityRepository")
								.put("issues", new ArrayList<>()))
						.put(new JSONObject()
								.put("DDD", 	"MODULE")
								.put("fitness", 66.67)
								.put("name", 	"domain")
								.put("issues", new ArrayList<>()))
						.put(new JSONObject()
								.put("DDD", 	"MODULE")
								.put("fitness", 60.0)
								.put("name", 	"infrastructure")
								.put("issues", new ArrayList<>()))
						.put(new JSONObject()
								.put("DDD", 	"ENTITY")
								.put("fitness", 60.0)
								.put("domain", 	"domain")
								.put("name", 	"Entity")
								.put("issues", new ArrayList<>()))
						.put(new JSONObject()
								.put("DDD", 	"CONTROLLER")
								.put("fitness", 58.33)
								.put("name", 	"EntityController")
								.put("issues", new ArrayList<>())));
		
		final JSONObject result = service.getMetric();
		
		assertEquals(expected.toString(), result.toString());
	}
}
