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

@Controller("/analyse") 
public class AnalyseController {	
	@Inject AnalyseService analyseService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyseController.class);

    @Get("/{path}") 
    @Produces(MediaType.APPLICATION_JSON) 
    public HttpResponse<String> getArtifacts(String path) {
    	LOGGER.info("HTTP GET: analyse/" + path);
    	return HttpResponse.ok(analyseService.analyzeStructure(path).toString());
    }
}