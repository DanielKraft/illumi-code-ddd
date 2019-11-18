package illumi.code.ddd.model;

public enum DDDIssueType {
	BLOCKER(8),
	CRITICAL(4),
	MAJOR(2),
	MINOR(1),
	INFO(0);
	
	public final int weight;
	 
    DDDIssueType(int weight) {
        this.weight = weight;
    }
}
