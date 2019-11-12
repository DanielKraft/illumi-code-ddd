package illumi.code.ddd.model.artifacts;

import org.neo4j.driver.v1.Record;

/**
 * Entity-Class: Method
 * @author Daniel Kraft
 */
public class Method {
			
	private String visibility;
	private String name;
	private String signature;
	
	public Method( Record record ) {
		this.visibility = record.get( "visibility" ).asString();
		this.name = record.get( "name" ).asString();
		this.signature = record.get( "signature" ).asString();
	}
	
	public Method(String visibility, String name, String signature) {
		this.visibility = visibility;
		this.name = name;
		this.signature = signature;
	}

	public String getVisibility() {
		return visibility;
	}
	
	public String getName() {
		return name;
	}

	public String getSignature() {
		return signature;
	}
}
