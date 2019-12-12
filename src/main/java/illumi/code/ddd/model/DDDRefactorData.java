package illumi.code.ddd.model;

import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Package;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("CheckStyle")
public class DDDRefactorData {

  private DDDStructure oldStructure;
  private DDDStructure newStructure;

  private Package domainModule;
  private Package applicationModule;
  private Package infrastructureModule;

  private Package modelModule;

  private ArrayList<Class> roots;

  /**
   * Constructor of DDDRefactorData.
   *
   * @param oldStructure : old structure of the system
   */
  public DDDRefactorData(DDDStructure oldStructure) {
    this.oldStructure = oldStructure;
    this.newStructure = new DDDStructure();
    this.newStructure.setPath(oldStructure.getPath());
    this.roots = new ArrayList<>();
  }

  public void setDomainModule(Package domainModule) {
    this.domainModule = domainModule;
  }

  public void setApplicationModule(Package applicationModule) {
    this.applicationModule = applicationModule;
  }

  public void setInfrastructureModule(Package infrastructureModule) {
    this.infrastructureModule = infrastructureModule;
  }

  public void setModelModule(Package modelModule) {
    this.modelModule = modelModule;
  }

  public void addRoots(Class root) {
    this.roots.add(root);
  }

  public DDDStructure getOldStructure() {
    return oldStructure;
  }

  public DDDStructure getNewStructure() {
    return newStructure;
  }

  public Package getDomainModule() {
    return domainModule;
  }

  public Package getApplicationModule() {
    return applicationModule;
  }

  public Package getInfrastructureModule() {
    return infrastructureModule;
  }

  public Package getModelModule() {
    return modelModule;
  }

  public List<Class> getRoots() {
    return roots;
  }
}
