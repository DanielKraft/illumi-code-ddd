package illumi.code.ddd.service.metric;

import illumi.code.ddd.model.Structure;
import org.json.JSONObject;

public interface MetricService {

	void setStructure(Structure structure);
	
	JSONObject getMetric();
}
