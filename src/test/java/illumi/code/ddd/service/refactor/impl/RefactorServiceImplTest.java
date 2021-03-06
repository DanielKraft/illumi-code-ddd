package illumi.code.ddd.service.refactor.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RefactorServiceImplTest {

  private RefactorServiceImpl service;
  private Class root;

  @BeforeEach
  void init() {
    service = new RefactorServiceImpl();

    DDDStructure structure = new DDDStructure();
    structure.setPath("de.test");

    Package module = new Package("test", "de.test");
    module.setType(DDDType.MODULE);

    Class root2 = new Class("Root2", "de.Root2");
    root2.setType(DDDType.AGGREGATE_ROOT);
    root2.setDomain("domain1");
    module.addContains(root2);
    structure.addClass(root2);

    root = new Class("Root", "de.test.Root");
    root.setType(DDDType.AGGREGATE_ROOT);
    root.setDomain("domain0");
    module.addContains(root);
    structure.addClass(root);

    ArrayList<Artifact> data = new ArrayList<>();
    data.add(module);
    structure.setStructure(data);

    service.setOldStructure(structure);
  }

  @Test
  void testRefactor() {
    final DDDStructure result = service.refactor();

    assertAll(() -> assertEquals(13, result.getPackages().size(), "#Package"),
        () -> assertEquals(8, result.getClasses().size(), "#Class"));
  }

  @Test
  void testRefactorInvalidDependency() {
    root.addDependencies("de.test.Class");

    final DDDStructure result = service.refactor();

    assertAll(() -> assertEquals(13, result.getPackages().size(), "#Package"),
        () -> assertEquals(8, result.getClasses().size(), "#Class"));
  }
}
