package com.gruter.drone.model;

public class ResultFile {
  private String path;
  private long createTime;
  private String length;
  
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }
  public long getCreateTime() {
    return createTime;
  }
  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  public String getLength() {
    return length;
  }
  public void setLength(String length) {
    this.length = length;
  }
}
