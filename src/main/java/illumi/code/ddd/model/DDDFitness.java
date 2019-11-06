package illumi.code.ddd.model;

import org.json.JSONObject;

public class DDDFitness {
	
	private int numberOfCriteria;
	private int numberOfFulfilledCriteria;
	
	public DDDFitness() {
		this.numberOfCriteria = 0;
		this.numberOfFulfilledCriteria = 0;
	}
	
	public DDDFitness(int numberOfCriteria, int numberOfFulfilledCriteria) {
		this.numberOfCriteria = numberOfCriteria;
		this.numberOfFulfilledCriteria = numberOfFulfilledCriteria;
	}
	
	public void addNumberOfCriteria(int num) {
		numberOfCriteria += num;
	}
	
	public void incNumberOfCriteria() {
		numberOfCriteria++;
	}
	
	public void incNumberOfFulfilledCriteria() {
		numberOfFulfilledCriteria++;
	}
	
	public void decNumberOfFulfilledCriteria() {
		numberOfFulfilledCriteria--;
	}
	
	public void add(DDDFitness fitness) {
		this.numberOfCriteria += fitness.numberOfCriteria;
		this.numberOfFulfilledCriteria += fitness.numberOfFulfilledCriteria;
	}
	
	public double calculateFitness() {
		if (numberOfCriteria != 0) {
			double fitness = (double) (numberOfFulfilledCriteria * 100) / numberOfCriteria;
			
			return Math.round(fitness * 100.0) / 100.0;
		}
		return 100.0;
	}
	
	public DDDRating getscore() {
		double fitness = calculateFitness();
		if (fitness >= 90.0) 		return DDDRating.A;
		else if (fitness >= 80.0)	return DDDRating.B;
		else if (fitness >= 70.0)	return DDDRating.C;
		else if (fitness >= 60.0)	return DDDRating.D;
		else if (fitness >= 50.0)	return DDDRating.E;
		else return DDDRating.F;
	}
	
	public JSONObject toJSON() {
		return new JSONObject()
				.put("score", getscore())
				.put("criteria", new JSONObject()
						.put("total", numberOfCriteria)
						.put("fulfilled", numberOfFulfilledCriteria))
				.put("fitness (%)", calculateFitness());
	}
}
