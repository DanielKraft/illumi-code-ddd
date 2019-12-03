package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class AggregateRootRefactorService extends DefaultRefactorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateRootRefactorService.class);

    AggregateRootRefactorService(DDDRefactorData refactorData) {
        super(refactorData);
    }

    @Override
    void refactor(Package model) {
        Package impl = (Package) model.getContains().get(0);

        new ArrayList<>(model.getContains()).stream()
                .parallel()
                .forEachOrdered(artifact -> {
                    if (artifact instanceof Class
                            && artifact.isTypeOf(DDDType.AGGREGATE_ROOT)) {
                        refactorAggregateRoot(model, impl, (Class) artifact);
                    }
                });
    }

    private void refactorAggregateRoot(Package model, Package impl, Class root) {
        Interface repository = getInterface(DDDType.REPOSITORY, model, root);
        if (repository == null) {
            repository = createRepository(model, root);
            Class repositoryImpl = createImpl(impl, repository, DDDType.REPOSITORY);
            impl.addContains(repositoryImpl);
            getRefactorData().getNewStructure().addClass(repositoryImpl);
            LOGGER.info(LOG_CREATE, REPOSITORY, repositoryImpl.getPath());
        }

        Interface factory = getInterface(DDDType.FACTORY, model, root);
        if (factory == null) {
            factory = createFactory(model, root);
            Class factoryImpl = createImpl(impl, factory, DDDType.FACTORY);
            factoryImpl.addField(new Field("private", "repository", repository.getPath()));
            impl.addContains(factoryImpl);
            getRefactorData().getNewStructure().addClass(factoryImpl);
            LOGGER.info(LOG_CREATE, FACTORY, factoryImpl.getPath());
        }
    }

    private Interface getInterface(DDDType dddType, Package model, Class root) {
       for (Artifact artifact : model.getContains()) {
            if (artifact.isTypeOf(dddType)
                    && artifact.getName().toLowerCase().contains(root.getName().toLowerCase())) {
                return (Interface) artifact;
            }
        }
        return null;
    }

    private Interface createRepository(Package model, Class root) {
        String name = root.getName() + REPOSITORY;
        String path = String.format("%s.%s", model.getPath(), name);

        Interface repository = new Interface(name, path);
        repository.setType(DDDType.REPOSITORY);
        repository.setDomain(root.getDomain());

        String id = getIdOfEntity(root);

        repository.addMethod(createMethod(id, "nextIdentity", ""));
        repository.addMethod(createMethod(root.getPath(), "findById", id));
        repository.addMethod(createMethod("save", root.getPath()));
        repository.addMethod(createMethod("delete", root.getPath()));
        repository.addMethod(createMethod("java.lang.Boolean", "contains", root.getPath()));
        repository.addMethod(createMethod("update", root.getPath()));

        model.addContains(repository);
        getRefactorData().getNewStructure().addInterface(repository);

        LOGGER.info(LOG_CREATE, REPOSITORY, repository.getPath());
        return repository;
    }

    private Interface createFactory(Package model, Class root) {
        String name = root.getName() + FACTORY;
        String path = String.format("%s.%s", model.getPath(), name);

        Interface factory = new Interface(name, path);
        factory.setType(DDDType.FACTORY);
        factory.setDomain(root.getDomain());

        factory.addMethod(createMethod(root.getPath(), "create", "..."));

        model.addContains(factory);
        getRefactorData().getNewStructure().addInterface(factory);

        LOGGER.info(LOG_CREATE, FACTORY, factory.getPath());
        return factory;
    }
}
