package illumi.code.ddd.service.metric.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Package;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OODMetricServiceTest {
    private ArrayList<Package> packages;
    private OODMetricService service;
    private Package module1;
    private Class entity;

    @BeforeEach
    void init() {
        packages = new ArrayList<>();
        module1 = new Package("domain1", "de.domain1");
        module1.setType(DDDType.MODULE);
        packages.add(module1);
        Class superClass = new Class("Super", "de.domain1.Super");
        superClass.setType(DDDType.ENTITY);
        module1.addContains(superClass);

        Package module2 = new Package("domain2", "de.domain2");
        module2.setType(DDDType.MODULE);
        packages.add(module2);
        entity = new Class("Entity", "de.domain2.Entity");
        entity.setType(DDDType.ENTITY);
        entity.setSuperClass(superClass);
        module2.addContains(entity);
        Package module3 = new Package("test", "de.domain2.test");
        module3.setType(DDDType.MODULE);
        packages.add(module3);
        module2.addContains(module3);
    }

    @Test
    void testOODMetric() {
        service = new OODMetricService(packages);

        final JSONObject result = service.calculate();

        final JSONObject expected = new JSONObject()
                .put("distance", new JSONObject()
                    .put("avg", 0.5)
                    .put("median", 0.5)
                    .put("standard deviation", 0.71)
                    .put("min", 0)
                    .put("max", 1))
                .put("module", new JSONObject()
                    .put("de.domain1", new JSONObject()
                            .put("abstractness", 0.0)
                            .put("instability", 0.0)
                            .put("distance", 1.0))
                    .put("de.domain2", new JSONObject()
                            .put("abstractness", 0.0)
                            .put("instability", 1.0)
                            .put("distance", 0.0)));

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void testOODMetricWithInterface() {
        Interface repo = new Interface("Repository", "de.domain1.Repository");
        repo.setType(DDDType.REPOSITORY);
        module1.addContains(repo);

        entity.addImplInterface(repo);

        service = new OODMetricService(packages);

        final JSONObject result = service.calculate();

        final JSONObject expected = new JSONObject()
                .put("distance", new JSONObject()
                    .put("avg", 0.25)
                    .put("median", 0.25)
                    .put("standard deviation", 0.35)
                    .put("min", 0)
                    .put("max", 0.5))
                .put("module", new JSONObject()
                    .put("de.domain1", new JSONObject()
                            .put("abstractness", 0.5)
                            .put("instability", 0.0)
                            .put("distance", 0.5))
                    .put("de.domain2", new JSONObject()
                            .put("abstractness", 0.0)
                            .put("instability", 1.0)
                            .put("distance", 0.0)));

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void testOODMetricWithDependency() {
        entity.setSuperClass(null);
        entity.addDependencies("de.domain1.Super");

        service = new OODMetricService(packages);

        final JSONObject result = service.calculate();

        final JSONObject expected = new JSONObject()
                .put("distance", new JSONObject()
                    .put("avg", 0.5)
                    .put("median", 0.5)
                    .put("standard deviation", 0.71)
                    .put("min", 0)
                    .put("max", 1))
                .put("module", new JSONObject()
                    .put("de.domain1", new JSONObject()
                            .put("abstractness", 0.0)
                            .put("instability", 0.0)
                            .put("distance", 1.0))
                    .put("de.domain2", new JSONObject()
                            .put("abstractness", 0.0)
                            .put("instability", 1.0)
                            .put("distance", 0.0)));
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void testOODMetricWithoutArtifacts() {
        packages = new ArrayList<>();
        packages.add(new Package("test", "de.test"));

        service = new OODMetricService(packages);

        final JSONObject result = service.calculate();

        final JSONObject expected = new JSONObject();

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void testOODMetricWithoutDependencies() {
        packages = new ArrayList<>();
        module1 = new Package("test", "de.test");
        entity = new Class("Entity", "de.test.Entity");
        entity.setType(DDDType.ENTITY);
        module1.addContains(entity);
        packages.add(module1);

        service = new OODMetricService(packages);

        final JSONObject result = service.calculate();

        final JSONObject expected = new JSONObject()
                .put("module", new JSONObject()
                    .put("de.test", new JSONObject().put("abstractness", 0.0)));

        assertEquals(expected.toString(), result.toString());
    }
}