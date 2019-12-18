package illumi.code.ddd.service.analyse.impl;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;

public class PackageAnalyseService {
  private Package module;
  private DDDStructure structure;

  public PackageAnalyseService(Package module, DDDStructure structure) {
    this.module = module;
    this.structure = structure;
  }

  /**
   * Find aggregate root of the domains.
   */
  public void setAggregateRoot() {
    if (isDomain()) {
      ArrayList<Class> candidates = getAggregateRootCandidates();
      if (candidates.size() == 1) {
        candidates.get(0).setType(DDDType.AGGREGATE_ROOT);
      } else if (!candidates.isEmpty()) {
        for (Class artifact : candidates) {
          if (structure.getDomains().contains(artifact.getLowerName())) {
            artifact.setType(DDDType.AGGREGATE_ROOT);
            return;
          }
        }
        setAggregateRootByDependency(candidates);
      }
    }
  }

  private void setAggregateRootByDependency(ArrayList<Class> candidates) {
    int highscore = 0;
    ArrayList<Class> result = new ArrayList<>();
    for (Class artifact : candidates) {
      int score = countDependsOnOtherCandidates(artifact, candidates);
      if (score == highscore) {
        result.add(artifact);
      } else if (score > highscore) {
        result = new ArrayList<>();
        result.add(artifact);
        highscore = score;
      }
    }
    if (result.size() == 1) {
      result.get(0).setType(DDDType.AGGREGATE_ROOT);
    }
  }

  private int countDependsOnOtherCandidates(Class artifact, ArrayList<Class> candidates) {
    int ctr = 0;
    for (Class candidate : candidates) {
      if (candidate != artifact) {
        ctr += getWightOfDependency(artifact, candidate);
      }
    }
    return ctr;
  }

  private int getWightOfDependency(Class artifact, Class candidate) {
    int ctr = 0;
    for (Field field : artifact.getFields()) {
      if (field.getType().endsWith(candidate.getName())) {
        ctr++;
      } else if (field.getType().startsWith("java.util.")
          && (convertNameToSingular(field.getName()).startsWith(candidate.getLowerName())
          || candidate.getLowerName().contains(convertNameToSingular(field.getName())))) {
        ctr += 2;
      }
    }
    return ctr;
  }

  private String convertNameToSingular(String name) {
    if (name.endsWith("ies")) {
      return name.substring(0, name.length() - 3) + "y";
    } else if (name.endsWith("s")) {
      return name.substring(0, name.length() - 1);
    }
    return name.toLowerCase();
  }

  private boolean isDomain() {
    return structure.getDomains().contains(module.getName());
  }

  private ArrayList<Class> getAggregateRootCandidates() {
    ArrayList<Class> entities = getEntities();
    setUsed(entities);
    if (!entities.isEmpty()) {
      return getEntityWithMinimalDependencies(entities);
    } else {
      return new ArrayList<>();
    }
  }

  private ArrayList<Class> getEntities() {
    ArrayList<Class> entities = new ArrayList<>();

    for (Artifact artifact : module.getContains()) {
      if (artifact.isTypeOf(DDDType.ENTITY)) {
        entities.add((Class) artifact);
      }
    }

    return entities;
  }

  private void setUsed(ArrayList<Class> entities) {
    entities.stream()
        .parallel()
        .forEachOrdered(artifact -> {
          for (Class entity : entities) {
            if (entity != artifact && entity.getDependencies().contains(artifact.getPath())) {
              artifact.addUsed(entity.getPath());
            }
          }
        });
  }

  private ArrayList<Class> getEntityWithMinimalDependencies(ArrayList<Class> entities) {
    ArrayList<Class> result = new ArrayList<>();
    result.add(entities.get(0));

    int highscore = entities.get(0).getUsed().size();

    for (int i = 1; i < entities.size(); i++) {
      if (entities.get(i).getUsed().size() == highscore) {
        result.add(entities.get(i));
      } else if (entities.get(i).getUsed().size() < highscore) {
        highscore = entities.get(i).getUsed().size();
        result = new ArrayList<>();
        result.add(entities.get(i));
      }
    }

    return result;
  }
}
