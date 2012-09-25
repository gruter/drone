package com.gruter.drone.dao;

import java.util.List;

import com.gruter.drone.model.HiveConnection;
import com.gruter.drone.model.HiveQuery;
import com.gruter.drone.model.HiveQueryCategory;
import com.gruter.drone.model.HiveUser;
import com.gruter.drone.model.QueryTask;

public interface IHiveManagerDAO {

	public void addHiveConnection(HiveConnection conn) throws Exception;

	public HiveConnection getHiveConnection(String connectionName) throws Exception;

	public List<HiveConnection> getHiveConnections() throws Exception;

	public List<HiveQuery> getHiveQueryList(String categoryName) throws Exception;
	
	public List<HiveQueryCategory> getHiveQueryCategoryList() throws Exception;

	public void insertHiveQuery(HiveQuery hiveQuery) throws Exception;

	public void insertHiveQueryCaterogy(HiveQueryCategory hiveQueryCategory) throws Exception;

	public void updateHiveQuery(HiveQuery hiveQuery) throws Exception;

	public void renameHiveQueryCategory(String oldName, String newName) throws Exception;

	public void renameHiveCategoryAllQuery(String oldName, String newName) throws Exception;

	public void deleteHiveQueryCategory(String categoryName) throws Exception;

	public void deleteHiveQueryByCategory(String categoryName) throws Exception;

	public void deleteHiveQueryById(int id) throws Exception;

  public void removeHiveConn(String connectionName) throws Exception;

  public void updateHiveConn(HiveConnection conn) throws Exception;

  public HiveQuery findHiveQueryByName(String queryName) throws Exception;
  
  public void insertQueryTask(QueryTask queryTask) throws Exception;
  
  public void updateQueryTask(QueryTask queryTask) throws Exception;
  
  public void deleteQueryTask(String taskId) throws Exception;
  
  public QueryTask getQueryTask(String taskId) throws Exception;
  
  public List<QueryTask> getQueryTasksByName(String userName) throws Exception;

  public List<HiveQuery> getHiveQueryListWithEmptyCategory() throws Exception;
  
  public HiveUser getHiveUser(String userId) throws Exception;
  
  public String getPassword(String password) throws Exception;
}