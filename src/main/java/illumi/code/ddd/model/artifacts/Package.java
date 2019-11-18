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
	
	public void setAggregateRoot(StructureService structureService) {
		new PackageAnalyseService(this, structureService).setAggregateRoot();
	}

	public void evaluate(StructureService structureService) {
		new PackageFitnessService(this, structureService).evaluate();
	}
}
