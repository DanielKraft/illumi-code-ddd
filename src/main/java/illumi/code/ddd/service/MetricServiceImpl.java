package illumi.code.ddd.service;

import javax.inject.Inject;

import org.json.JSONArray;
import org.neo4j.driver.v1.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.Package;
import illumi.code.ddd.model.Class;
import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.Interface;
import illumi.code.ddd.model.Method;
import illumi.code.ddd.model.Enum;
import illumi.code.ddd.model.Field;
import illumi.code.ddd.model.Annotation;

public class MetricServiceImpl implements MetricService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyseServiceImpl.class);
	
    private Driver driver;
        
    private StructureService structureService;
    
    public @Inject MetricServiceImpl(Driver driver) { 
    	this.driver = driver;
    }
    
    public void setStructureService(StructureService structureService) {
    	this.structureService = structureService;
    }
    
	@Override
	public JSONArray getMetrics() {
		evaluateClasses();
		return structureService.getJOSN();
	}
	
	private void evaluatePackages() {
		
	}
	
	private void evaluateClasses() {
		structureService.getClasses().stream()
			.parallel()
			.forEach(item -> {
				if (item.getType() == DDDType.ENTITY) {
					evaluateEntity(item);
				}
			});
	}
	
	private void evaluateEntity(Class item) {
		DDDFitness fitness = new DDDFitness();
		// Must have criteria of Entity: ID, equals() and hashCode()
		fitness.addNumberOfCriteria(3);
		
		for (Field field : item.getFields()) {
			// Is field an ID? 
			if (field.getName().toUpperCase().endsWith("ID")) {
				fitness.incNumberOfFulfilledCriteria();
			}
			
			// Is type of field Entity or Value Object?
			fitness.incNumberOfCriteria();
			if (field.getType().contains(structureService.getPath())) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		
		for (Method method : item.getMethods()) {
			// equals() or hashCode()? 
			if (method.getName().equals("equals")) {
				fitness.incNumberOfFulfilledCriteria();
			} else if (method.getName().equals("hashCode")) {
				fitness.incNumberOfFulfilledCriteria();
			}
		}
		item.setFitness(fitness);
	}
	
	private void evaluateInterfaces() {
		structureService.getInterfaces().stream()
			.parallel()
			.forEach(item -> {
				
			});
	}
	
	private void evaluateEnums() {
		structureService.getEnums().stream()
			.parallel()
			.forEach(item -> {
				
			});
	}
	
	private void evaluateAnnotations() {
		structureService.getAnnotations().stream()
			.parallel()
			.forEach(item -> {
				
			});
	}
	

}
