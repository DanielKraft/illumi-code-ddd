package illumi.code.ddd.service.metric;

import illumi.code.ddd.model.DDDStructure;
import org.json.JSONObject;

public interface MetricService {

  void setStructure(DDDStructure structure);

  JSONObject getMetric();
}
