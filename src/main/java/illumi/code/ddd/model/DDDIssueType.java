package illumi.code.ddd.model;

public enum DDDIssueType {
	BLOCKER(4),
	CRITICAL(3),
	MAJOR(2),
	MINOR(1),
	INFO(0);
	
	public final int weight;
	 
    private DDDIssueType(int weight) {
        this.weight = weight;
    }
}
