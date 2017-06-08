package com.wilkey.bean;

import java.io.Serializable;

/**
 * The persistent class for the sp_ques_item database table.
 * 
 */
public class SpReportItem implements Serializable {

	private String id;
	
	private String typeId;

	private Integer combinationTimes;

	private Double difficultyValue;

	private String district;

	private String guid;

	private String itemType;

	private String picPath;

	private Integer reallyTimes;

	private String title;

	private String titleHtml;

	private Integer years;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public Integer getCombinationTimes() {
		return combinationTimes;
	}

	public void setCombinationTimes(Integer combinationTimes) {
		this.combinationTimes = combinationTimes;
	}

	public Double getDifficultyValue() {
		return difficultyValue;
	}

	public void setDifficultyValue(Double difficultyValue) {
		this.difficultyValue = difficultyValue;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}

	public Integer getReallyTimes() {
		return reallyTimes;
	}

	public void setReallyTimes(Integer reallyTimes) {
		this.reallyTimes = reallyTimes;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleHtml() {
		return titleHtml;
	}

	public void setTitleHtml(String titleHtml) {
		this.titleHtml = titleHtml;
	}

	public Integer getYears() {
		return years;
	}

	public void setYears(Integer years) {
		this.years = years;
	}

	public SpReportItem(String id, String typeId, Integer combinationTimes, Double difficultyValue, String district, String guid, String itemType, String picPath, Integer reallyTimes, String title, String titleHtml,
			Integer years) {
		this.id = id;
		this.typeId = typeId;
		this.combinationTimes = combinationTimes;
		this.difficultyValue = difficultyValue;
		this.district = district;
		this.guid = guid;
		this.itemType = itemType;
		this.picPath = picPath;
		this.reallyTimes = reallyTimes;
		this.title = title;
		this.titleHtml = titleHtml;
		this.years = years;
	}

}