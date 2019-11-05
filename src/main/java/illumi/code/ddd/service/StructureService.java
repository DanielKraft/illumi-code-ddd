package illumi.code.ddd.service;

import java.util.ArrayList;


import org.json.JSONArray;
import org.json.JSONObject;

import illumi.code.ddd.model.Annotation;
import illumi.code.ddd.model.Artifact;
import illumi.code.ddd.model.Class;
import illumi.code.ddd.model.Enum;
import illumi.code.ddd.model.Interface;
import illumi.code.ddd.model.Package;

public class StructureService {
		
	private String path;   
	
	private ArrayList<Artifact> structure; 
    
    private ArrayList<Class> classes;
    private ArrayList<Interface> interfaces;
    private ArrayList<Enum> enums;
    private ArrayList<Annotation> annotations;
	
	public StructureService () {
		this.structure = new ArrayList<>();
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

	public ArrayList<Artifact> getStructure() {
		return structure;
	}

	public void setStructure(ArrayList<Artifact> artifacts) {
		this.structure = artifacts;
	}

	public ArrayList<Class> getClasses() {
		return classes;
	}

	public void addClasses(Class c) {
		this.classes.add(c);
	}

	public ArrayList<Interface> getInterfaces() {
		return interfaces;
	}

	public void addInterfaces(Interface i) {
		this.interfaces.add(i);
	}

	public ArrayList<Enum> getEnums() {
		return enums;
	}

	public void addEnums(Enum e) {
		this.enums.add(e);
	}

	public ArrayList<Annotation> getAnnotations() {
		return annotations;
	}

	public void addAnnotations(Annotation a) {
		this.annotations.add(a);
	}
	
	public JSONArray getJOSN() {
		return convertPackage(structure);
	}
	
	private JSONArray convertPackage(ArrayList<Artifact> artifacts) {
		JSONArray array = new JSONArray();
		for (Artifact artifact : artifacts) {
			JSONObject json = artifact.toJSON();
			if (artifact instanceof Package) {
				json.put("contains", convertPackage(((Package) artifact).getConataints()));
			}
			array.put(json);
		}
		return array;
	}
}
