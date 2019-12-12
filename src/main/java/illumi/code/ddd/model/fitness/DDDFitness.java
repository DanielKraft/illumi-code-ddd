package illumi.code.ddd.model.fitness;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

@SuppressWarnings("CheckStyle")
public class DDDFitness {

  private int numberOfCriteria;
  private int numberOfFulfilledCriteria;

  private ArrayList<DDDIssue> issues;

  public DDDFitness() {
    this(0, 0);
  }

  /**
   * Constructor of DDDFitness.
   *
   * @param numberOfCriteria          : number of total criteria
   * @param numberOfFulfilledCriteria : number of fulfilled criteria
   */
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

  /**
   * Add issue if criteria is not fulfilled.
   *
   * @param successful : boolean if criteria is fulfilled
   * @param type       : wight of the criteria
   * @param message    : error message
   */
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

  /**
   * Add fulfilled criteria.
   *
   * @param type : wight of the criteria
   * @return DDDIssueType
   */
  public DDDFitness addSuccessfulCriteria(DDDIssueType type) {
    numberOfCriteria += type.weight;
    numberOfFulfilledCriteria += type.weight;
    return this;
  }

  /**
   * Add two DDDFitness objects.
   *
   * @param fitness : other DDDFitness
   */
  public void add(DDDFitness fitness) {
    this.numberOfCriteria += fitness.numberOfCriteria;
    this.numberOfFulfilledCriteria += fitness.numberOfFulfilledCriteria;
    this.issues.addAll(fitness.issues);
  }

  /**
   * Calculates the fitness of this object.
   *
   * @return percentage of fitness
   */
  public double calculateFitness() {
    if (numberOfCriteria != 0) {
      double fitness = (double) (numberOfFulfilledCriteria * 100) / numberOfCriteria;

      return Math.round(fitness * 100.0) / 100.0;
    }
    return 100.0;
  }

  /**
   * Get score of this object.
   * @return DDDRating
   */
  public DDDRating getScore() {
    double fitness = calculateFitness();
    if (fitness >= DDDRating.A.lowerBorder) {
      return DDDRating.A;
    } else if (fitness >= DDDRating.B.lowerBorder) {
      return DDDRating.B;
    } else if (fitness >= DDDRating.C.lowerBorder) {
      return DDDRating.C;
    } else if (fitness >= DDDRating.D.lowerBorder) {
      return DDDRating.D;
    } else if (fitness >= DDDRating.E.lowerBorder) {
      return DDDRating.E;
    } else {
      return DDDRating.F;
    }
  }

  /**
   * Generate summary of this object.
   * @return JSONObject
   */
  public JSONObject summary() {
    return new JSONObject()
        .put("score", getScore())
        .put("fitness", calculateFitness())
        .put("#Issues", issues.size());
  }

  /**
   * Get list of issues.
   * @return List of issues
   */
  public List<DDDIssue> getIssues() {
    issues.sort((DDDIssue i1, DDDIssue i2) ->
        Integer.compare(i2.getType().weight, i1.getType().weight));
    return issues;
  }

  /**
   * Get sorted list of issues.
   * @return List of issues
   */
  public List<String> getSortedIssues() {
    ArrayList<String> result = new ArrayList<>();
    getIssues().stream()
        .parallel()
        .forEachOrdered(dddIssue -> result.add(dddIssue.toString()));

    return result;
  }

}
