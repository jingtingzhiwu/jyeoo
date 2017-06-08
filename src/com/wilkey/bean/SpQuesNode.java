package com.wilkey.bean;

import java.io.Serializable;

/**
 * The persistent class for the sp_ques_category database table.
 * 
 */
public class SpQuesNode implements Serializable {

	private String id;

	private String nodeId;
	
	private int nodeLevel;

	private String nodeName;

	private String parentNodeId;

	private String nodeType;

	private String bookVersion;

	private String bookClass;

	private String subject;

	public SpQuesNode() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getParentNodeId() {
		return parentNodeId;
	}

	public void setParentNodeId(String parentNodeId) {
		this.parentNodeId = parentNodeId;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getBookVersion() {
		return bookVersion;
	}

	public void setBookVersion(String bookVersion) {
		this.bookVersion = bookVersion;
	}

	public String getBookClass() {
		return bookClass;
	}

	public void setBookClass(String bookClass) {
		this.bookClass = bookClass;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getNodeLevel() {
		return nodeLevel;
	}

	public void setNodeLevel(int nodeLevel) {
		this.nodeLevel = nodeLevel;
	}
	
	public SpQuesNode(String id, String nodeId, String nodeName, String parentNodeId, String nodeType, String bookVersion, String bookClass, String subject, int nodeLevel) {
		super();
		this.id = id;
		this.nodeId = nodeId;
		this.nodeLevel = nodeLevel;
		this.nodeName = nodeName;
		this.parentNodeId = parentNodeId;
		this.nodeType = nodeType;
		this.bookVersion = bookVersion;
		this.bookClass = bookClass;
		this.subject = subject;
	}

}