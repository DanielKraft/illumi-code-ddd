package illumi.code.ddd.service;

import java.util.ArrayList;

import javax.inject.Inject;

import org.json.JSONObject;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Package;


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
		
		DDDFitness fitness = new DDDFitness();
				
		calcFitness(fitness, (ArrayList<Artifact>) structureService.getStructure());
		
		JSONObject metric = fitness.toJSON();
		ArrayList<JSONObject> rating = getRating();
				
		return new JSONObject()
				.put("metric", metric)
				.put("DDD", calcArtifactMetric(rating))
				.put("rating", rating);
	}

	private void calcFitness(DDDFitness fitness, ArrayList<Artifact> artifacts) {
		artifacts.stream()
			.parallel()
			.forEach(artifact -> {
				fitness.add(artifact.getDDDFitness());
				if (artifact instanceof Package) {
					calcFitness(fitness, (ArrayList<Artifact>) ((Package) artifact).getConataints());
				}
			});
	}

	private ArrayList<JSONObject> getRating() {
		ArrayList<JSONObject> result = new ArrayList<>();
		
		getContainingArtifacts(result, (ArrayList<Artifact>) structureService.getStructure());
		result.sort((JSONObject a1, JSONObject a2) -> ((Double) a2.get("fitness")).compareTo((Double) a1.get("fitness")));
		return result;
	}

	private void getContainingArtifacts(ArrayList<JSONObject> result, ArrayList<Artifact> artifacts) {
		artifacts.stream()
			.parallel()
			.forEach(artifact -> {
				result.add(artifact.toJSON());
				if (artifact instanceof Package) {
					getContainingArtifacts(result, (ArrayList<Artifact>) ((Package) artifact).getConataints());
				}
			});
	}

	private JSONObject calcArtifactMetric(ArrayList<JSONObject> rating) {
		return new JSONObject()
				.put("#MODULE", countArtifact(DDDType.MODULE, rating))
				.put("#ENTITY", countArtifact(DDDType.ENTITY, rating))
				.put("#VALUE_OBJECT", countArtifact(DDDType.VALUE_OBJECT, rating))
				.put("#AGGREGATE_ROOT", countArtifact(DDDType.AGGREGATE_ROOT, rating))
				.put("#FACTORY", countArtifact(DDDType.FACTORY, rating))
				.put("#REPOSITORY", countArtifact(DDDType.REPOSITORY, rating))
				.put("#SERVICE", countArtifact(DDDType.SERVICE, rating))
				.put("#APPLICATION_SERVICE", countArtifact(DDDType.APPLICATION_SERVICE, rating))
				.put("#CONTROLLER", countArtifact(DDDType.CONTROLLER, rating))
				.put("#INFRASTRUCTUR", countArtifact(DDDType.INFRASTRUCTUR, rating));
	}
	
	private int countArtifact(DDDType type, ArrayList<JSONObject> rating) {
		int ctr = 0;
		for (JSONObject jsonObject : rating) {
			if (jsonObject.get("DDD") == type) {
				ctr++;
			}
		}
		return ctr;
	}
}
