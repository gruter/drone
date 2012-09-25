package com.gruter.drone.model;


public class HiveConnection {
  private String connectionName;
  private String driverClass;
  private String connectionUri;
  private String fileSystemUri;
  private String description;
  
  public String getConnectionName() {
    return connectionName;
  }
  public void setConnectionName(String connectionName) {
    this.connectionName = connectionName;
  }
  public String getConnectionUri() {
    return connectionUri;
  }
  public void setConnectionUri(String connectionUri) {
    this.connectionUri = connectionUri;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String getDriverClass() {
    return driverClass;
  }
  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }
  public String getFileSystemUri() {
    return fileSystemUri;
  }
  public void setFileSystemUri(String fileSystemUri) {
    this.fileSystemUri = fileSystemUri;
  }
}
