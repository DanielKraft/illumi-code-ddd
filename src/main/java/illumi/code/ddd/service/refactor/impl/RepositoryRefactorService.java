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

public class RepositoryRefactorService extends DefaultRefactorService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryRefactorService.class);

  RepositoryRefactorService(DDDRefactorData refactorData) {
    super(refactorData);
  }

  @Override
  void refactor(Package model) {
    Package impl = (Package) model.getContains().get(0);

    model.getContains().stream()
        .parallel()
        .forEachOrdered(artifact -> {
          if (artifact instanceof Interface
              && artifact.isTypeOf(DDDType.REPOSITORY)) {
            refactorRepository(model, impl, (Interface) artifact);
          }
        });

    impl.getContains().stream()
        .parallel()
        .forEachOrdered(artifact -> {
          if (artifact instanceof Class
              && artifact.isTypeOf(DDDType.REPOSITORY)) {
            refactorRepository(model, (Class) artifact);
          }
        });
  }

  private void refactorRepository(Package model, Package impl, Interface repository) {
    if (!repository.getName().toLowerCase().contains(REPOSITORY.toLowerCase())) {
      repository.setName(repository.getName() + REPOSITORY);
      repository.setPath(repository.getPath() + REPOSITORY);
    }

    Class repositoryImpl = getImpl(impl, repository, DDDType.REPOSITORY);
    refactorRepositoryMethods(model, repository);

    if (repositoryImpl != null) {
      refactorRepositoryMethods(model, repositoryImpl);
    } else {
      repositoryImpl = createImpl(impl, repository, DDDType.REPOSITORY);
      impl.addContains(repositoryImpl);
      getRefactorData().getNewStructure().addClass(repositoryImpl);
      LOGGER.info(LOG_CREATE, REPOSITORY, repositoryImpl.getPath());
    }
  }

  private void refactorRepository(Package model, Class repositoryImpl) {
    if (!repositoryImpl.getName().toLowerCase().contains(REPOSITORY_IMPL.toLowerCase())) {
      repositoryImpl.setName(repositoryImpl.getName() + REPOSITORY_IMPL);
      repositoryImpl.setPath(repositoryImpl.getPath() + REPOSITORY_IMPL);
    }

    refactorRepositoryMethods(model, repositoryImpl);

    if (repositoryImpl.getImplInterfaces().isEmpty()) {
      Interface repository = createInterface(model, repositoryImpl, DDDType.REPOSITORY);
      model.addContains(repository);
      getRefactorData().getNewStructure().addInterface(repository);
      LOGGER.info(LOG_CREATE, REPOSITORY, repository.getPath());
    } else {
      repositoryImpl.getImplInterfaces().stream()
          .parallel()
          .forEachOrdered(repo -> refactorRepositoryMethods(model, repo));
    }
  }

  private void refactorRepositoryMethods(Package model, File repository) {
    Class entity = getEntity(model, repository);
    if (entity != null) {
      String id = getIdOfEntity(entity);

      new ArrayList<>(repository.getDDDFitness().getIssues()).stream()
          .parallel()
          .forEachOrdered(issue -> {
            if (issue.getDescription().contains("no nextIdentity")) {
              repository.addMethod(createMethod(id, "nextIdentity", ""));
            } else if (issue.getDescription().contains("no findBy/get")) {
              repository.addMethod(createMethod(entity.getPath(), "findById", id));
            } else if (issue.getDescription().contains("no save/add/insert/put")) {
              repository.addMethod(createMethod("save", entity.getPath()));
            } else if (issue.getDescription().contains("no delete/remove")) {
              repository.addMethod(createMethod("delete", entity.getPath()));
            } else if (issue.getDescription().contains("no contains/exists")) {
              repository.addMethod(createMethod("java.lang.Boolean", "contains", entity.getPath()));
            } else if (issue.getDescription().contains("no update")) {
              repository.addMethod(createMethod("update", entity.getPath()));
            }
            repository.getDDDFitness().getIssues().remove(issue);
          });
    }
  }
}
