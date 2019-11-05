package illumi.code.ddd.model;

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
	
	public double calculateFitness() {
		double fitness = (double) (numberOfFulfilledCriteria * 100) / numberOfCriteria;
		
		return Math.round(fitness * 100.0) / 100.0;
	}
}
