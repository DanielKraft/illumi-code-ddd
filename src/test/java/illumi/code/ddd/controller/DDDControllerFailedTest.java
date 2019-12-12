package illumi.code.ddd.controller;

import static org.mockito.Mockito.mock;

import illumi.code.ddd.service.analyse.AnalyseService;
import illumi.code.ddd.service.analyse.impl.AnalyseServiceImpl;
import illumi.code.ddd.service.fitness.FitnessService;
import illumi.code.ddd.service.fitness.impl.FitnessServiceImpl;
import illumi.code.ddd.service.metric.MetricService;
import illumi.code.ddd.service.metric.impl.MetricServiceImpl;
import illumi.code.ddd.service.refactor.RefactorService;
import illumi.code.ddd.service.refactor.impl.RefactorServiceImpl;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
@SuppressWarnings("CheckStyle")
class DDDControllerFailedTest {

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
  void testCreatingMetrics() {
    Assertions.assertThrows(HttpClientResponseException.class, () ->
        client.toBlocking().retrieve(HttpRequest.GET("/metric")));
  }

  @Test
  void testRefactorStructure() {
    Assertions.assertThrows(HttpClientResponseException.class, () ->
        client.toBlocking().retrieve(HttpRequest.GET("/refactor")));
  }
}
