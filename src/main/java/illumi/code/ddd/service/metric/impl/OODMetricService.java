package illumi.code.ddd.service.metric.impl;

import java.util.ArrayList;

import illumi.code.ddd.model.artifacts.*;

import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import org.json.JSONObject;

public class OODMetricService {

    private ArrayList<Package> packages;

    public OODMetricService(ArrayList<Package> packages) {
        this.packages = packages;
    }

    public JSONObject calculate() {
        JSONObject result = new JSONObject();

        packages.stream()
                .parallel()
                .forEachOrdered(module -> result.put(module.getPath(), analyseModule(module)));

        return result;
    }

    private JSONObject analyseModule(Package module) {
        Double abstractness = calculateAbstractness(module);
        Double instability = calculateInstability(module);
        Double distance = calculateDistance(abstractness, instability);

        if (abstractness != null) {
            return new JSONObject()
                    .put("abstractness", abstractness)
                    .put("instability", instability)
                    .put("distance", distance);
        }
        return null;
    }

    private Double calculateAbstractness(Package module) {
        long numberOfAbstracts = countNumberOfAbstracts(module);
        long numberOfClasses = countNumberOfClasses(module);
        if (numberOfClasses > 0) {
            double abstractness = (double) numberOfAbstracts / numberOfClasses;
            return Math.round(abstractness * 100.0) / 100.0;
        }
        return null;
    }

    private long countNumberOfAbstracts(Package module) {
        return module.getContains().stream()
                .filter(artifact -> artifact instanceof Interface).count();
    }

    private long countNumberOfClasses(Package module) {
        return module.getContains().stream()
                .filter(artifact -> artifact instanceof File).count();
    }

    private Double calculateInstability(Package module) {
        long numberOfAfferentCouplings = countAfferentCouplings(module);
        long numberOfEfferentCouplings = countEfferentCouplings(module);
        if (numberOfAfferentCouplings > 0
                || numberOfEfferentCouplings > 0) {
            double instability = (double) numberOfEfferentCouplings / (numberOfAfferentCouplings + numberOfEfferentCouplings);
            return Math.round(instability * 100.0) / 100.0;
        }
        return null;
    }

    private long countAfferentCouplings(Package module) {
        long ctr = 0;
        for (Package item : packages) {
            if (item != module) {
                ctr += item.getContains().stream()
                        .filter(artifact -> artifact instanceof File
                                && dependsOnModule(module, (File) artifact)).count();
            }
        }
        return ctr;
    }

    private boolean dependsOnModule(Package module, File artifact) {

        for (Interface implInterface : artifact.getImplInterfaces()) {
            if (module.getContains().contains(implInterface)) {
                return true;
            }
        }
        if (artifact instanceof Class) {
            return dependsOnModule(module, (Class) artifact);
        }
        return false;
    }

    private boolean dependsOnModule(Package module, Class artifact) {
        if (artifact.getSuperClass() != null
                && module.getContains().contains(artifact.getSuperClass())) {
            return true;
        }

        for (String dependency : artifact.getDependencies()) {
            if (dependency.contains(String.format(".%s.", module.getName()))) {
                return true;
            }
        }
        return false;
    }

    private long countEfferentCouplings(Package module) {
        long ctr = 0;

        for (Artifact artifact : module.getContains()) {
            if (artifact instanceof File) {
                for (Package item : packages) {
                    if (item != module && dependsOnModule(item, (File) artifact)) {
                        ctr++;
                        break;
                    }
                }
            }
        }

        return ctr;
    }

    private Double calculateDistance(Double abstractness, Double instability) {
        if (abstractness != null && instability != null) {
            return Math.round(Math.abs(abstractness + instability - 1.0) * 100.0) / 100.0;
        }
        return null;
    }
}
