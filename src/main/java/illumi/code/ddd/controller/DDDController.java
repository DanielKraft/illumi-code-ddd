package illumi.code.ddd.controller;

import javax.inject.Inject;

import illumi.code.ddd.model.Structure;
import illumi.code.ddd.service.analyse.AnalyseService;
import illumi.code.ddd.service.fitness.FitnessService;
import illumi.code.ddd.service.metric.MetricService;
import illumi.code.ddd.service.refactor.RefactorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller()
public class DDDController {
	private static final Logger LOGGER = LoggerFactory.getLogger(DDDController.class);

	@Inject
	AnalyseService analyseService;
	@Inject
	FitnessService fitnessService;
	@Inject
	MetricService metricService;
	@Inject
	RefactorService refactorService;

	private Structure structure;

    @Get("/analyse/{path}") 
    @Produces(MediaType.APPLICATION_JSON) 
    public HttpResponse<String> getArtifacts(String path) {
    	LOGGER.info("HTTP GET: analyse/{}", path);
    	structure = new Structure();
		analyseService.setStructure(structure);
		fitnessService.setStructure(structure);
    	analyseService.analyzeStructure(path);
		return HttpResponse.ok(fitnessService.getStructureWithFitness().toString());
    }

    @Get("/metric") 
    @Produces(MediaType.APPLICATION_JSON) 
    public HttpResponse<String> getMetrics() {
		LOGGER.info("HTTP GET: metric/");
		if (structure != null) {
			metricService.setStructure(structure);
			return HttpResponse.ok(metricService.getMetric().toString());
		}
		return HttpResponse.badRequest("{\"message\":\"No project has been analyzed!\"}");
    }



	@Get("/refactor")
	@Produces(MediaType.APPLICATION_JSON)
	public HttpResponse<String> refactor() {
		LOGGER.info("HTTP GET: refactor/");
		if (structure != null) {
			refactorService.setOldStructure(structure);
			structure = refactorService.refactor();
			fitnessService.setStructure(structure);
			return HttpResponse.ok(fitnessService.getStructureWithFitness().toString());
		}
		return HttpResponse.badRequest("{\"message\":\"No project has been analyzed!\"}");
	}
}