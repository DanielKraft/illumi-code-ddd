package illumi.code.ddd.service.analyse.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.Structure;

import java.util.ArrayList;

public class PackageAnalyseService {
    private Package module;
    private Structure structure;

    public PackageAnalyseService(Package module, Structure structure) {
        this.module = module;
        this.structure = structure;
    }

    public void setAggregateRoot() {
        if (isDomain()) {
            ArrayList<Class> candidates = getAggregateRootCandidates();
            if (candidates.size() == 1) {
                candidates.get(0).setType(DDDType.AGGREGATE_ROOT);
            } else {
                for (Class artifact : candidates) {
                    if (structure.getDomains().contains(artifact.getName().toLowerCase())) {
                        artifact.setType(DDDType.AGGREGATE_ROOT);
                    }
                }
            }
        }
    }

    private boolean isDomain() {
        return structure.getDomains().contains(module.getName());
    }

    private ArrayList<Class> getAggregateRootCandidates() {
        ArrayList<Class> entities = getEntities();
        setUsed(entities);
        if (!entities.isEmpty()) {
            return getEntityWithMinimalDependencies(entities);
        } else {
            return new ArrayList<>();
        }
    }

    private ArrayList<Class> getEntities() {
        ArrayList<Class> entities = new ArrayList<>();

        for (Artifact artifact : module.getContains()) {
            if (artifact.isTypeOf(DDDType.ENTITY)) {
                entities.add((Class) artifact);
            }
        }

        return entities;
    }

    private void setUsed(ArrayList<Class> entities) {
        entities.stream()
            .parallel()
            .forEach(artifact -> {
                for (Class entity: entities) {
                    if (entity != artifact && entity.getDependencies().contains(artifact.getName())) {
                        artifact.addUsed(entity.getName());
                    }
                }
            });
    }

    private ArrayList<Class> getEntityWithMinimalDependencies(ArrayList<Class> entities) {
        ArrayList<Class> result = new ArrayList<>();
        result.add(entities.get(0));

        int highscore = entities.get(0).getUsed().size();

        for (int i = 1; i < entities.size(); i++) {
            if (entities.get(i).getUsed().size() == highscore) {
                result.add(entities.get(i));
            } else if (entities.get(i).getUsed().size() < highscore) {
                highscore = entities.get(i).getUsed().size();
                result = new ArrayList<>();
                result.add(entities.get(i));
            }
        }

        return result;
    }
}
