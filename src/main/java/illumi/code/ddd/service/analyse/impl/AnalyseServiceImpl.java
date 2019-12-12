package illumi.code.ddd.service.analyse.impl;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Annotation;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Enum;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.service.analyse.AnalyseService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.json.JSONArray;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AnalyseServiceImpl implements AnalyseService {

  private static final String QUERY_ARTIFACT =
      "MATCH (root:Package)-[:CONTAINS]->(artifact) "
          + "WHERE root.fqn={path} "
          + "AND (artifact:Package "
          + "OR artifact:Class "
          + "OR artifact:Interface "
          + "OR artifact:Enum "
          + "OR artifact:Annotation) "
          + "RETURN DISTINCT artifact.name as name, "
          + "artifact.fqn as path, "
          + "labels(artifact) as types";

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalyseServiceImpl.class);

  private Driver driver;

  private DDDStructure structure;

  public @Inject AnalyseServiceImpl(Driver driver) {
    this.driver = driver;
    this.structure = new DDDStructure();
  }

  @Override
  public void setStructure(DDDStructure structure) {
    this.structure = structure;
  }

  @Override
  public JSONArray analyzeStructure(String path) {
    structure.setPath(path);
    structure.setStructure(getArtifacts(path));
    analyzeClasses();
    analyzeInterfaces();
    analyzeEnums();
    analyzeAnnotations();

    setupDomains();
    analyseDomains();

    findInfrastructure();

    findEvents();
    return structure.getJSON();
  }

  private ArrayList<Artifact> getArtifacts(String path) {
    try (Session session = driver.session()) {
      LOGGER.info("[READ] Artifacts of {}", path);
      StatementResult result = session.run(QUERY_ARTIFACT, Values.parameters("path", path));
      return convertResultToArtifacts(result);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
    return new ArrayList<>();
  }

  private ArrayList<Artifact> convertResultToArtifacts(StatementResult result) {
    ArrayList<Artifact> artifacts = new ArrayList<>();
    result.stream()
        .parallel()
        .forEach(item -> {
          List<Object> types = item.get("types").asList();
          Artifact artifact;

          if (types.contains("Package")) {
            artifact = new Package(item);
            ((Package) artifact).setContains(getArtifacts(artifact.getPath()));
            structure.addPackage((Package) artifact);
            LOGGER.info("[CREATE] - PACKAGE - {}", artifact.getPath());
          } else if (types.contains("Class")) {
            artifact = new Class(item);
            structure.addClass((Class) artifact);
            LOGGER.info("[CREATE] - CLASS - {}", artifact.getPath());
          } else if (types.contains("Interface")) {
            artifact = new Interface(item);
            structure.addInterface((Interface) artifact);
            LOGGER.info("[CREATE] - INTERFACE - {}", artifact.getPath());
          } else if (types.contains("Enum")) {
            artifact = new Enum(item);
            structure.addEnum((Enum) artifact);
            LOGGER.info("[CREATE] - ENUM - {}", artifact.getPath());
          } else {
            artifact = new Annotation(item);
            structure.addAnnotation((Annotation) artifact);
            LOGGER.info("[CREATE] - ANNOTATION - {}", artifact.getPath());
          }

          artifacts.add(artifact);
        });
    return artifacts;
  }

  private void analyzeClasses() {
    structure.getClasses().stream()
        .parallel()
        .forEach(item -> {
          item.setFields(driver);
          item.setMethods(driver);
          item.setSuperClass(driver, structure.getClasses());
          item.setImplInterfaces(driver, structure.getInterfaces());
          item.setAnnotations(driver, structure.getAnnotations());
          item.setDependencies(driver, structure.getPath());

          if (item.getSuperClass() != null) {
            item.setType(DDDType.ENTITY);
            item.getSuperClass().setType(DDDType.ENTITY);
          }
        });

    structure.getClasses().stream()
        .parallel()
        .forEachOrdered(item -> item.setType(structure));
  }

  private void analyzeInterfaces() {
    structure.getInterfaces().stream()
        .parallel()
        .forEach(item -> {
          item.setFields(driver);
          item.setMethods(driver);
          item.setImplInterfaces(driver, structure.getInterfaces());
          item.setAnnotations(driver, structure.getAnnotations());

          item.setType();
        });
  }

  private void analyzeEnums() {
    structure.getEnums().stream()
        .parallel()
        .forEach(item -> {
          item.setFields(driver);
          item.setAnnotations(driver, structure.getAnnotations());
        });
  }

  private void analyzeAnnotations() {
    structure.getAnnotations().stream()
        .parallel()
        .forEach(item -> {
          item.setFields(driver);
          item.setMethods(driver);
          item.setAnnotations(driver, structure.getAnnotations());
        });
  }

  private void setupDomains() {
    structure.getClasses().stream()
        .parallel()
        .forEach(item -> {
          if (item.isTypeOf(DDDType.ENTITY)
              || item.isTypeOf(DDDType.VALUE_OBJECT)
              || item.isTypeOf(DDDType.REPOSITORY)
              || item.isTypeOf(DDDType.FACTORY)) {
            addDomain(item);
          }
        });

    structure.getInterfaces().stream()
        .parallel()
        .forEach(item -> {
          if (item.isTypeOf(DDDType.REPOSITORY)
              || item.isTypeOf(DDDType.FACTORY)) {
            addDomain(item);
          }
        });

    structure.getEnums().stream()
        .parallel()
        .forEach(this::addDomain);
  }

  private void addDomain(Artifact item) {
    String[] split = item.getPath().split("[.]");
    String domain = split[split.length - 2];
    structure.addDomain(domain);
    item.setDomain(domain);
  }

  private void analyseDomains() {
    structure.getPackages().stream()
        .parallel()
        .forEach(item -> item.setAggregateRoot(structure));
  }

  private void findInfrastructure() {
    structure.getClasses().stream()
        .parallel()
        .forEach(artifact -> artifact.setInfrastructure(structure));
  }

  private void findEvents() {
    structure.getClasses().stream()
        .parallel()
        .forEach(Class::setDomainEvent);
  }
}
