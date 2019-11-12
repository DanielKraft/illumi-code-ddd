package illumi.code.ddd.service;

import org.json.JSONObject;

public interface MetricService {

	void setStructureService(StructureService structureService);
	
	JSONObject getMetric();
}
