package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;

import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;

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

	public Package(Record record, DDDType type, ArrayList<Artifact> conataints) {
		super(record, type);
		this.conataints = conataints;
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
