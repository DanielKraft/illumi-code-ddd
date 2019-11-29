package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityRefactorServiceTest {

    private DDDStructure structure;
    private EntityRefactorService service;
    private DDDRefactorData refactorData;

    private Package module;

    @BeforeEach
    void init() {
        structure = new DDDStructure();
        structure.setPath("de.test");

        module = new Package("test", "de.test");
        module.setType(DDDType.MODULE);

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

        Interface repo = new Interface("Repo", "de.test.Repo");
        repo.setType(DDDType.REPOSITORY);
        refactorData.getModelModule().addContains(repo);

        Class repoImpl = new Class("RepoImpl", "de.test.RepoImpl");
        repoImpl.setType(DDDType.REPOSITORY);
        refactorData.getModelModule().addContains(repoImpl);

        refactorData.getDomainModule().addContains(repo);

        service = new EntityRefactorService(refactorData);
    }

    @Test
    void testRefactor() {
        Class name = new Class("NameEntity", "de.test.NameEntity");
        name.setType(DDDType.VALUE_OBJECT);
        refactorData.getNewStructure().addClass(name);
        refactorData.getModelModule().addContains(name);

        Class entity = new Class("Entity", "de.test.Entity");
        entity.setType(DDDType.ENTITY);
        entity.addField(new Field("private", "id", "de.test.EntityId"));
        entity.addField(new Field("private", "entities", "de.test.List"));
        entity.addMethod(new Method("public", "equals", "java.lang.Boolean equals(Object)"));
        entity.addMethod(new Method("public", "hashCode", "java.lang.Integer hashCode()"));
        entity.addMethod(new Method("public", "getId", "de.test.EntityId getId()"));
        entity.addMethod(new Method("public", "getNames", "de.test.List getName()"));
        entity.addMethod(new Method("public", "setNames", "void setName(de.test.List)"));
        refactorData.getNewStructure().addClass(entity);
        refactorData.getModelModule().addContains(entity);

        service.refactor();

        assertAll(  () -> assertEquals(2, entity.getFields().size(), "#Field"),
                () -> assertEquals(7, entity.getMethods().size(), "#Method"));
    }

    @Test
    void testRefactorEntity() {
        Class id = new Class("EntityId", "de.test.EntityId");
        id.setType(DDDType.VALUE_OBJECT);
        refactorData.getNewStructure().addClass(id);
        refactorData.getModelModule().addContains(id);

        Class name = new Class("Name", "de.test.Name");
        name.setType(DDDType.VALUE_OBJECT);
        refactorData.getNewStructure().addClass(name);
        refactorData.getModelModule().addContains(name);

        Class entity = new Class("Entity", "de.test.Entity");
        entity.setType(DDDType.ENTITY);
        entity.addField(new Field("private", "id", "de.test.EntityId"));
        entity.addField(new Field("private", "names", "de.test.List"));
        entity.addMethod(new Method("public", "equals", "java.lang.Boolean equals(Object)"));
        entity.addMethod(new Method("public", "hashCode", "java.lang.Integer hashCode()"));
        entity.addMethod(new Method("public", "getId", "de.test.EntityId getId()"));
        entity.addMethod(new Method("public", "getNames", "de.test.List getName()"));
        entity.addMethod(new Method("public", "setNames", "void setName(de.test.List)"));
        refactorData.getNewStructure().addClass(entity);
        refactorData.getModelModule().addContains(entity);

        service.refactor();

        assertAll(  () -> assertEquals(2, entity.getFields().size(), "#Field"),
                    () -> assertEquals(5, entity.getMethods().size(), "#Method"));
    }

    @Test
    void testRefactorInvalidEntity() {
        Class entity = new Class("Entity", "de.test.Entity");
        entity.setType(DDDType.ENTITY);
        entity.addField(new Field("private", "attribute", "String"));

        refactorData.getNewStructure().addClass(entity);
        refactorData.getModelModule().addContains(entity);

        service.refactor();

        assertAll(  () -> assertEquals(2, entity.getFields().size(), "#Field"),
                    () -> assertEquals(5, entity.getMethods().size(), "#Method"));
    }

    @Test
    void testRefactorEntityWithId() {
        Class superClass = new Class("Super", "de.test.Super");
        superClass.setType(DDDType.ENTITY);
        superClass.addField(new Field("private", "id", "java.lang.long"));

        Class entity = new Class("Entity", "de.test.Entity");
        entity.setType(DDDType.ENTITY);
        entity.setSuperClass(superClass);
        entity.addField(new Field("private", "attribute", "String"));

        refactorData.getNewStructure().addClass(entity);
        refactorData.getModelModule().addContains(entity);

        service.refactor();

        assertAll(  () -> assertEquals(1, entity.getFields().size(), "#Field"),
                    () -> assertEquals(4, entity.getMethods().size(), "#Method"));
    }

    @Test
    void testRefactorEntityToValueObject() {
        Class entity = new Class("EntityName", "de.test.EntityName");
        entity.setType(DDDType.ENTITY);
        entity.addField(new Field("private", "name", "java.lang.String"));
        entity.addField(new Field("private", "attribute", "String"));

        refactorData.getNewStructure().addClass(entity);
        refactorData.getModelModule().addContains(entity);

        service.refactor();

        assertEquals(DDDType.VALUE_OBJECT, entity.getType());
    }

    @Test
    void testRefactorEntityToInvalidValueObject() {
        Class entity = new Class("EntityName", "de.test.EntityName");
        entity.setType(DDDType.ENTITY);
        entity.addField(new Field("private", "name", "String"));
        entity.addField(new Field("private", "attribute", "String"));

        refactorData.getNewStructure().addClass(entity);
        refactorData.getModelModule().addContains(entity);

        service.refactor();

        assertEquals(DDDType.ENTITY, entity.getType());
    }
}