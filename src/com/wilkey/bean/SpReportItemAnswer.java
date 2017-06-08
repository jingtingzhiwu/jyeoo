package com.wilkey.bean;

import java.io.Serializable;
import java.util.Date;


/**
 * The persistent class for the sp_report_item_answer database table.
 * 
 */
public class SpReportItemAnswer implements Serializable {

	private String id;

	private String answerComment;

	private String answerHtml;

	private String answerNote;

	private String answerOpinion;

	private String answerPoint;

	private int itemId;

	public SpReportItemAnswer() {
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAnswerComment() {
		return this.answerComment;
	}

	public void setAnswerComment(String answerComment) {
		this.answerComment = answerComment;
	}

	public String getAnswerHtml() {
		return this.answerHtml;
	}

	public void setAnswerHtml(String answerHtml) {
		this.answerHtml = answerHtml;
	}

	public String getAnswerNote() {
		return this.answerNote;
	}

	public void setAnswerNote(String answerNote) {
		this.answerNote = answerNote;
	}

	public String getAnswerOpinion() {
		return this.answerOpinion;
	}

	public void setAnswerOpinion(String answerOpinion) {
		this.answerOpinion = answerOpinion;
	}

	public String getAnswerPoint() {
		return this.answerPoint;
	}

	public void setAnswerPoint(String answerPoint) {
		this.answerPoint = answerPoint;
	}

	public int getItemId() {
		return this.itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

}