package com.wilkey.bean;

import java.io.Serializable;
import java.util.Date;


/**
 * The persistent class for the sp_ques_item_opinion database table.
 * 
 */
public class SpQuesItemOpinion implements Serializable {

	private String id;

	private String itemId;

	private String opinion;
	
	public SpQuesItemOpinion() {
	}

	public SpQuesItemOpinion(String id, String itemId, String opinion) {
		this.id = id;
		this.itemId = itemId;
		this.opinion = opinion;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getItemId() {
		return this.itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getOpinion() {
		return this.opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

}