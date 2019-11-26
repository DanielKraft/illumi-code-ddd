package illumi.code.ddd.service.fitness;

import illumi.code.ddd.model.Structure;
import org.json.JSONArray;

public interface FitnessService {
	
	void setStructure(Structure structure);
	
	JSONArray getStructureWithFitness();
}
