package com.clouderp.flowchart.model;

import java.util.*;

import org.openxava.application.meta.MetaApplications;

public class FlowCloud {
	
	private String name;
	
	private String application;
	
	private Collection<LinkCloud> link = new LinkedList<LinkCloud>();

	private HashMap<String, NodeCloud> nodes = new HashMap<String, NodeCloud>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<LinkCloud> getLink() {
		return link;
	}

	public void setLink(Collection<LinkCloud> link) {
		this.link = link;
	}
	
	public String getApplication() {
		if (this.application == null){
			this.application = MetaApplications.getApplicationsNames().iterator().next().toString();
		}
		return this.application;
	}
	
	public NodeCloud addNode(String name){
		NodeCloud node = null;
		if (!this.getNodes().containsKey(name)){
			node = new NodeCloud();
			node.setName(name);
			this.getNodes().put(name, node);
		}
		else{
			node = this.getNodes().get(name);
		}
		return node;
	}
	
	private Map<String, NodeCloud> getNodes(){
		return nodes;
	}

	public LinkCloud addLink(NodeCloud start, NodeCloud finish) {
		LinkCloud link = new LinkCloud();
		link.setNodeStart(start);
		link.setNodeFinish(finish);
		this.getLink().add(link);
		return link;
	}
}
