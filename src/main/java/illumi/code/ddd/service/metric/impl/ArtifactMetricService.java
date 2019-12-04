package illumi.code.ddd.service.metric.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import org.json.JSONObject;

import java.util.ArrayList;

public class ArtifactMetricService {

    private ArrayList<Artifact> allArtifacts;

    public ArtifactMetricService(ArrayList<Artifact> allArtifacts) {
        this.allArtifacts = allArtifacts;
    }

    public JSONObject calcArtifactMetric() {
        return new JSONObject()
                .put("#MODULE",                 countArtifact(DDDType.MODULE))
                .put("#ENTITY",                 countArtifact(DDDType.ENTITY))
                .put("#VALUE_OBJECT",           countArtifact(DDDType.VALUE_OBJECT))
                .put("#AGGREGATE_ROOT",         countArtifact(DDDType.AGGREGATE_ROOT))
                .put("#FACTORY",                countArtifact(DDDType.FACTORY))
                .put("#REPOSITORY",             countArtifact(DDDType.REPOSITORY))
                .put("#SERVICE",                countArtifact(DDDType.SERVICE))
                .put("#APPLICATION_SERVICE",    countArtifact(DDDType.APPLICATION_SERVICE))
                .put("#CONTROLLER",             countArtifact(DDDType.CONTROLLER))
                .put("#INFRASTRUCTUR",          countArtifact(DDDType.INFRASTRUCTURE));
    }

    private int countArtifact(DDDType type) {
        int ctr = 0;
        for (Artifact artifact : allArtifacts) {
            if (artifact.getType() == type) {
                ctr++;
            }
        }
        return ctr;
    }
}
