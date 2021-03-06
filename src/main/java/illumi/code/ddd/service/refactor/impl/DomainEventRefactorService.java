package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Field;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;

public class DomainEventRefactorService extends DefaultRefactorService {

  DomainEventRefactorService(DDDRefactorData refactorData) {
    super(refactorData);
  }

  @Override
  void refactor(Package model) {
    for (Artifact artifact : new ArrayList<>(model.getContains())) {
      if (artifact instanceof Class
          && artifact.isTypeOf(DDDType.DOMAIN_EVENT)) {
        refactorDomainEvent(model, (Class) artifact);
      }
    }
  }

  private void refactorDomainEvent(Package model, Class artifact) {
    for (Field field : new ArrayList<>(artifact.getFields())) {
      if (Field.isId(field)) {
        refactorIdPath(model, field);
      }

      if (needsGetter(artifact, field)) {
        artifact.addMethod(createGetter(field));
      }

      if (needsSideEffectFreeSetter(artifact, field)) {
        artifact.addMethod(createSideEffectFreeSetter(field));
      }
    }
  }

  private void refactorIdPath(Package model, Field field) {
    for (Artifact artifact : model.getContains()) {
      if (artifact instanceof Class
          && (artifact.isTypeOf(DDDType.ENTITY) || artifact.isTypeOf(DDDType.AGGREGATE_ROOT))
          && field.getLowerName().startsWith(artifact.getLowerName())) {
        field.setType(getIdOfEntity((Class) artifact));
      }
    }
  }
}
