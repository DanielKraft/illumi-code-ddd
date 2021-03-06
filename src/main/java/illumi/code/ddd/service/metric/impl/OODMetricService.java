package illumi.code.ddd.service.metric.impl;

import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.File;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("CheckStyle")
public class OODMetricService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OODMetricService.class);

  private ArrayList<Package> packages;

  private ArrayList<Double> distances;

  public OODMetricService(List<Package> packages) {
    this.packages = (ArrayList<Package>) packages;
    this.distances = new ArrayList<>();
  }

  /**
   * Calculates OOD-metric.
   * @return OOD-metric as JSON
   */
  public JSONObject calculate() {
    return new JSONObject()
        .put("module", calculateModule())
        .put("distance", calculateMetric());
  }

  private JSONObject calculateModule() {
    LOGGER.info("[CALCULATE] - OOD - Module");
    JSONObject result = new JSONObject();

    packages.stream()
        .parallel()
        .forEachOrdered(module -> result.put(module.getPath(), analyseModule(module)));

    return result.isEmpty() ? null : result;
  }

  private JSONObject calculateMetric() {
    LOGGER.info("[CALCULATE] - OOD - Distance");
    if (!distances.isEmpty()) {
      DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
      distances.forEach(descriptiveStatistics::addValue);

      return new JSONObject()
          .put("avg", round(descriptiveStatistics.getMean()))
          .put("median", round(descriptiveStatistics.getPercentile(50)))
          .put("standard deviation", round(descriptiveStatistics.getStandardDeviation()))
          .put("min", round(descriptiveStatistics.getMin()))
          .put("max", round(descriptiveStatistics.getMax()));
    }
    return null;
  }

  private JSONObject analyseModule(Package module) {
    Double abstractness = calculateAbstractness(module);
    Double instability = calculateInstability(module);
    Double distance = calculateDistance(abstractness, instability);

    if (abstractness != null) {
      return new JSONObject()
          .put("abstractness", round(abstractness))
          .put("instability", round(instability))
          .put("distance", round(distance));
    }
    return null;
  }

  private Double round(Double value) {
    if (value != null) {
      return (double) Math.round(value * 100.0) / 100.0;
    }
    return null;
  }

  private Double calculateAbstractness(Package module) {
    long numberOfAbstracts = countNumberOfAbstracts(module);
    long numberOfClasses = countNumberOfClasses(module);
    if (numberOfClasses > 0) {
      return (double) numberOfAbstracts / numberOfClasses;
    }
    return null;
  }

  private long countNumberOfAbstracts(Package module) {
    return module.getContains().stream()
        .filter(artifact -> artifact instanceof Interface).count();
  }

  private long countNumberOfClasses(Package module) {
    return module.getContains().stream()
        .filter(artifact -> artifact instanceof File).count();
  }

  private Double calculateInstability(Package module) {
    long numberOfAfferentCouplings = countAfferentCouplings(module);
    long numberOfEfferentCouplings = countEfferentCouplings(module);
    if (numberOfAfferentCouplings > 0
        || numberOfEfferentCouplings > 0) {
      return (double) numberOfEfferentCouplings
          / (numberOfAfferentCouplings + numberOfEfferentCouplings);
    }
    return null;
  }

  private long countAfferentCouplings(Package module) {
    long ctr = 0;
    for (Package item : packages) {
      if (item != module) {
        ctr += item.getContains().stream()
            .filter(artifact -> artifact instanceof File
                && dependsOnModule(module, (File) artifact)).count();
      }
    }
    return ctr;
  }

  private boolean dependsOnModule(Package module, File artifact) {

    for (Interface implInterface : artifact.getImplInterfaces()) {
      if (module.getContains().contains(implInterface)) {
        return true;
      }
    }
    if (artifact instanceof Class) {
      return dependsOnModule(module, (Class) artifact);
    }
    return false;
  }

  private boolean dependsOnModule(Package module, Class artifact) {
    if (artifact.getSuperClass() != null
        && module.getContains().contains(artifact.getSuperClass())) {
      return true;
    }

    for (String dependency : artifact.getDependencies()) {
      if (dependency.contains(String.format(".%s.", module.getName()))) {
        return true;
      }
    }
    return false;
  }

  private long countEfferentCouplings(Package module) {
    long ctr = 0;

    for (Artifact artifact : module.getContains()) {
      if (artifact instanceof File) {
        for (Package item : packages) {
          if (item != module && dependsOnModule(item, (File) artifact)) {
            ctr++;
            break;
          }
        }
      }
    }

    return ctr;
  }

  private Double calculateDistance(Double abstractness, Double instability) {
    if (abstractness != null && instability != null) {
      double distance = Math.abs(abstractness + instability - 1.0);
      distances.add(distance);

      return distance;
    }
    return null;
  }
}
