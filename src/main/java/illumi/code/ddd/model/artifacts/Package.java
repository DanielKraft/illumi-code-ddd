package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import illumi.code.ddd.service.analyse.impl.PackageAnalyseService;
import illumi.code.ddd.service.fitness.impl.PackageFitnessService;

import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.model.DDDStructure;

/**
 * Entity-Class: Package
 * @author Daniel Kraft
 */
public class Package extends Artifact {
		
	private ArrayList<Artifact> contains;
	
	public Package(Record record) {
		super(record, DDDType.MODULE);
		
		this.contains = new ArrayList<>();
	}

	public Package(String name, String path) {
		super(name, path, DDDType.MODULE);
		this.contains = new ArrayList<>();
	}

	public List<Artifact> getContains() {
		return contains;
	}

	public void setContains(List<Artifact> contains) {
		this.contains = (ArrayList<Artifact>) contains;
	}

	public void addContains(Artifact artifact) {
		if (!this.contains.contains(artifact)) {
			this.contains.add(artifact);
		}
	}
	
	public void setAggregateRoot(DDDStructure structure) {
		new PackageAnalyseService(this, structure).setAggregateRoot();
	}

	public void evaluate(DDDStructure structure) {
		setFitness(new PackageFitnessService(this, structure).evaluate());
	}

}
