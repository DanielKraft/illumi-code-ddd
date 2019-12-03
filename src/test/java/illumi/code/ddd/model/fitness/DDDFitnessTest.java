package illumi.code.ddd.model.fitness;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class DDDFitnessTest {
	
	@Test
	void testAddSuccessfulCriteria() {
		DDDFitness fitness = new DDDFitness();
		
		fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		
		JSONObject result = fitness.summary();
		assertAll("Should find an aggregate root",
				() -> assertEquals(DDDRating.A, 	result.get("score"), 									"Score"),
				() -> assertEquals(3, 		((JSONObject) result.get("criteria")).get("total"), 	"Total"),
				() -> assertEquals(3, 		((JSONObject) result.get("criteria")).get("fulfilled"), "Fulfilled"),
				() -> assertEquals(100.0, 	result.get("fitness"), 									"Fitness"));
	}
	
	@Test
	void testAddFailedCriteria() {
		DDDFitness fitness = new DDDFitness();
		
		fitness.addFailedCriteria(DDDIssueType.MINOR, "test");
		
		JSONObject result = fitness.summary();
		
		assertEquals(DDDRating.F, result.get("score"));
		assertEquals(1, ((JSONObject) result.get("criteria")).get("total"));
		assertEquals(0, ((JSONObject) result.get("criteria")).get("fulfilled"));
		assertEquals(0.0, result.get("fitness"));
	}
	
	@Test
	void testAddFitness() {
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
	void testCalculateFitness() {
		DDDFitness fitness = new DDDFitness(4, 2);
		
		double result = fitness.calculateFitness();
		
		assertEquals(50.0, result);
	}
	
	@Test
	void testCalculateFitnessDivideZero() {
		DDDFitness fitness = new DDDFitness();
		
		double result = fitness.calculateFitness();
		
		assertEquals(100.0, result);
	}
	
	@Test
	void testCalculateFitnessRound() {
		DDDFitness fitness = new DDDFitness(3, 2);
		
		double result = fitness.calculateFitness();
		
		assertEquals(66.67, result);
	}
	
	@Test
	void testGetScoreAWith100Percent() {
		DDDFitness fitness = new DDDFitness(100, 100);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.A, result);
	}
	
	@Test
	void testGetScoreAWith97Percent() {
		DDDFitness fitness = new DDDFitness(100, 97);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.A, result);
	}
	
	@Test
	void testGetScoreAWith95Percent() {
		DDDFitness fitness = new DDDFitness(100, 95);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.A, result);
	}
	
	@Test
	void testGetScoreBWith94Percent() {
		DDDFitness fitness = new DDDFitness(100, 94);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.B, result);
	}

	@Test
	void testGetScoreBAWith93Percent() {
		DDDFitness fitness = new DDDFitness(100, 93);

		DDDRating result = fitness.getScore();

		assertEquals(DDDRating.B, result);
	}
	
	@Test
	void testGetScoreBWith90Percent() {
		DDDFitness fitness = new DDDFitness(100, 90);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.B, result);
	}
	
	@Test
	void testGetScoreCWith89Percent() {
		DDDFitness fitness = new DDDFitness(100, 89);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.C, result);
	}
	
	@Test
	void testGetScoreCWith85Percent() {
		DDDFitness fitness = new DDDFitness(100, 85);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.C, result);
	}
	
	@Test
	void testGetScoreCAWith80Percent() {
		DDDFitness fitness = new DDDFitness(100, 80);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.C, result);
	}
	
	@Test
	void testGetScoreDWith79Percent() {
		DDDFitness fitness = new DDDFitness(100, 79);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.D, result);
	}
	
	@Test
	void testGetScoreDWith65Percent() {
		DDDFitness fitness = new DDDFitness(100, 65);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.D, result);
	}
	
	@Test
	void testGetScoreDAWith50Percent() {
		DDDFitness fitness = new DDDFitness(100, 50);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.D, result);
	}
	
	@Test
	void testGetScoreEWith49Percent() {
		DDDFitness fitness = new DDDFitness(100, 49);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.E, result);
	}
	
	@Test
	void testGetScoreEWith30Percent() {
		DDDFitness fitness = new DDDFitness(100, 30);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.E, result);
	}
	
	@Test
	void testGetScoreEAWith20Percent() {
		DDDFitness fitness = new DDDFitness(100, 20);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.E, result);
	}
	
	@Test
	void testGetScoreEWith19Percent() {
		DDDFitness fitness = new DDDFitness(100, 19);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.F, result);
	}
	
	@Test
	void testGetScoreEAWith0Percent() {
		DDDFitness fitness = new DDDFitness(100, 0);
		
		DDDRating result = fitness.getScore();
		
		assertEquals(DDDRating.F, result);
	}
}
