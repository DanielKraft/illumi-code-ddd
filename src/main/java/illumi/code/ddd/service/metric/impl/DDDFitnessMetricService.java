package illumi.code.ddd.service.metric.impl;

import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.fitness.DDDFitness;
import org.json.JSONObject;

import java.util.ArrayList;

public class DDDFitnessMetricService {

    private ArrayList<Artifact> allArtifacts;

    public DDDFitnessMetricService(ArrayList<Artifact> allArtifacts) {
        this.allArtifacts = allArtifacts;
    }

    public JSONObject calcFitness() {
        DDDFitness fitness = new DDDFitness();
        allArtifacts.stream()
                .parallel()
                .forEachOrdered(artifact -> fitness.add(artifact.getDDDFitness()));
        return fitness.summary();
    }

    public ArrayList<JSONObject> getHotspots() {
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
}
