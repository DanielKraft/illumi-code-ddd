package illumi.code.ddd.service.fitness.impl;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.service.fitness.FitnessService;

import javax.inject.Inject;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FitnessServiceImpl implements FitnessService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FitnessServiceImpl.class);

  private DDDStructure structure;

  public @Inject FitnessServiceImpl() {
    // @Inject is needed
  }

  @Override
  public void setStructure(DDDStructure structure) {
    this.structure = structure;
  }

  @Override
  public JSONArray getStructureWithFitness() {
    evaluateModules();
    evaluateClasses();
    evaluateInterfaces();
    evaluateAnnotations();
    return structure.getJSON();
  }

  private void evaluateModules() {
    structure.getPackages().stream()
        .parallel()
        .forEach(module -> {
          LOGGER.info("[EVALUATE] - MODULE - DDD:MODULE:{}", module.getName());
          module.evaluate(structure);
        });
  }

  private void evaluateClasses() {
    structure.getClasses().stream()
        .parallel()
        .forEach(item -> item.evaluate(structure));
  }

  private void evaluateInterfaces() {
    structure.getInterfaces().stream()
        .parallel()
        .forEach(Interface::evaluate);
  }

  private void evaluateAnnotations() {
    structure.getAnnotations().stream()
        .parallel()
        .forEach(item -> {
          LOGGER.info("[EVALUATE] - ANNOTATION - DDD:INFRASTRUCTURE:{}", item.getName());
          item.evaluate();
        });
  }
}
