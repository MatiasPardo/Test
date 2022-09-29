package com.clouderp.flowchart.model;

public class NodeCloud {
	
	private String name;
	
	private String description;

	private String nameLink;
	
	private String link;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getNameLink() {
		return nameLink;
	}

	public void setNameLink(String nameLink) {
		this.nameLink = nameLink;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void assignHyperLink(String name, String url) {
		this.setNameLink(name);
		this.setLink(url);
	}
}
