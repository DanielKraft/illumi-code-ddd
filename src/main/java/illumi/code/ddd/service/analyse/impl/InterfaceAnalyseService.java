package illumi.code.ddd.service.analyse.impl;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.artifacts.Interface;

public class InterfaceAnalyseService {
  private Interface artifact;

  public InterfaceAnalyseService(Interface artifact) {
    this.artifact = artifact;
  }

  /**
   * Set DDD-Type of the interface.
   */
  public void setType() {
    if (isInfrastructure()) {
      artifact.setType(DDDType.INFRASTRUCTURE);
    }
  }

  private boolean isInfrastructure() {
    return artifact.getName().toUpperCase().contains("JPA")
        || artifact.getName().toUpperCase().contains("CRUD");
  }
}
