package illumi.code.ddd.service;

public interface RefactorService {

    void setStructureService(StructureService structureService);

    StructureService refactor();
}
