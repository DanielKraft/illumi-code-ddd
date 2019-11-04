package illumi.code.ddd.model;

import java.util.ArrayList;

import org.neo4j.driver.v1.Record;

/**
 * Entity-Class: Package
 * @author Daniel Kraft
 */
public class Package extends Artifact {
	
	private ArrayList<Artifact> conataints;
	
	public Package(Record record) {
		super(record, DDDType.MODULE);
		
		this.conataints = new ArrayList<>();
	}

	public ArrayList<Artifact> getConataints() {
		return conataints;
	}

	public void setConataints(ArrayList<Artifact> conataints) {
		this.conataints = conataints;
	}

	public void addConataints(Artifact artifact) {
		this.conataints.add(artifact);
	}
}
