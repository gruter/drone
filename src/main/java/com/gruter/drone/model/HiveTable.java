package com.gruter.drone.model;

import java.util.List;

public class HiveTable {
  private String tableName;
  private List<HiveTableColumn> columns;
  public String getTableName() {
    return tableName;
  }
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
  public List<HiveTableColumn> getColumns() {
    return columns;
  }
  public void setColumns(List<HiveTableColumn> columns) {
    this.columns = columns;
  }
}
