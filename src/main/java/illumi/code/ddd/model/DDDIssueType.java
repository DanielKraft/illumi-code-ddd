package illumi.code.ddd.model;

public enum DDDIssueType {
	BLOCKER(10),
	CRITICAL(5),
	MAJOR(3),
	MINOR(1),
	INFO(0);
	
	public final int weight;
	 
    DDDIssueType(int weight) {
        this.weight = weight;
    }
}
