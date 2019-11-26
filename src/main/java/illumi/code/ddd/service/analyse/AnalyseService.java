package illumi.code.ddd.service.analyse;

import illumi.code.ddd.model.Structure;
import org.json.JSONArray;

public interface AnalyseService {
	
	void setStructure(Structure structure);
	
	JSONArray analyzeStructure(String path);
}
