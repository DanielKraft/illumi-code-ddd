package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RefactorServiceImplTest {

    private RefactorServiceImpl service;

    @BeforeAll
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

        Class root = new Class("Root", "de.test.Root");
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

        assertAll(	() -> assertEquals(13, result.getPackages().size(), "#Package"),
                    () -> assertEquals(4, result.getClasses().size(), "#Class"));
    }
}
