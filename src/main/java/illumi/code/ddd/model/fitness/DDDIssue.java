package illumi.code.ddd.model.fitness;

public class DDDIssue {
	
	private DDDIssueType type;
	
	private String description;

	DDDIssue(DDDIssueType type, String description) {
		super();
		this.type = type;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
