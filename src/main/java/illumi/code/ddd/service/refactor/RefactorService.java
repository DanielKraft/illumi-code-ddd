package illumi.code.ddd.service.refactor;

import illumi.code.ddd.model.Structure;

public interface RefactorService {

    void setOldStructure(Structure oldStructure);

    Structure refactor();
}
