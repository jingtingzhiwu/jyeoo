package com.wilkey.bean;

import java.io.Serializable;


/**
 * The persistent class for the sp_ques_item database table.
 * 
 */
public class SpQuesItem implements Serializable {

	private String id;
	
	private String nodeId;

	private Integer combinationTimes;

	private Double difficultyValue;

	private String district;

	private String guid;

	private String itemType;
	
	private String difficultyType;
	
	private String quesType;
	
	private String sourceType;
	
	private String picPath;

	private Integer reallyTimes;

	private String subject;

	private String title;

	private String titleHtml;

	private Integer years;

	public SpQuesItem(String id, String nodeId, String subject, String quesGuid, String title2, String titleHtml2, Integer combinationTimes2, Integer reallyTimes2, Double difficultyValue2, Integer years2, String district2, String itemType2, String diffType2, String quesType2, String sourceType2, String picPath2) {
		this.id = id;
		this.guid = quesGuid;
		this.nodeId = nodeId;
		this.subject = subject;
		this.title = title2;
		this.titleHtml = titleHtml2;
		this.combinationTimes = combinationTimes2;
		this.reallyTimes = reallyTimes2;
		this.difficultyValue = difficultyValue2;
		this.years = years2;
		this.district = district2;
		this.itemType = itemType2;
		this.difficultyType = diffType2;
		this.quesType = quesType2;
		this.sourceType = sourceType2;
		this.picPath = picPath2;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getCombinationTimes() {
		return this.combinationTimes;
	}

	public void setCombinationTimes(Integer combinationTimes) {
		this.combinationTimes = combinationTimes;
	}

	public Double getDifficultyValue() {
		return this.difficultyValue;
	}

	public void setDifficultyValue(Double difficultyValue) {
		this.difficultyValue = difficultyValue;
	}

	public String getDistrict() {
		return this.district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getItemType() {
		return this.itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public Integer getReallyTimes() {
		return this.reallyTimes;
	}

	public void setReallyTimes(Integer reallyTimes) {
		this.reallyTimes = reallyTimes;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleHtml() {
		return this.titleHtml;
	}

	public void setTitleHtml(String titleHtml) {
		this.titleHtml = titleHtml;
	}

	public Integer getYears() {
		return this.years;
	}

	public void setYears(Integer years) {
		this.years = years;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}

	public String getDifficultyType() {
		return difficultyType;
	}

	public void setDifficultyType(String diffType) {
		this.difficultyType = diffType;
	}

	public String getQuesType() {
		return quesType;
	}

	public void setQuesType(String quesType) {
		this.quesType = quesType;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

}