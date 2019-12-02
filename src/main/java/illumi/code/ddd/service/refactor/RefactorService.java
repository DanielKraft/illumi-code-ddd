package illumi.code.ddd.service.refactor;

import illumi.code.ddd.model.DDDStructure;

public interface RefactorService {

    void setOldStructure(DDDStructure oldStructure);

    DDDStructure refactor();
}
