package illumi.code.ddd.model.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Record;

import illumi.code.ddd.model.DDDFitness;
import illumi.code.ddd.model.DDDIssueType;
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
	
	public void evaluate(StructureService structureService) {
		DDDFitness fitness = new DDDFitness();
		
		if (isDomainModule(structureService)) {
			fitness = evaluateDomainModule(structureService);
		} else if (containsInfrastructure()) {
			fitness = evaluateInfrastructureModule(structureService);
		} else if (containsApplication()) {
			fitness = evaluateApplicationModule(structureService);
		} else {
			fitness = new DDDFitness().addFailedCriteria(DDDIssueType.INFO, String.format("The module '%s' is no DDD-Module.", getName()));
		}
		setFitness(fitness);
	}

	private boolean isDomainModule(StructureService structureService) {
		return structureService.getDomains().contains(getName());
	}

	private DDDFitness evaluateDomainModule(StructureService structureService) {
		DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);
		if (getPath().contains(structureService.getPath() + "domain." + getName())) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The module '%s' is not a submodule of the module 'domain'.", getName()));
		}
		
		if (containsAggregateRoot()) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The module '%s' does not contain an Aggregate Root.", getName()));
		}
		return fitness;
	}

	private boolean containsAggregateRoot() {
		for (Artifact artifact : getConataints()) {
			if (artifact.getType() == DDDType.AGGREGATE_ROOT) {
				return true;
			}
		}
		return false;
	}

	private boolean containsInfrastructure() {
		for (Artifact artifact : getConataints()) {
			if (artifact.getType() == DDDType.INFRASTRUCTUR || artifact.getType() == DDDType.CONTROLLER) {
				return true;
			}
		}
		return false;
	}

	private DDDFitness evaluateInfrastructureModule(StructureService structureService) {
		DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);
		
		if (getPath().contains(structureService.getPath() + "infrastructur")) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The module '%s' is not an infrastructure module.", getName()));
		}
		
		if (containsOnlyInfrastructure()) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The module '%s' dose not only containts infrastructure artifacts.", getName()));
		}
		
		return fitness;
	}
	
	private boolean containsOnlyInfrastructure() {
		for (Artifact artifact : getConataints()) {
			if (artifact.getType() != DDDType.INFRASTRUCTUR && artifact.getType() != DDDType.CONTROLLER) {
				return false;
			}
		}
		return true;
	}
	
	private boolean containsApplication() {
		for (Artifact artifact : getConataints()) {
			if (artifact.getType() == DDDType.APPLICATION_SERVICE) {
				return true;
			}
		}
		return false;
	}

	private DDDFitness evaluateApplicationModule(StructureService structureService) {
		DDDFitness fitness = new DDDFitness().addSuccessfulCriteria(DDDIssueType.MINOR);
		
		if (getPath().contains(structureService.getPath() + "application")) {
			fitness.addSuccessfulCriteria(DDDIssueType.MINOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MINOR, String.format("The module '%s' is not an application module.", getName()));
		}
		
		if (containsOnlyApplication()) {
			fitness.addSuccessfulCriteria(DDDIssueType.MAJOR);
		} else {
			fitness.addFailedCriteria(DDDIssueType.MAJOR, String.format("The module '%s' dose not only containts infrastructure artifacts.", getName()));
		}
		
		return fitness;
	}

	private boolean containsOnlyApplication() {
		for (Artifact artifact : getConataints()) {
			if (artifact.getType() != DDDType.APPLICATION_SERVICE) {
				return false;
			}
		}
		return true;
	}
	
}
