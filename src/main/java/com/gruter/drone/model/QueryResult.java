package com.gruter.drone.model;

import java.util.List;

public class QueryResult {
  private long totalRows;
  private boolean error;
  private String errorMessage;
  private List<String> columnNames;
  private List<List<Object>> resultDatas;
  
  private String resultPath;

  public List<String> getColumnNames() {
    return columnNames;
  }

  public void setColumnNames(List<String> columnNames) {
    this.columnNames = columnNames;
  }

  public List<List<Object>> getResultDatas() {
    return resultDatas;
  }

  public void setResultDatas(List<List<Object>> resultDatas) {
    this.resultDatas = resultDatas;
  }

  public String getResultPath() {
    return resultPath;
  }

  public void setResultPath(String resultPath) {
    this.resultPath = resultPath;
  }

  public long getTotalRows() {
    return totalRows;
  }

  public void setTotalRows(long totalRows) {
    this.totalRows = totalRows;
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
