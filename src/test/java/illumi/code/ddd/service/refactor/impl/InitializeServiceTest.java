package illumi.code.ddd.service.refactor.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InitializeServiceTest {

  private InitializeService service;
  private DDDRefactorData refactorData;

  @BeforeEach
  void init() {
    DDDStructure structure = new DDDStructure();
    Package module = new Package("test", "de.test");
    structure.addPackage(module);

    Class entity = new Class("Entity", "de.test.Entity");
    entity.setType(DDDType.ENTITY);
    module.addContains(entity);
    structure.addClass(entity);

    Class root = new Class("Root", "de.test.Root");
    root.setType(DDDType.AGGREGATE_ROOT);
    module.addContains(root);
    structure.addClass(root);

    ArrayList<Artifact> data = new ArrayList<>();
    data.add(module);
    structure.setStructure(data);

    refactorData = new DDDRefactorData(structure);
    service = new InitializeService(refactorData);
  }

  @Test
  void testInitModules() {
    service.initModules();

    assertAll(
        () -> assertEquals(9, refactorData.getNewStructure().getPackages().size(), "#Package"),
        () -> assertEquals(1, refactorData.getNewStructure().getClasses().size(), "#Class"));
  }
}
