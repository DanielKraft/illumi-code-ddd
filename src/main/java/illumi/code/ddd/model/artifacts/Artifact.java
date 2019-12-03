package illumi.code.ddd.model.artifacts;

import org.json.JSONObject;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.fitness.DDDFitness;
import illumi.code.ddd.model.DDDType;

public abstract class Artifact {

	private String name;
	private String path;


	private String domain;

	private DDDType type;
	private DDDFitness fitness;
	
	public Artifact(Record record, DDDType type) {
		this(record.get( "name" ).asString(), record.get( "path" ).asString(), type);
	}
	
	public Artifact(String name, String path, DDDType type) {
		this.name = name;
		this.path = path;
		this.type = type;
		this.fitness = new DDDFitness();
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public DDDType getType() {
		return type;
	}
	
	public boolean isTypeOf(DDDType type) {
		return this.type == type;
	}

	public void setType(DDDType type) {
		this.type = type;
	}
	
	public void setFitness(DDDFitness fitness) {
		this.fitness = fitness;
	}
	
	public DDDFitness getDDDFitness() {
		return fitness;
	}
	
	public double getFitness() {
		return fitness.calculateFitness();
	}

	public JSONObject toJSON() {
		return new JSONObject()
				.put("name", name)
				.put("DDD", type)
				.put("fitness", getFitness());
	}

	public JSONObject toJSONSummary() {
		return new JSONObject()
				.put("name", name)
				.put("DDD", type)
				.put("domain", domain)
				.put("fitness", getFitness())
				.put("issues", fitness.getIssues());
	}
}
