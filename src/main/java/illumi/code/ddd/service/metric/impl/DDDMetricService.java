package illumi.code.ddd.service.metric.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.fitness.DDDFitness;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DDDMetricService {

    private ArrayList<Artifact> allArtifacts;

    public DDDMetricService(List<Artifact> allArtifacts) {
        this.allArtifacts = (ArrayList<Artifact>) allArtifacts;
    }

    public JSONObject calculate() {
        return new JSONObject()
                .put("metric", calcFitness())
                .put("artifact", calcArtifactMetric())
                .put("hotspot", getHotspot());
    }

    private JSONObject calcFitness() {
        DDDFitness fitness = new DDDFitness();
        allArtifacts.stream()
                .parallel()
                .forEachOrdered(artifact -> fitness.add(artifact.getDDDFitness()));
        return fitness.summary();
    }

    private ArrayList<JSONObject> getHotspot() {
        ArrayList<JSONObject> json = new ArrayList<>();
        allArtifacts.stream()
                .parallel()
                .forEachOrdered(artifact -> {
                    if (!artifact.getDDDFitness().getIssues().isEmpty()
                            || artifact.getFitness() < 100.0) {
                        json.add(artifact.toJSONSummary());
                    }
                });
        return json;
    }

    private JSONObject calcArtifactMetric() {
        return new JSONObject()
                .put("#MODULE",                 countArtifact(DDDType.MODULE))
                .put("#ENTITY",                 countArtifact(DDDType.ENTITY))
                .put("#VALUE_OBJECT",           countArtifact(DDDType.VALUE_OBJECT))
                .put("#AGGREGATE_ROOT",         countArtifact(DDDType.AGGREGATE_ROOT))
                .put("#FACTORY",                countArtifact(DDDType.FACTORY))
                .put("#REPOSITORY",             countArtifact(DDDType.REPOSITORY))
                .put("#SERVICE",                countArtifact(DDDType.SERVICE))
                .put("#APPLICATION_SERVICE",    countArtifact(DDDType.APPLICATION_SERVICE))
                .put("#INFRASTRUCTUR",          countArtifact(DDDType.INFRASTRUCTURE));
    }

    private Long countArtifact(DDDType type) {
        long ctr = allArtifacts.stream().filter(artifact -> artifact.getType() == type).count();
        return ctr > 0 ? ctr : null;
    }
}
