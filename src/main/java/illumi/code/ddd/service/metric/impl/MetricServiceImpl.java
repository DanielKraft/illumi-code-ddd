package illumi.code.ddd.service.metric.impl;

import java.util.ArrayList;

import javax.inject.Inject;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.service.metric.MetricService;
import org.json.JSONObject;

import illumi.code.ddd.model.artifacts.Artifact;


public class MetricServiceImpl implements MetricService {
	    
    private DDDStructure structure;

	public @Inject MetricServiceImpl() {
		// Needed for @Inject
	}
    
    @Override
    public void setStructure(DDDStructure structure) {
    	this.structure = structure;
    }
    
	@Override
	public JSONObject getMetric() {
		ArrayList<Artifact> allArtifacts = (ArrayList<Artifact>) structure.getAllArtifacts();
		DDDFitnessMetricService fitnessMetric = new DDDFitnessMetricService(allArtifacts);
		ArtifactMetricService artifactMetric = new ArtifactMetricService(allArtifacts);

		return new JSONObject()
				.put("metric", 		fitnessMetric.calcFitness())
				.put("DDD", 		artifactMetric.calcArtifactMetric())
				.put("hotspots", 	fitnessMetric.getHotspots());
	}


}
