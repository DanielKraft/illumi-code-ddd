package illumi.code.ddd.controller;


import javax.inject.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import illumi.code.ddd.service.AnalyseService;
import illumi.code.ddd.service.FitnessService;
import illumi.code.ddd.service.StructureService;

@Controller("/") 
public class DDDController {	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DDDController.class);
	
	@Inject AnalyseService analyseService;
	@Inject FitnessService metricService;
	
	private StructureService structureService;
	
	public DDDController() {
		super();
		this.structureService = new StructureService();
	}
	
    @Get("/analyse/{path}") 
    @Produces(MediaType.APPLICATION_JSON) 
    public HttpResponse<String> getArtifacts(String path) {
    	LOGGER.info("HTTP GET: analyse/" + path);
    	analyseService.setStructureService(structureService);
    	return HttpResponse.ok(analyseService.analyzeStructure(path).toString());
    }
    
    @Get("/metric") 
    @Produces(MediaType.APPLICATION_JSON) 
    public HttpResponse<String> getMetrics() {
    	LOGGER.info("HTTP GET: metric/");
    	metricService.setStructureService(structureService);
    	return HttpResponse.ok(metricService.getMetrics().toString());
    }
}