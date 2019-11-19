package illumi.code.ddd.model;

public enum DDDRating {
	A(90.0),
	B(80.0),
	C(60.0),
	D(40.0),
	E(20.0),
	F(0.0);

	public final double lowerBorder;
	 
    DDDRating(double lowerBorder) {
        this.lowerBorder = lowerBorder;
    }
}
