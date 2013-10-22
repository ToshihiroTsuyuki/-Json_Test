package com.example.pullcity;

public class CityItem {
	private String city;
	private String code;
	public CityItem(String city, String code) {
		super();
		this.city = city;
		this.code = code;
	}
	public String getCity() {
		return city;
	}
	public String getCode() {
		return code;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CityItem={");
		sb.append("city title =" + city);
		sb.append("city id =" + code);
		sb.append("}");
		return sb.toString();
	}
	
	
	
	
	
	
	
	
}
