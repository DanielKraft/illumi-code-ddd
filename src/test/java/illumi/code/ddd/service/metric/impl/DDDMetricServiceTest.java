package illumi.code.ddd.service.metric.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.fitness.DDDIssueType;
import illumi.code.ddd.model.fitness.DDDRating;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DDDMetricServiceTest {
    private ArrayList<Artifact> artifacts;
    private DDDMetricService service;
    private Class entity;

    @BeforeEach
    void init() {
        artifacts = new ArrayList<>();
        Class superClass = new Class("Super", "de.Super");
        superClass.setType(DDDType.ENTITY);
        artifacts.add(superClass);

        entity = new Class("Entity", "de.Entity");
        entity.setType(DDDType.ENTITY);
        entity.setSuperClass(superClass);
        artifacts.add(entity);

        Package module = new Package("test", "de.test");
        module.setType(DDDType.MODULE);
        artifacts.add(module);
    }

    @Test
    void testWithArtifacts() {
        service = new DDDMetricService(artifacts);

        final JSONObject result = service.calculate();

        final JSONObject expected = new JSONObject()
                .put("metric", new JSONObject()
                        .put("score", DDDRating.A)
                        .put("fitness", 100.0)
                        .put("statistic", new JSONObject()
                                .put("avg", 100)
                                .put("median", 100)
                                .put("standard deviation", 0)
                                .put("min", 100)
                                .put("max", 100))
                        .put("#Issues", 0))
                .put("artifact", new JSONObject()
                        .put("#MODULE", 				1)
                        .put("#ENTITY", 				2))
                .put("hotspot", new JSONArray());

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void testWithIssues() {
        DDDFitness fitness = new DDDFitness(2, 2);
        fitness.addFailedCriteria(DDDIssueType.MAJOR, "Description");
        entity.setFitness(fitness);

        service = new DDDMetricService(artifacts);

        final JSONObject result = service.calculate();

        final JSONObject expected = new JSONObject()
                .put("metric", new JSONObject()
                    .put("score", DDDRating.E)
                    .put("fitness", 40.0)
                    .put("statistic", new JSONObject()
                            .put("avg", 80)
                            .put("median", 100)
                            .put("standard deviation", 34.64)
                            .put("min", 40)
                            .put("max", 100))
                    .put("#Issues", 1))
                .put("artifact", new JSONObject()
                    .put("#MODULE", 				1)
                    .put("#ENTITY", 				2))
                .put("hotspot", new JSONArray()
                    .put(new JSONObject()
                        .put("name", "Entity")
                        .put("DDD", DDDType.ENTITY)
                        .put("fitness", 40.0)
                        .put("issues", new JSONArray()
                            .put("[MAJOR] Description"))));

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void testWithBadFitness() {
        DDDFitness fitness = new DDDFitness(2, 1);
        entity.setFitness(fitness);

        service = new DDDMetricService(artifacts);

        final JSONObject result = service.calculate();

        final JSONObject expected = new JSONObject()
                .put("metric", new JSONObject()
                        .put("score", DDDRating.D)
                        .put("fitness", 50.0)
                        .put("statistic", new JSONObject()
                            .put("avg", 83.33)
                            .put("median", 100)
                            .put("standard deviation", 28.87)
                            .put("min", 50)
                            .put("max", 100))
                        .put("#Issues", 0))
                .put("artifact", new JSONObject()
                        .put("#MODULE", 				1)
                        .put("#ENTITY", 				2))
                .put("hotspot", new JSONArray()
                        .put(new JSONObject()
                                .put("name", "Entity")
                                .put("DDD", DDDType.ENTITY)
                                .put("fitness", 50.0)
                                .put("issues", new JSONArray())));

        assertEquals(expected.toString(), result.toString());
    }

    @Test
    void testEmptyStructure() {
        service = new DDDMetricService(new ArrayList<>());

        final JSONObject result = service.calculate();

        final JSONObject expected = new JSONObject()
                .put("metric", new JSONObject()
                    .put("score", DDDRating.A)
                    .put("fitness", 100.0)
                    .put("#Issues", 0))
                .put("artifact", new JSONObject())
                .put("hotspot", new JSONArray());

        assertEquals(expected.toString(), result.toString());
    }
}