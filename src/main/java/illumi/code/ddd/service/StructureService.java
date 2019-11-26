package illumi.code.ddd.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import illumi.code.ddd.model.artifacts.Annotation;
import illumi.code.ddd.model.artifacts.Artifact;
import illumi.code.ddd.model.artifacts.Class;
import illumi.code.ddd.model.artifacts.Enum;
import illumi.code.ddd.model.artifacts.Interface;
import illumi.code.ddd.model.artifacts.Package;

public class StructureService {
		
	private String path;   
	
	private ArrayList<Artifact> structure; 
    
	private ArrayList<String> domains;
	
	private ArrayList<Package> packages;
    private ArrayList<Class> classes;
    private ArrayList<Interface> interfaces;
    private ArrayList<Enum> enums;
    private ArrayList<Annotation> annotations;
	
	public StructureService () {
		init();
	}
	
	public void init() {
		this.path = "";
		this.structure = new ArrayList<>();
		this.domains = new ArrayList<>();
		this.packages = new ArrayList<>();
		this.classes = new ArrayList<>();
		this.interfaces = new ArrayList<>();
		this.enums = new ArrayList<>();
		this.annotations = new ArrayList<>();
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		String[] split = path.split("[.]");
		this.path = split[split.length-1] + ".";
	}

	List<Artifact> getStructure() {
		return structure;
	}

	public void setStructure(List<Artifact> artifacts) {
		this.structure = (ArrayList<Artifact>) artifacts;
	}

	public List<String> getDomains() {
		return domains;
	}

	public void addDomain(String domain) {
		if (!this.domains.contains(domain)) {
			this.domains.add(domain);
		}
	}
	
	public List<Package> getPackages() {
		return packages;
	}

	public void addPackage(Package module) {
		this.packages.add(module);
	}

	public List<Class> getClasses() {
		return classes;
	}

	public void addClass(Class c) {
		if (!this.classes.contains(c)) {
			this.classes.add(c);
		}
	}

	List<Interface> getInterfaces() {
		return interfaces;
	}

	public void addInterface(Interface i) {
		this.interfaces.add(i);
	}

	List<Enum> getEnums() {
		return enums;
	}

	void addEnum(Enum e) {
		this.enums.add(e);
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void addAnnotation(Annotation a) {
		this.annotations.add(a);
	}
	
	List<Artifact> getAllArtifacts() {
		ArrayList<Artifact> all = new ArrayList<>();
		
		all.addAll(this.packages);
		all.addAll(this.classes);
		all.addAll(this.interfaces);
		all.addAll(this.enums);
		all.addAll(this.annotations);

		all.sort((Artifact a1, Artifact a2) -> Double.compare(a2.getFitness(), a1.getFitness()));
		return all;
	}
	
	JSONArray getJSON() {
		return convertPackage(structure);
	}
	
	private JSONArray convertPackage(ArrayList<Artifact> artifacts) {
		JSONArray array = new JSONArray();
		for (Artifact artifact : artifacts) {
			JSONObject json = artifact.toJSON();
			if (artifact instanceof Package) {
				json.put("contains", convertPackage((ArrayList<Artifact>) ((Package) artifact).getContains()));
			}
			array.put(json);
		}
		return array;
	}
}
