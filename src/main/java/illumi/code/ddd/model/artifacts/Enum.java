package illumi.code.ddd.model.artifacts;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.fitness.DDDFitness;

import org.neo4j.driver.v1.Record;

public class Enum extends File {

  public Enum(Record record) {
    super(record, DDDType.VALUE_OBJECT);
    setFitness(new DDDFitness());
  }

  public Enum(String name, String path) {
    super(name, path, DDDType.VALUE_OBJECT);
    setFitness(new DDDFitness());
  }
}
