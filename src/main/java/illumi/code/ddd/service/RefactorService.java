package illumi.code.ddd.service;

public interface RefactorService {

    void setOldStructure(StructureService oldStructure);

    StructureService refactor();
}
