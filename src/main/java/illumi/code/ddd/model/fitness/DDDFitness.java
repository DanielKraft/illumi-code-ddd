package illumi.code.ddd.model.fitness;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class DDDFitness {
	
	private int numberOfCriteria;
	private int numberOfFulfilledCriteria;
	
	private ArrayList<DDDIssue> issues;
	
	public DDDFitness() {
		this(0, 0);
	}
	
	public DDDFitness(int numberOfCriteria, int numberOfFulfilledCriteria) {
		this.numberOfCriteria = numberOfCriteria;
		this.numberOfFulfilledCriteria = numberOfFulfilledCriteria;
		this.issues = new ArrayList<>();
	}

	public int getNumberOfCriteria() {
		return numberOfCriteria;
	}

	public int getNumberOfFulfilledCriteria() {
		return numberOfFulfilledCriteria;
	}

	public void addIssue(boolean successful, DDDIssueType type, String message) {
		if (successful) {
			addSuccessfulCriteria(type);
		} else {
			addFailedCriteria(type, message);
		}
	}

	public void addFailedCriteria(DDDIssueType type, String description) {
		numberOfCriteria += type.weight;
		issues.add(new DDDIssue(type, description));
	}
	
	public DDDFitness addSuccessfulCriteria(DDDIssueType type) {
		numberOfCriteria += type.weight;
		numberOfFulfilledCriteria += type.weight;
		return this;
	}
	
	public void add(DDDFitness fitness) {
		this.numberOfCriteria += fitness.numberOfCriteria;
		this.numberOfFulfilledCriteria += fitness.numberOfFulfilledCriteria;
		this.issues.addAll(fitness.issues);
	}
	
	public double calculateFitness() {
		if (numberOfCriteria != 0) {
			double fitness = (double) (numberOfFulfilledCriteria * 100) / numberOfCriteria;
			
			return Math.round(fitness * 100.0) / 100.0;
		}
		return 100.0;
	}
	
	public DDDRating getScore() {
		double fitness = calculateFitness();
		if (fitness >= DDDRating.A.lowerBorder) 		return DDDRating.A;
		else if (fitness >= DDDRating.B.lowerBorder)	return DDDRating.B;
		else if (fitness >= DDDRating.C.lowerBorder)	return DDDRating.C;
		else if (fitness >= DDDRating.D.lowerBorder)	return DDDRating.D;
		else if (fitness >= DDDRating.E.lowerBorder)	return DDDRating.E;
		else return DDDRating.F;
	}
	
	public JSONObject summary() {
		return new JSONObject()
				.put("score", getScore())
				.put("criteria", new JSONObject()
						.put("total", numberOfCriteria)
						.put("fulfilled", numberOfFulfilledCriteria))
				.put("fitness", calculateFitness())
				.put("#Issues", issues.size());
	}

	public List<DDDIssue> getIssues() {
		issues.sort((DDDIssue i1, DDDIssue i2) -> Integer.compare(i2.getType().weight, i1.getType().weight));
		return issues;
	}

	public List<String> getSortedIssues() {
		ArrayList<String> result = new ArrayList<>();
		getIssues().stream()
				.parallel()
				.forEachOrdered(dddIssue -> result.add(dddIssue.toString()));

		return result;
	}
	
}
