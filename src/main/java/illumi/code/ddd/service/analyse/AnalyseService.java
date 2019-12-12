package illumi.code.ddd.service.analyse;

import illumi.code.ddd.model.DDDStructure;
import org.json.JSONArray;

public interface AnalyseService {

  void setStructure(DDDStructure structure);

  JSONArray analyzeStructure(String path);
}
