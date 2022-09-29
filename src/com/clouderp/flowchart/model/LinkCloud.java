package com.clouderp.flowchart.model;

public class LinkCloud {

	private NodeCloud nodeStart;
	
	private NodeCloud nodeFinish; 

	private String description;
			
	public NodeCloud getNodeStart() {
		return nodeStart;
	}

	public void setNodeStart(NodeCloud nodeStart) {
		this.nodeStart = nodeStart;
	}

	public NodeCloud getNodeFinish() {
		return nodeFinish;
	}

	public void setNodeFinish(NodeCloud nodeFinish) {
		this.nodeFinish = nodeFinish;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
