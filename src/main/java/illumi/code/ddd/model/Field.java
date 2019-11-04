package illumi.code.ddd.model;

import org.neo4j.driver.v1.Record;

public class Field {
	
	private String visibility;
	private String name;
	private String type;
	
	public Field( Record record) {
		this.visibility = record.get( "visibility" ).asString();
		this.name = record.get( "name" ).asString();
		this.type = record.get( "type" ).asString();
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
