package illumi.code.ddd.service.metric.impl;

import javax.inject.Inject;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.service.metric.MetricService;
import org.json.JSONObject;

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
		DDDMetricService fitnessMetric = new DDDMetricService(structure.getAllArtifacts());

		OODMetricService oodMetric = new OODMetricService(structure.getPackages());

		return new JSONObject()
				.put("DDD", fitnessMetric.calculate())
				.put("OOD", oodMetric.calculate());
	}
}
