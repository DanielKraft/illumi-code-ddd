package illumi.code.ddd.model.artifacts;

import org.neo4j.driver.v1.Record;

public class Field {
	
	private String visibility;
	private String name;
	private String type;
	
	public Field( Record record) {
		this.visibility = record.get( "visibility" ).asString();
		this.name = record.get( "name" ).asString();
		this.type = record.get( "type" ).asString().split(" ")[0];
	}

	public Field(String visibility, String name, String type) {
		this.visibility = visibility;
		this.name = name;
		this.type = type;
	}

	public String getVisibility() {
		return visibility;
	}
	
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
}
