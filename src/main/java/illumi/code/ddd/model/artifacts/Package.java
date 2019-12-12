package illumi.code.ddd.model.artifacts;

import illumi.code.ddd.model.DDDStructure;
import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.analyse.impl.PackageAnalyseService;
import illumi.code.ddd.service.fitness.impl.PackageFitnessService;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Record;

public class Package extends Artifact {
  private ArrayList<Artifact> contains;

  /**
   * Constructor of package using Neo4j record.
   *
   * @param record : Neo4j result
   */
  public Package(Record record) {
    super(record, DDDType.MODULE);

    this.contains = new ArrayList<>();
  }

  public Package(String name, String path) {
    super(name, path, DDDType.MODULE);
    this.contains = new ArrayList<>();
  }

  public List<Artifact> getContains() {
    return contains;
  }

  public void setContains(List<Artifact> contains) {
    this.contains = (ArrayList<Artifact>) contains;
  }

  /**
   * Add containing artifact.
   *
   * @param artifact : new artifact
   */
  public void addContains(Artifact artifact) {
    if (!this.contains.contains(artifact)) {
      this.contains.add(artifact);
    }
  }

  public void setAggregateRoot(DDDStructure structure) {
    new PackageAnalyseService(this, structure).setAggregateRoot();
  }

  public void evaluate(DDDStructure structure) {
    setFitness(new PackageFitnessService(this, structure).evaluate());
  }

}
