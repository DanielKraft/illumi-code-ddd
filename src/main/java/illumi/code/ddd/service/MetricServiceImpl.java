package illumi.code.ddd.service;

import java.util.ArrayList;

import javax.inject.Inject;

import org.json.JSONObject;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Package;


public class MetricServiceImpl implements MetricService {
	    
    private StructureService structureService;
    
    public @Inject MetricServiceImpl() { }
    
    @Override
    public void setStructureService(StructureService structureService) {
    	this.structureService = structureService;
    }
    
	@Override
	public JSONObject getMetric() {
		
		DDDFitness fitness = new DDDFitness();
				
		calcFitness(fitness, structureService.getStructure());
		
		return new JSONObject()
				.put("metric", fitness.toJSON())
				.put("rating", getRating());
	}

	private void calcFitness(DDDFitness fitness, ArrayList<Artifact> artifacts) {
		artifacts.stream()
			.parallel()
			.forEach(artifact -> {
				fitness.add(artifact.getDDDFitness());
				if (artifact instanceof Package) {
					calcFitness(fitness, ((Package) artifact).getConataints());
				}
			});
	}

	private ArrayList<JSONObject> getRating() {
		ArrayList<JSONObject> result = new ArrayList<JSONObject>();
		
		getContainingArtifacts(result, structureService.getStructure());
		result.sort((JSONObject a1, JSONObject a2) -> ((Double) a2.get("fitness")).compareTo((Double) a1.get("fitness")));
		return result;
	}

	private void getContainingArtifacts(ArrayList<JSONObject> result, ArrayList<Artifact> artifacts) {
		artifacts.stream()
			.parallel()
			.forEach(artifact -> {
				result.add(artifact.toJSON());
				if (artifact instanceof Package) {
					getContainingArtifacts(result, ((Package) artifact).getConataints());
				}
			});
	}
}
