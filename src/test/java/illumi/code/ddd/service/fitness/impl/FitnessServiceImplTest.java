package illumi.code.ddd.service.fitness.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Annotation;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FitnessServiceImplTest {

  private FitnessServiceImpl service;

  @BeforeAll
  void init() {
    DDDStructure structure = new DDDStructure();
    structure.setPath("de.test");

    Package artifact1 = new Package("Package", "de.test.Package");
    artifact1.setType(DDDType.MODULE);
    structure.addPackage(artifact1);

    Class artifact2 = new Class("Class", "de.test.Class");
    artifact2.setType(DDDType.ENTITY);
    structure.addClass(artifact2);

    Interface artifact3 = new Interface("Interface", "de.test.Interface");
    artifact3.setType(DDDType.REPOSITORY);
    structure.addInterface(artifact3);

    Annotation artifact4 = new Annotation("Annotation", "de.test.Annotation");
    artifact4.setType(DDDType.INFRASTRUCTURE);
    structure.addAnnotation(artifact4);

    ArrayList<Artifact> data = new ArrayList<>();
    data.add(artifact1);
    data.add(artifact2);
    data.add(artifact3);
    data.add(artifact4);

    structure.setStructure(data);

    service = new FitnessServiceImpl();
    service.setStructure(structure);
  }

  @Test
  void testFitnessOfModules() {
    final JSONArray result = service.getStructureWithFitness();

    assertAll("Should return fitness of classes",
        () -> assertEquals(100.0, result.getJSONObject(0).getDouble("fitness")),
        () -> assertEquals(0.0, result.getJSONObject(1).getDouble("fitness")),
        () -> assertEquals(0.0, result.getJSONObject(2).getDouble("fitness")),
        () -> assertEquals(0.0, result.getJSONObject(3).getDouble("fitness")));
  }
}
