package illumi.code.ddd.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import illumi.code.ddd.service.AnalyseService;
import illumi.code.ddd.service.AnalyseServiceImpl;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;

@MicronautTest
public class DDDControllerTest {

	@Inject
	AnalyseService analyseService; 
	
	@Inject
    @Client("/")
    RxHttpClient client;
	
	@MockBean(AnalyseServiceImpl.class) 
    public AnalyseService analyseService() {
        return mock(AnalyseService.class); 
    }

	@Test
    public void testAnalyzeStructure() {
		
		JSONArray data = new JSONArray();
		data.put(new JSONObject()
				.put("contains", new JSONArray()
						.put(new JSONObject().put("DDD", "VALUE_OBJECT").put("name", "Visit"))
						.put(new JSONObject().put("DDD", "REPOSITORY").put("name", "VisitRepository")))
				.put("DDD", "MODULE")
				.put("name", "visit"));
		
		when(analyseService.analyzeStructure("org.petclinic")).then(invocation -> data);
		
		final String expected = "[{\"contains\":["
									+ "{\"DDD\":\"VALUE_OBJECT\",\"name\":\"Visit\"},"
									+ "{\"DDD\":\"REPOSITORY\",\"name\":\"VisitRepository\"}],"
								+ "\"DDD\":\"MODULE\",\"name\":\"visit\"}]";
		
		final String result = client.toBlocking().retrieve(HttpRequest.GET("/analyse/org.petclinic"));

    	Assertions.assertEquals(expected, result);
        
        verify(analyseService, times(1)).analyzeStructure("org.petclinic");
	}
}
