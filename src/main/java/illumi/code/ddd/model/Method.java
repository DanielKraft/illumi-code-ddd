package illumi.code.ddd.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Record;

/**
 * Entity-Class: Method
 * @author Daniel Kraft
 */
public class Method {
	
	private String visibility;
	private String name;
	private String signature;
	
	private ArrayList<Field> variables;

	public Method( Record record ) {
		this.visibility = record.get( "visibility" ).asString();
		this.name = record.get( "name" ).asString();
		this.signature = record.get( "signature" ).asString();
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
	
	public List<Field> getVariables() {
		return variables;
	}

	public void addVariables(Field variable) {
		this.variables.add(variable);
	}
}
