package illumi.code.ddd.service;

import org.json.JSONArray;

public interface AnalyseService {
	
	void setStructureService(StructureService structureService);
	
	JSONArray analyzeStructure(String path);
}
