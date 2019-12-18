package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.File;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactoryRefactorService extends DefaultRefactorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FactoryRefactorService.class);

  FactoryRefactorService(DDDRefactorData refactorData) {
    super(refactorData);
  }

  @Override
  void refactor(Package model) {
    Package impl = (Package) model.getContains().get(0);

    model.getContains().stream()
        .parallel()
        .forEachOrdered(artifact -> {
          if (artifact instanceof Interface
              && artifact.isTypeOf(DDDType.FACTORY)) {
            refactorFactory(model, impl, (Interface) artifact);
          }
        });

    impl.getContains().stream()
        .parallel()
        .forEachOrdered(artifact -> {
          if (artifact instanceof Class
              && artifact.isTypeOf(DDDType.FACTORY)) {
            refactorFactory(model, (Class) artifact);
          }
        });
  }

  private void refactorFactory(Package model, Package impl, Interface factory) {
    if (!factory.getLowerName().contains(FACTORY.toLowerCase())) {
      factory.setName(factory.getName() + FACTORY);
      factory.setPath(factory.getPath() + FACTORY);
    }

    Class factoryImpl = getImpl(impl, factory, DDDType.FACTORY);
    refactorFactoryMethods(model, factory);

    if (factoryImpl != null) {
      refactorFactoryMethods(model, factoryImpl);
    } else {
      factoryImpl = createImpl(impl, factory, DDDType.FACTORY);
      impl.addContains(factoryImpl);
      getRefactorData().getNewStructure().addClass(factoryImpl);
      LOGGER.info(LOG_CREATE, FACTORY, factoryImpl.getPath());
    }
  }

  private void refactorFactory(Package model, Class factoryImpl) {
    if (!factoryImpl.getLowerName().contains(FACTORY_IMPL.toLowerCase())) {
      factoryImpl.setName(factoryImpl.getName() + FACTORY_IMPL);
      factoryImpl.setPath(factoryImpl.getPath() + FACTORY_IMPL);
    }

    refactorFactoryMethods(model, factoryImpl);

    if (factoryImpl.getImplInterfaces().isEmpty()) {
      Interface repository = createInterface(model, factoryImpl, DDDType.FACTORY);
      model.addContains(repository);
      getRefactorData().getNewStructure().addInterface(repository);
      LOGGER.info(LOG_CREATE, REPOSITORY, repository.getPath());
    } else {
      factoryImpl.getImplInterfaces().stream()
          .parallel()
          .forEachOrdered(repo -> refactorFactoryMethods(model, repo));
    }
  }

  private void refactorFactoryMethods(Package model, File repository) {
    Class entity = getEntity(model, repository);
    if (entity != null) {
      new ArrayList<>(repository.getDDDFitness().getIssues()).stream()
          .parallel()
          .forEachOrdered(issue -> {
            if (issue.getDescription().contains("no create")) {
              repository.addMethod(createMethod(entity.getPath(), "create", "..."));
            }
            repository.getDDDFitness().getIssues().remove(issue);
          });
    }
  }

}
