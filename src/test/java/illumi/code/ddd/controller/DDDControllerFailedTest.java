package illumi.code.ddd.controller;

import static org.mockito.Mockito.mock;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import illumi.code.ddd.service.AnalyseService;
import illumi.code.ddd.service.AnalyseServiceImpl;
import illumi.code.ddd.service.FitnessService;
import illumi.code.ddd.service.FitnessServiceImpl;
import illumi.code.ddd.service.MetricService;
import illumi.code.ddd.service.MetricServiceImpl;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;

@MicronautTest
class DDDControllerFailedTest {

	@Inject AnalyseService analyseService; 
	@Inject FitnessService fitnessService;
	@Inject MetricService metricService;
	
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
	
	@Test
    void testCreatingMetrics() {
		Assertions.assertThrows(HttpClientResponseException.class, () -> client.toBlocking().retrieve(HttpRequest.GET("/metric")));
	}
}
