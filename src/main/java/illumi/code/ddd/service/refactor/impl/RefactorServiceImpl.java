package illumi.code.ddd.service.refactor.impl;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.artifacts.*;
import illumi.code.ddd.model.artifacts.Package;
import illumi.code.ddd.model.DDDRefactorData;
import illumi.code.ddd.service.refactor.RefactorService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class RefactorServiceImpl implements RefactorService {

    private DDDRefactorData refactorData;

    public @Inject
    RefactorServiceImpl() {
        // Empty
    }

    @Override
    public void setOldStructure(DDDStructure oldStructure) {
        this.refactorData = new DDDRefactorData(oldStructure);
    }

    @Override
    public DDDStructure refactor() {

        new InitializeService(refactorData).initModules();
        new AssignService(refactorData).assign();

        deleteEmptyModules(refactorData.getNewStructure().getStructure());
        return refactorData.getNewStructure();
    }

    private void deleteEmptyModules(List<Artifact> structure) {
        for (Artifact artifact : new ArrayList<>(structure)) {
            if (artifact instanceof Package) {
                if (((Package) artifact).getContains().isEmpty()) {
                    structure.remove(artifact);
                } else {
                    deleteEmptyModules(((Package) artifact).getContains());
                }
            }
        }
    }
}
