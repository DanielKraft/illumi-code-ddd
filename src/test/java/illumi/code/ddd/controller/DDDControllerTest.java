package illumi.code.ddd.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.service.analyse.AnalyseService;
import illumi.code.ddd.service.analyse.impl.AnalyseServiceImpl;
import illumi.code.ddd.service.fitness.FitnessService;
import illumi.code.ddd.service.fitness.impl.FitnessServiceImpl;
import illumi.code.ddd.service.metric.MetricService;
import illumi.code.ddd.service.metric.impl.MetricServiceImpl;
import illumi.code.ddd.service.refactor.RefactorService;
import illumi.code.ddd.service.refactor.impl.RefactorServiceImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;

@MicronautTest
class DDDControllerTest {

	@Inject
	AnalyseService analyseService;
	@Inject
	FitnessService fitnessService;
	@Inject
	MetricService metricService;
	@Inject
	RefactorService refactorService;
	
	@Inject
    @Client("/")
    RxHttpClient client;
	
	@MockBean(AnalyseServiceImpl.class)
    AnalyseService analyseService() {
        return mock(AnalyseService.class); 
    }
	
	@MockBean(FitnessServiceImpl.class)
    FitnessService fitnessService() {
        return mock(FitnessService.class); 
    }

	@MockBean(MetricServiceImpl.class)
    MetricService metricService() {
        return mock(MetricService.class);
    }

	@MockBean(RefactorServiceImpl.class)
	RefactorService refactorService() {
		return mock(RefactorService.class);
	}

	@Test
    void testAnalyzeStructure() {
		
		JSONArray data = new JSONArray();
		data.put(new JSONObject()
				.put("contains", new JSONArray()
						.put(new JSONObject().put("DDD", "VALUE_OBJECT").put("name", "Visit").put("fitness", 16.67).put("domain", "visit"))
						.put(new JSONObject().put("DDD", "REPOSITORY").put("name", "VisitRepository").put("fitness", 0).put("domain", "visit")))
				.put("DDD", "MODULE")
				.put("name", "visit"));
		
		when(analyseService.analyzeStructure("org.petclinic")).then(invocation -> data);
		when(fitnessService.getStructureWithFitness()).then(invocation -> data);
				
		final String expected = "[{\"contains\":["
									+ "{\"DDD\":\"VALUE_OBJECT\",\"fitness\":16.67,\"domain\":\"visit\",\"name\":\"Visit\"},"
									+ "{\"DDD\":\"REPOSITORY\",\"fitness\":0,\"domain\":\"visit\",\"name\":\"VisitRepository\"}],"
								+ "\"DDD\":\"MODULE\",\"name\":\"visit\"}]";
		
		final String result = client.toBlocking().retrieve(HttpRequest.GET("/analyse/org.petclinic"));

    	Assertions.assertEquals(expected, result);
        
    	verify(analyseService, times(1)).analyzeStructure("org.petclinic");
        verify(fitnessService, times(1)).getStructureWithFitness();
	}
	
	@Test
    void testCreatingMetrics() {
		JSONObject data = new JSONObject()
				.put("metric", new JSONObject()
						.put("score", "F")
						.put("criteria", new JSONObject()
								.put("total", 92)
								.put("fulfilled", 20))
						.put("fitness (%)", 21.74));
		
		when(metricService.getMetric()).then(invocation -> data);
		
		final String expected = "{\"metric\":{\"score\":\"F\",\"criteria\":{\"total\":92,\"fulfilled\":20},\"fitness (%)\":21.74}}";
		
		final String result = client.toBlocking().retrieve(HttpRequest.GET("/metric"));
		
		Assertions.assertEquals(expected, result);
		
		verify(metricService, times(1)).getMetric();
	}

	@Test
	void testRefactoring() {
		JSONArray data = new JSONArray();
		data.put(new JSONObject()
				.put("contains", new JSONArray()
						.put(new JSONObject().put("DDD", "VALUE_OBJECT").put("name", "Visit").put("fitness", 16.67).put("domain", "visit"))
						.put(new JSONObject().put("DDD", "REPOSITORY").put("name", "VisitRepository").put("fitness", 0).put("domain", "visit")))
				.put("DDD", "MODULE")
				.put("name", "visit"));

		when(refactorService.refactor()).then(invocation -> new DDDStructure());
		when(fitnessService.getStructureWithFitness()).then(invocation -> data);

		final String expected = "[{\"contains\":["
				+ "{\"DDD\":\"VALUE_OBJECT\",\"fitness\":16.67,\"domain\":\"visit\",\"name\":\"Visit\"},"
				+ "{\"DDD\":\"REPOSITORY\",\"fitness\":0,\"domain\":\"visit\",\"name\":\"VisitRepository\"}],"
				+ "\"DDD\":\"MODULE\",\"name\":\"visit\"}]";

		final String result = client.toBlocking().retrieve(HttpRequest.GET("/refactor"));

		Assertions.assertEquals(expected, result);

		verify(refactorService, times(1)).refactor();
		verify(fitnessService, times(1)).getStructureWithFitness();
	}
}
