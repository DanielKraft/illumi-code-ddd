package illumi.code.ddd.model.fitness;

@SuppressWarnings("CheckStyle")
public enum DDDRating {
  A(95.0),
  B(90.0),
  C(80.0),
  D(50.0),
  E(20.0),
  F(0.0);

  public final double lowerBorder;

  DDDRating(double lowerBorder) {
    this.lowerBorder = lowerBorder;
  }
}
