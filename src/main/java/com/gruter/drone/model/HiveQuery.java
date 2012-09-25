package com.gruter.drone.model;

import java.sql.Timestamp;

public class HiveQuery {
  private int queryId;
	private String queryName;
	private String categoryName;
	private String query;
	private String creatorName;
	private Timestamp createDate;
	private String description;
	
	public String getQueryName() {
		return queryName;
	}
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getCreatorName() {
		return creatorName;
	}
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
  public int getQueryId() {
    return queryId;
  }
  public void setQueryId(int queryId) {
    this.queryId = queryId;
  }
}
