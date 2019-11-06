package illumi.code.ddd.service;

import org.json.JSONArray;

public interface FitnessService {
	
	void setStructureService(StructureService structureService);
	
	JSONArray getMetrics();
}
