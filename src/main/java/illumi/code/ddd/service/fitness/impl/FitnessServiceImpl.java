package illumi.code.ddd.service.fitness.impl;


import javax.inject.Inject;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.service.fitness.FitnessService;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import illumi.code.ddd.model.artifacts.Interface;

public class FitnessServiceImpl implements FitnessService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FitnessServiceImpl.class);
	        
    private DDDStructure structure;
    
    public @Inject FitnessServiceImpl() {
    	// @Inject is needed
    }
    
    @Override
    public void setStructure(DDDStructure structure) {
    	this.structure = structure;
    }
    
	@Override
	public JSONArray getStructureWithFitness() {
		evaluateModules();
		evaluateClasses();
		evaluateInterfaces();
		evaluateAnnotations();
		return structure.getJSON();
	}
	
	private void evaluateModules() {
		LOGGER.info("Evaluation of Modules");
		structure.getPackages().stream()
			.parallel()
			.forEach(module -> {
				LOGGER.info("DDD:MODULE:{}", module.getName());
				module.evaluate(structure);
			});
	}
	
	private void evaluateClasses() {
		LOGGER.info("Evaluation of Classes");
		structure.getClasses().stream()
			.parallel()
			.forEach(item -> item.evaluate(structure));
	}

	private void evaluateInterfaces() {
		LOGGER.info("Evaluation of Interfaces");
		structure.getInterfaces().stream()
			.parallel()
			.forEach(Interface::evaluate);
	}
	
	private void evaluateAnnotations() {
		LOGGER.info("Evaluation of Annotations");
		structure.getAnnotations().stream()
			.parallel()
			.forEach(item -> {
				LOGGER.info("DDD:INFRASTRUCTUR:{}", item.getName());
				item.evaluate();
			});
	}
}
