package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

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

	public Package(String name, String path) {
		super(name, path, DDDType.MODULE);
		this.conataints = new ArrayList<>();
	}

	public List<Artifact> getConataints() {
		return conataints;
	}

	public void setConataints(List<Artifact> conataints) {
		this.conataints = (ArrayList<Artifact>) conataints;
	}

	public void addConataints(Artifact artifact) {
		this.conataints.add(artifact);
	}
	
}
