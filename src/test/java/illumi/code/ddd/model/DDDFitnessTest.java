package illumi.code.ddd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class DDDFitnessTest {
	
	@Test
	public void testAddSuccessfulCriteria() {
		DDDFitness fitness = new DDDFitness();
		
		fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		
		JSONObject result = fitness.summary();
		
		assertEquals(DDDRating.A, result.get("score"));
		assertEquals(2, ((JSONObject) result.get("criteria")).get("total"));
		assertEquals(2, ((JSONObject) result.get("criteria")).get("fulfilled"));
		assertEquals(100.0, result.get("fitness"));
	}
	
	@Test
	public void testAddFailedCriteria() {
		DDDFitness fitness = new DDDFitness();
		
		fitness.addFailedCriteria(DDDIssueType.MINOR, "test");
		
		JSONObject result = fitness.summary();
		
		assertEquals(DDDRating.F, result.get("score"));
		assertEquals(1, ((JSONObject) result.get("criteria")).get("total"));
		assertEquals(0, ((JSONObject) result.get("criteria")).get("fulfilled"));
		assertEquals(0.0, result.get("fitness"));
	}
	
	@Test
	public void testAddFitness() {
		DDDFitness fitness = new DDDFitness(2, 2);
		
		DDDFitness otherFitness = new DDDFitness(8, 4);
		
		fitness.add(otherFitness);
		
		JSONObject result = fitness.summary();
		
		assertEquals(DDDRating.D, result.get("score"));
		assertEquals(10, ((JSONObject) result.get("criteria")).get("total"));
		assertEquals(6, ((JSONObject) result.get("criteria")).get("fulfilled"));
		assertEquals(60.0, result.get("fitness"));
	}
	
	@Test
	public void testCalculateFitness() {
		DDDFitness fitness = new DDDFitness(4, 2);
		
		double result = fitness.calculateFitness();
		
		assertEquals(50.0, result);
	}
	
	@Test
	public void testCalculateFitnessDivideZero() {
		DDDFitness fitness = new DDDFitness();
		
		double result = fitness.calculateFitness();
		
		assertEquals(100.0, result);
	}
	
	@Test
	public void testCalculateFitnessRound() {
		DDDFitness fitness = new DDDFitness(3, 2);
		
		double result = fitness.calculateFitness();
		
		assertEquals(66.67, result);
	}
	
	@Test
	public void testGetScoreAWith100Procent() {
		DDDFitness fitness = new DDDFitness(10, 10);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.A, result);
	}
	
	@Test
	public void testGetScoreAWith90Procent() {
		DDDFitness fitness = new DDDFitness(10, 9);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.A, result);
	}
	
	@Test
	public void testGetScoreAWith95Procent() {
		DDDFitness fitness = new DDDFitness(20, 19);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.A, result);
	}
	
	@Test
	public void testGetScoreBWith89Procent() {
		DDDFitness fitness = new DDDFitness(100, 89);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.B, result);
	}
	
	@Test
	public void testGetScoreBWith80Procent() {
		DDDFitness fitness = new DDDFitness(100, 80);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.B, result);
	}
	
	@Test
	public void testGetScoreBAWith85Procent() {
		DDDFitness fitness = new DDDFitness(100, 85);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.B, result);
	}
	
	@Test
	public void testGetScoreCWith79Procent() {
		DDDFitness fitness = new DDDFitness(100, 79);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.C, result);
	}
	
	@Test
	public void testGetScoreCWith70Procent() {
		DDDFitness fitness = new DDDFitness(100, 70);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.C, result);
	}
	
	@Test
	public void testGetScoreCAWith75Procent() {
		DDDFitness fitness = new DDDFitness(100, 75);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.C, result);
	}
	
	@Test
	public void testGetScoreDWith69Procent() {
		DDDFitness fitness = new DDDFitness(100, 69);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.D, result);
	}
	
	@Test
	public void testGetScoreDWith60Procent() {
		DDDFitness fitness = new DDDFitness(100, 60);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.D, result);
	}
	
	@Test
	public void testGetScoreDAWith65Procent() {
		DDDFitness fitness = new DDDFitness(100, 65);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.D, result);
	}
	
	@Test
	public void testGetScoreEWith59Procent() {
		DDDFitness fitness = new DDDFitness(100, 59);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.E, result);
	}
	
	@Test
	public void testGetScoreEWith50Procent() {
		DDDFitness fitness = new DDDFitness(100, 50);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.E, result);
	}
	
	@Test
	public void testGetScoreEAWith55Procent() {
		DDDFitness fitness = new DDDFitness(100, 55);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.E, result);
	}
	
	@Test
	public void testGetScoreEWith49Procent() {
		DDDFitness fitness = new DDDFitness(100, 49);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.F, result);
	}
	
	@Test
	public void testGetScoreEAWith0Procent() {
		DDDFitness fitness = new DDDFitness(100, 0);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.F, result);
	}
}
