package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssignServiceTest {

    private DDDStructure structure;
    private AssignService service;
    private DDDRefactorData refactorData;

    private Package module;

    @BeforeEach
    void init() {
        structure = new DDDStructure();
        structure.setPath("de.test");

        module = new Package("test", "de.test");
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

        refactorData = new DDDRefactorData(structure);

        new InitializeService(refactorData).initModules();

        service = new AssignService(refactorData);
    }

    @Test
    void testAssignApplicationService() {
        Class artifact = new Class("ApplicationService", "de.test.ApplicationService");
        artifact.setType(DDDType.APPLICATION_SERVICE);
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                    () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
                    () -> assertEquals("test.application.ApplicationService", artifact.getPath(), "path"));
    }

    @Test
    void testAssignInfrastructure() {
        Class artifact = new Class("Infrastructure", "de.test.Infrastructure");
        artifact.setType(DDDType.INFRASTRUCTURE);
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                    () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
                    () -> assertEquals("test.infrastructure.Infrastructure", artifact.getPath(), "path"));
    }

    @Test
    void testAssignService() {
        Class artifact = new Class("Domain0Service", "de.test.Domain0Service");
        artifact.setType(DDDType.SERVICE);
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                    () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
                    () -> assertEquals("test.application.root.Domain0Service", artifact.getPath(), "path"));
    }

    @Test
    void testAssignRepository() {
        Class artifact = new Class("Repository", "de.test.domain0.Repository");
        artifact.setType(DDDType.REPOSITORY);
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                    () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
                    () -> assertEquals("test.domain.root.model.impl.Repository", artifact.getPath(), "path"));
    }

    @Test
    void testAssignEntity() {
        Class artifact = new Class("Entity", "de.test.Entity");
        artifact.setType(DDDType.ENTITY);
        artifact.setDomain("domain0");
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
                () -> assertEquals("test.domain.root.model.Entity", artifact.getPath(), "path"));
    }

    @Test
    void testAssignModel() {
        Class artifact = new Class("Entity", "de.test.Entity");
        artifact.setType(DDDType.ENTITY);
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                    () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
                    () -> assertEquals("test.domain.model.Entity", artifact.getPath(), "path"));
    }

    @Test
    void testAssignWithDependency() {
        Class artifact = new Class("Entity", "de.test.Entity");
        artifact.setType(DDDType.ENTITY);
        artifact.addDependencies("Test");
        artifact.addDependencies("Root");
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                    () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
                    () -> assertEquals("test.domain.root.model.Entity", artifact.getPath(), "path"));
    }

    @Test
    void testAssignModelWithDependency() {
        Class model = new Class("Model", "de.test.Model");
        model.setType(DDDType.ENTITY);
        module.addContains(model);
        structure.addClass(model);

        Class artifact = new Class("Entity", "de.test.Entity");
        artifact.setType(DDDType.ENTITY);
        artifact.addDependencies("Model");
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                () -> assertEquals(4, refactorData.getNewStructure().getClasses().size(), "#Class"),
                () -> assertEquals("test.domain.model.Entity", artifact.getPath(), "path"));
    }

    @Test
    void testAssignWithFields() {
        Class artifact = new Class("Entity", "de.test.Entity");
        artifact.setType(DDDType.ENTITY);
        artifact.addField(new Field("private", "test", "de.Other"));
        artifact.addField(new Field("private", "rootId", "de.Int"));
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                    () -> assertEquals(3, refactorData.getNewStructure().getClasses().size(), "#Class"),
                    () -> assertEquals("test.domain.root.model.Entity", artifact.getPath(), "path"));
    }

    @Test
    void testAssignModelWithFields() {
        Class model = new Class("Model", "de.test.Model");
        model.setType(DDDType.ENTITY);
        module.addContains(model);
        structure.addClass(model);

        Class artifact = new Class("Entity", "de.test.Entity");
        artifact.setType(DDDType.ENTITY);
        artifact.addField(new Field("private", "modelId", "de.test.Model"));
        module.addContains(artifact);
        structure.addClass(artifact);

        service.assign();

        assertAll(	() -> assertEquals(12, refactorData.getNewStructure().getPackages().size(), "#Package"),
                () -> assertEquals(4, refactorData.getNewStructure().getClasses().size(), "#Class"),
                () -> assertEquals("test.domain.model.Entity", artifact.getPath(), "path"));
    }
}
