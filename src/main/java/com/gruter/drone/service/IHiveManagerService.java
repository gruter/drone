package com.gruter.drone.service;

import java.util.List;

import org.apache.hadoop.fs.FileSystem;

import com.gruter.drone.model.HiveConnection;
import com.gruter.drone.model.HiveQuery;
import com.gruter.drone.model.HiveQueryCategory;
import com.gruter.drone.model.HiveTable;
import com.gruter.drone.model.HiveTreeNode;
import com.gruter.drone.model.HiveUser;
import com.gruter.drone.model.QueryResult;
import com.gruter.drone.model.QueryTask;
import com.gruter.drone.model.QueryTaskForView;
import com.gruter.drone.model.ResultFile;

public interface IHiveManagerService {

  public List<HiveQueryCategory> listCategory() throws Exception;

  public List<HiveTreeNode> getConnectionTree(String nodeValue, String category, String connectionName) throws Exception;

  public List<HiveConnection> getConnections() throws Exception;

  public void addConnection(HiveConnection conn) throws Exception;

  public void removeHiveConnection(String connectionName) throws Exception;

  public List<HiveTable> getTables(String connectionName, boolean includeColumn) throws Exception;
  
  public HiveTable getTable(String connectionName, String tableName) throws Exception;

  /**
   * 비동기적으로 query를 실행한다.
   * @param userName
   * @param connectionName
   * @param query
   * @return query 실행 taskid
   * @throws Exception
   */
  public String executeQuery(String userName, String connectionName, String query, boolean saveFile) throws Exception;
  
  public QueryTask getQueryTask(String taskId) throws Exception;
  
  public List<QueryTaskForView> getQueryTasksByUser(String userName) throws Exception;
  
  public QueryResult getQueryResult(String taskId) throws Exception;

  public String explain(String connectionName, String query) throws Exception;

  public void saveHiveQuery(HiveQuery hiveQuery) throws Exception;
  
  public void removeHiveQuery(String queryId) throws Exception;

  public void addQueryCategory(HiveQueryCategory hiveQueryCategory) throws Exception;

  public void removeQuery(String categoryName, int queryId) throws Exception;

  public List<HiveQuery> getQueryListForCategory() throws Exception;
  
  public List<ResultFile> getResultFileList(String taskId) throws Exception;
  
  public void deleteQueryTask(String taskId) throws Exception;

  public HiveConnection getConnectionInfo(String connectionName) throws Exception;

  public void saveConnection(HiveConnection conn) throws Exception;

  public FileSystem getFileSystem(String connectionName) throws Exception;

  public HiveUser getHiveUser(String userId) throws Exception;

  public String getPassword(String password) throws Exception;

}
