package illumi.code.ddd.service;

import java.util.ArrayList;

import javax.inject.Inject;

import org.json.JSONObject;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;


public class MetricServiceImpl implements MetricService {
	    
    private StructureService structureService;
    
    public @Inject MetricServiceImpl() {
    	// @Inject is needed
    }
    
    @Override
    public void setStructureService(StructureService structureService) {
    	this.structureService = structureService;
    }
    
	@Override
	public JSONObject getMetric() {				
		ArrayList<Artifact> allArtifacts = (ArrayList<Artifact>) structureService.getAllArtifacts();
		DDDFitness fitness = calcFitness(allArtifacts);
						
		return new JSONObject()
				.put("metric", fitness.summary())
				.put("DDD", calcArtifactMetric(allArtifacts))
				.put("rating", toJSON(allArtifacts));
	}

	private DDDFitness calcFitness(ArrayList<Artifact> allArtifacts) {
		DDDFitness fitness = new DDDFitness();
		allArtifacts.stream()
			.parallel()
			.forEachOrdered(artifact -> fitness.add(artifact.getDDDFitness()));
		return fitness;
	}
	
	private JSONObject calcArtifactMetric(ArrayList<Artifact> allArtifacts) {
		return new JSONObject()
				.put("#MODULE", countArtifact(DDDType.MODULE, allArtifacts))
				.put("#ENTITY", countArtifact(DDDType.ENTITY, allArtifacts))
				.put("#VALUE_OBJECT", countArtifact(DDDType.VALUE_OBJECT, allArtifacts))
				.put("#AGGREGATE_ROOT", countArtifact(DDDType.AGGREGATE_ROOT, allArtifacts))
				.put("#FACTORY", countArtifact(DDDType.FACTORY, allArtifacts))
				.put("#REPOSITORY", countArtifact(DDDType.REPOSITORY, allArtifacts))
				.put("#SERVICE", countArtifact(DDDType.SERVICE, allArtifacts))
				.put("#APPLICATION_SERVICE", countArtifact(DDDType.APPLICATION_SERVICE, allArtifacts))
				.put("#CONTROLLER", countArtifact(DDDType.CONTROLLER, allArtifacts))
				.put("#INFRASTRUCTUR", countArtifact(DDDType.INFRASTRUCTURE, allArtifacts));
	}
	
	private int countArtifact(DDDType type, ArrayList<Artifact> allArtifacts) {
		int ctr = 0;
		for (Artifact artifact : allArtifacts) {
			if (artifact.getType() == type) {
				ctr++;
			}
		}
		return ctr;
	}
	
	private ArrayList<JSONObject> toJSON(ArrayList<Artifact> allArtifacts) {
		ArrayList<JSONObject> json = new ArrayList<>();
		allArtifacts.stream()
			.parallel()
			.forEachOrdered(artifact -> json.add(artifact.toJSON()));
		return json;
	}
}
