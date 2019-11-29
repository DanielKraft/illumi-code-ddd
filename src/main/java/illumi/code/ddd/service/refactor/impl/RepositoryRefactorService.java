package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class RepositoryRefactorService extends DefaultRefactorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryRefactorService.class);
    public static final String REPOSITORY_IMPL = "RepositoryImpl";
    public static final String REPOSITORY = "Repository";

    RepositoryRefactorService(DDDRefactorData refactorData) {
        super(refactorData);
    }

    @Override
    void refactor(Package model) {
        Package impl = (Package) model.getContains().get(0);

        model.getContains().stream()
                .parallel()
                .forEach(artifact -> {
                    if (artifact instanceof Interface
                            && artifact.isTypeOf(DDDType.REPOSITORY)) {
                        refactorRepository(model, impl, (Interface) artifact);
                    }
                });

        impl.getContains().stream()
                .parallel()
                .forEach(artifact -> {
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

        Class repositoryImpl = getImpl(impl, repository);
        refactorRepositoryMethods(model, repository);

        if (repositoryImpl != null) {
            refactorRepositoryMethods(model, repositoryImpl);
        } else {
            repositoryImpl = createRepositoryImpl(impl, repository);
            impl.addContains(repositoryImpl);
            getRefactorData().getNewStructure().addClass(repositoryImpl);
            LOGGER.info(LOG_CREATE, repositoryImpl.getPath());
        }
    }

    private Class getImpl(Package impl, Interface repository) {
        for (Artifact item : impl.getContains()) {
            if (item instanceof Class
                    && item.isTypeOf(DDDType.REPOSITORY)
                    && item.getName().toLowerCase().contains(repository.getName().toLowerCase())) {
                return (Class) item;
            }
        }
        return null;
    }

    private Class createRepositoryImpl(Package impl, Interface repository) {
        String name = String.format("%sImpl", repository.getName());
        String path = String.format("%s.%s", impl.getPath(), name);
        Class repositoryImpl = new Class(name, path);
        repositoryImpl.setType(DDDType.REPOSITORY);
        repositoryImpl.setDomain(repository.getDomain());

        repositoryImpl.addImplInterface(repository);

        copyMethods(repository, repositoryImpl);

        return repositoryImpl;
    }

    private void refactorRepository(Package model, Class repositoryImpl) {
        if (!repositoryImpl.getName().toLowerCase().contains(REPOSITORY_IMPL.toLowerCase())) {
            repositoryImpl.setName(repositoryImpl.getName() + REPOSITORY_IMPL);
            repositoryImpl.setPath(repositoryImpl.getPath() + REPOSITORY_IMPL);
        }

        refactorRepositoryMethods(model, repositoryImpl);

        if (repositoryImpl.getImplInterfaces().isEmpty()) {
            Interface repository = createRepository(model, repositoryImpl);
            model.addContains(repository);
            getRefactorData().getNewStructure().addInterface(repository);
            LOGGER.info(LOG_CREATE, repository.getPath());
        } else {
            repositoryImpl.getImplInterfaces().stream()
                    .parallel()
                    .forEach(repo -> refactorRepositoryMethods(model, repo));
        }
    }

    private Interface createRepository(Package model, Class repositoryImpl) {
        String name = repositoryImpl.getName().replace("Impl", "");
        String path = String.format("%s.%s", model.getPath(), name);
        Interface repository = new Interface(name, path);
        repository.setType(DDDType.REPOSITORY);
        repository.setDomain(repositoryImpl.getDomain());

        repository.addImplInterface(repository);

        copyMethods(repositoryImpl, repository);

        return repository;
    }

    private void copyMethods(File oldRepo, File newRepo) {
        oldRepo.getMethods().stream()
                .parallel()
                .forEach(method -> newRepo.addMethod(new Method(method)));
    }

    private void refactorRepositoryMethods(Package model, File repository) {
        Class entity = getEntityOfRepository(model, repository);
        if (entity != null) {
            String id = getIdOfEntity(entity);

            new ArrayList<>(repository.getDDDFitness().getIssues()).stream()
                    .parallel()
                    .forEach(issue -> {
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

    private Class getEntityOfRepository(Package model, File repository) {
        for (Artifact artifact : model.getContains()) {
            if (artifact instanceof Class
                    && repository.getName().toLowerCase().contains(artifact.getName().toLowerCase())) {
                return (Class) artifact;
            }
        }
        return null;
    }

    private String getIdOfEntity(Class entity) {
        for (Field field : entity.getFields()) {
            if (Field.isId(field)) {
                return field.getType();
            }
        }

        if (entity.getSuperClass() != null) {
            return getIdOfEntity(entity.getSuperClass());
        }

        return "";
    }

    private Method createMethod(String name, String attribute) {
        return createMethod("void", name, attribute);
    }

    private Method createMethod(String value, String name, String attribute) {
        String signature = String.format("%s %s(%s)", value, name, attribute);
        LOGGER.info(LOG_CREATE, signature);
        return new Method(PUBLIC, name, signature);
    }
}
