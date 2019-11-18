package illumi.code.ddd.service.analyse;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.service.StructureService;

import java.util.ArrayList;

public class PackageAnalyseService {
    private Package module;
    private StructureService structureService;

    public PackageAnalyseService(Package module, StructureService structureService) {
        this.module = module;
        this.structureService = structureService;
    }

    public void setAggregateRoot() {
        if (isDomain()) {
            ArrayList<Artifact> candidates = getAggregateRootCandidates();
            if (candidates.size() == 1) {
                candidates.get(0).setType(DDDType.AGGREGATE_ROOT);
            } else {
                for (Artifact artifact : candidates) {
                    if (structureService.getDomains().contains(artifact.getName().toLowerCase())) {
                        artifact.setType(DDDType.AGGREGATE_ROOT);
                    }
                }
            }
        }
    }

    private boolean isDomain() {
        return structureService.getDomains().contains(module.getName());
    }

    private ArrayList<Artifact> getEntities() {
        ArrayList<Artifact> entities = new ArrayList<>();

        for (Artifact artifact : module.getConataints()) {
            if (artifact.isTypeOf(DDDType.ENTITY)) {
                entities.add(artifact);
            }
        }

        return entities;
    }

    private ArrayList<Artifact> getAggregateRootCandidates() {
        ArrayList<Artifact> entities = getEntities();
        if (!entities.isEmpty()) {
            ArrayList<Integer> dependencies = getDependencies(entities);
            return getEntityWithMinmalDependencies(entities, dependencies);
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<Integer> getDependencies(ArrayList<Artifact> entities) {
        ArrayList<Integer> dependencies = new ArrayList<>();
        entities.stream()
                .parallel()
                .forEachOrdered(artifact -> dependencies.add(countDependencies(entities, artifact)));
        return dependencies;
    }

    private Integer countDependencies(ArrayList<Artifact> entities, Artifact artifact) {
        int ctr = 0;
        for (Artifact entity : entities) {
            if (entity != artifact) {
                for (Field field : ((Class) entity).getFields()) {
                    if (field.getType().equals(artifact.getPath())) {
                        ctr++;
                    }
                }
            }
        }
        return ctr;
    }

    private ArrayList<Artifact> getEntityWithMinmalDependencies(ArrayList<Artifact> entities, ArrayList<Integer> dependencies) {
        ArrayList<Artifact> result = new ArrayList<>();
        result.add(entities.get(0));

        int highscore = dependencies.get(0);
        for (int i = 1; i < dependencies.size(); i++) {
            if (dependencies.get(i) == highscore) {
                result.add(entities.get(i));
            } else if (dependencies.get(i) < highscore) {
                highscore = dependencies.get(i);
                result = new ArrayList<>();
                result.add(entities.get(i));
            }
        }
        return result;
    }
}
