package illumi.code.ddd.service.metric.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.fitness.DDDFitness;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("CheckStyle")
public class DDDMetricService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DDDMetricService.class);

  private ArrayList<Artifact> allArtifacts;

  public DDDMetricService(List<Artifact> allArtifacts) {
    this.allArtifacts = (ArrayList<Artifact>) allArtifacts;
  }

  /**
   * Calculates DDD-Metric.
   * @return metric summary as JSON
   */
  public JSONObject calculate() {
    return new JSONObject()
        .put("metric", calcFitness()
            .put("statistic", allArtifacts.isEmpty() ? null : getStatistics()))
        .put("artifact", calcArtifactMetric())
        .put("hotspot", getHotspot());
  }

  private JSONObject calcFitness() {
    LOGGER.info("[CALCULATE] - DDD - Fitness");
    DDDFitness fitness = new DDDFitness();
    allArtifacts.stream()
        .parallel()
        .forEachOrdered(artifact -> fitness.add(artifact.getDDDFitness()));
    return fitness.summary();
  }

  private JSONObject getStatistics() {
    LOGGER.info("[CALCULATE] - DDD - Statistic");
    DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
    allArtifacts.forEach(artifact -> descriptiveStatistics.addValue(artifact.getFitness()));

    return new JSONObject()
        .put("avg", round(descriptiveStatistics.getMean()))
        .put("median", round(descriptiveStatistics.getPercentile(50)))
        .put("standard deviation", round(descriptiveStatistics.getStandardDeviation()))
        .put("min", round(descriptiveStatistics.getMin()))
        .put("max", round(descriptiveStatistics.getMax()));
  }

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private ArrayList<JSONObject> getHotspot() {
    LOGGER.info("[CALCULATE] - DDD - Hotspot");
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
    LOGGER.info("[CALCULATE] - DDD - Artifact");
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
