package com.wilkey.bean;

import java.io.Serializable;
import java.util.Date;


/**
 * The persistent class for the sp_report_type database table.
 * 
 */
public class SpReportType implements Serializable {

	private String id;

	private Double difficultyValue;

	private String district;

	private Integer downloadTimes;

	private String guid;

	private Integer score;

	private String title;

	private Integer viewTimes;

	private Integer years;
	
	private String subject;

	public SpReportType() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Integer getDownloadTimes() {
		return downloadTimes;
	}

	public void setDownloadTimes(Integer downloadTimes) {
		this.downloadTimes = downloadTimes;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getViewTimes() {
		return viewTimes;
	}

	public void setViewTimes(Integer viewTimes) {
		this.viewTimes = viewTimes;
	}

	public Integer getYears() {
		return years;
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

	public SpReportType(String id, Double difficultyValue, String district, Integer downloadTimes, String guid, Integer score, String title, Integer viewTimes, Integer years, String subject) {
		this.id = id;
		this.difficultyValue = difficultyValue;
		this.district = district;
		this.downloadTimes = downloadTimes;
		this.guid = guid;
		this.score = score;
		this.title = title;
		this.viewTimes = viewTimes;
		this.years = years;
		this.subject = subject;
	}
}