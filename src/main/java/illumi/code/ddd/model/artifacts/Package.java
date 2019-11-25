package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import illumi.code.ddd.service.analyse.PackageAnalyseService;
import illumi.code.ddd.service.fitness.PackageFitnessService;
import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDType;
import illumi.code.ddd.service.StructureService;

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
	
	public void setAggregateRoot(StructureService structureService) {
		new PackageAnalyseService(this, structureService).setAggregateRoot();
	}

	public void evaluate(StructureService structureService) {
		setFitness(new PackageFitnessService(this, structureService).evaluate());
	}
}
