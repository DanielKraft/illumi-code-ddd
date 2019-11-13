package illumi.code.ddd.service;


import javax.inject.Inject;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import illumi.code.ddd.model.artifacts.Interface;

public class FitnessServiceImpl implements FitnessService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FitnessServiceImpl.class);
	        
    private StructureService structureService;
    
    public @Inject FitnessServiceImpl() {
    	// @Inject is needed
    }
    
    @Override
    public void setStructureService(StructureService structureService) {
    	this.structureService = structureService;
    }
    
	@Override
	public JSONArray getStructureWithFitness() {
		evaluateModules();
		evaluateClasses();
		evaluateInterfaces();
		evaluateAnnotations();
		return structureService.getJOSN();
	}
	
	private void evaluateModules() {
		LOGGER.info("Evaluation of Modules");
		structureService.getPackages().stream()
			.parallel()
			.forEach(module -> {
				LOGGER.info("DDD:MODULE:{}", module.getName());
				module.evaluate(structureService);
			});
	}
	
	private void evaluateClasses() {
		LOGGER.info("Evaluation of Classes");
		structureService.getClasses().stream()
			.parallel()
			.forEach(item -> item.evaluate(structureService));
	}

	private void evaluateInterfaces() {
		LOGGER.info("Evaluation of Interfaces");
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(Interface::evaluate);
	}
	
	private void evaluateAnnotations() {
		LOGGER.info("Evaluation of Annotations");
		structureService.getAnnotations().stream()
			.parallel()
			.forEach(item -> {
				LOGGER.info("DDD:INFRASTRUCTUR:{}", item.getName());
				item.evaluate();
			});
	}
}
