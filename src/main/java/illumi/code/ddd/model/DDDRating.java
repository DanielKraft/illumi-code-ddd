package illumi.code.ddd.model;

public enum DDDRating {
	A(90.0),
	B(80.0),
	C(70.0),
	D(60.0),
	E(50.0),
	F(0.0);

	public final double lowerBorder;
	 
    DDDRating(double lowerBorder) {
        this.lowerBorder = lowerBorder;
    }
}
