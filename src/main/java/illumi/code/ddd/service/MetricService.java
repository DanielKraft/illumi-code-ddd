package illumi.code.ddd.service;

import org.json.JSONArray;

public interface MetricService {
	
	void setStructureService(StructureService structureService);
	
	JSONArray getMetrics();
}
