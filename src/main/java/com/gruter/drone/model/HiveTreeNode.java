package com.gruter.drone.model;

public class HiveTreeNode {
  private String category;
  private String text;
	private String value;
  private String connectionName;
  private HiveConnection hiveConn;
	private String iconCls;
  private String cls;
  private boolean leaf;
	
	public HiveTreeNode() {
	}

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getIconCls() {
    return iconCls;
  }

  public void setIconCls(String iconCls) {
    this.iconCls = iconCls;
  }

  public String getCls() {
    return cls;
  }

  public void setCls(String cls) {
    this.cls = cls;
  }

  public boolean isLeaf() {
    return leaf;
  }

  public void setLeaf(boolean leaf) {
    this.leaf = leaf;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getConnectionName() {
    return connectionName;
  }

  public void setConnectionName(String connectionName) {
    this.connectionName = connectionName;
  }

  public HiveConnection getHiveConn() {
    return hiveConn;
  }

  public void setHiveConn(HiveConnection hiveConn) {
    this.hiveConn = hiveConn;
  }

}
