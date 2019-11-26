package illumi.code.ddd.service.fitness;

import illumi.code.ddd.model.DDDStructure;
import org.json.JSONArray;

public interface FitnessService {
	
	void setStructure(DDDStructure structure);
	
	JSONArray getStructureWithFitness();
}
