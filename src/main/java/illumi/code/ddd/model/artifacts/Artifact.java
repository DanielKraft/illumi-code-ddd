package illumi.code.ddd.model.artifacts;

import org.json.JSONObject;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDType;

public abstract class Artifact {

	private String name;
	private String path;
	
	private String domain;
	
	private DDDType type;
	private DDDFitness fitness;
	
	public Artifact(Record record, DDDType type) {
		this.name = record.get( "name" ).asString();
		this.path = record.get( "path" ).asString();
		this.type = type;
	}
	
	public Artifact(String name, String path, DDDType type) {
		this.name = name;
		this.path = path;
		this.type = type;
		this.fitness = new DDDFitness();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artifact other = (Artifact) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
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
	
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public DDDType getType() {
		return type;
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
				.put("domain", domain)
				.put("fitness", fitness != null ? getFitness() : null);
	}
}
