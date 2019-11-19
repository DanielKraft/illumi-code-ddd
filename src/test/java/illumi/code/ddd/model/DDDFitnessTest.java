package illumi.code.ddd.model;

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
		
		assertEquals(DDDRating.C, result.get("score"));
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
	void testGetScoreAWith100Procent() {
		DDDFitness fitness = new DDDFitness(100, 100);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.A, result);
	}
	
	@Test
	void testGetScoreAWith95Procent() {
		DDDFitness fitness = new DDDFitness(100, 95);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.A, result);
	}
	
	@Test
	void testGetScoreAWith90Procent() {
		DDDFitness fitness = new DDDFitness(100, 90);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.A, result);
	}
	
	@Test
	void testGetScoreBWith89Procent() {
		DDDFitness fitness = new DDDFitness(100, 89);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.B, result);
	}

	@Test
	void testGetScoreBAWith85Procent() {
		DDDFitness fitness = new DDDFitness(100, 85);

		DDDRating result = fitness.getscore();

		assertEquals(DDDRating.B, result);
	}
	
	@Test
	void testGetScoreBWith80Procent() {
		DDDFitness fitness = new DDDFitness(100, 80);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.B, result);
	}
	
	@Test
	void testGetScoreCWith79Procent() {
		DDDFitness fitness = new DDDFitness(100, 79);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.C, result);
	}
	
	@Test
	void testGetScoreCWith70Procent() {
		DDDFitness fitness = new DDDFitness(100, 70);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.C, result);
	}
	
	@Test
	void testGetScoreCAWith60Procent() {
		DDDFitness fitness = new DDDFitness(100, 60);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.C, result);
	}
	
	@Test
	void testGetScoreDWith59Procent() {
		DDDFitness fitness = new DDDFitness(100, 59);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.D, result);
	}
	
	@Test
	void testGetScoreDWith50Procent() {
		DDDFitness fitness = new DDDFitness(100, 50);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.D, result);
	}
	
	@Test
	void testGetScoreDAWith40Procent() {
		DDDFitness fitness = new DDDFitness(100, 40);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.D, result);
	}
	
	@Test
	void testGetScoreEWith39Procent() {
		DDDFitness fitness = new DDDFitness(100, 39);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.E, result);
	}
	
	@Test
	void testGetScoreEWith30Procent() {
		DDDFitness fitness = new DDDFitness(100, 30);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.E, result);
	}
	
	@Test
	void testGetScoreEAWith25Procent() {
		DDDFitness fitness = new DDDFitness(100, 25);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.E, result);
	}
	
	@Test
	void testGetScoreEWith19Procent() {
		DDDFitness fitness = new DDDFitness(100, 19);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.F, result);
	}
	
	@Test
	void testGetScoreEAWith0Procent() {
		DDDFitness fitness = new DDDFitness(100, 0);
		
		DDDRating result = fitness.getscore();
		
		assertEquals(DDDRating.F, result);
	}
}
