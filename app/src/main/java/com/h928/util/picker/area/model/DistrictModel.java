package com.h928.util.picker.area.model;

public class DistrictModel {
	private String name;
	private String code;
	
	public DistrictModel() {
		super();
	}

	public DistrictModel(String name, String code) {
		super();
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "DistrictModel [name=" + name + ", code=" + code + "]";
	}

}
