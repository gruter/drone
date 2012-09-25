package com.gruter.drone.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.gruter.drone.common.DroneSqlMapBase;
import com.gruter.drone.model.HiveConnection;
import com.gruter.drone.model.HiveQuery;
import com.gruter.drone.model.HiveQueryCategory;
import com.gruter.drone.model.HiveUser;
import com.gruter.drone.model.QueryTask;

@Repository("hiveManagerDAO")
public class HiveManagerDAO extends DroneSqlMapBase implements IHiveManagerDAO {
	private static String PREFIX = "hiveSQL.";

	@Override
	public void addHiveConnection(HiveConnection conn) throws Exception {
		this.sqlMapClient.insert(PREFIX + "addHiveConn", conn);
	}

	@Override
	public HiveConnection getHiveConnection(String connectionName) throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("connectionName", connectionName);
		return (HiveConnection) this.sqlMapClient.queryForObject(PREFIX + "listHiveConns", paramMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<HiveConnection> getHiveConnections() throws Exception {
		return this.sqlMapClient.queryForList(PREFIX + "listHiveConns");
	}

	@Override
	public void removeHiveConn(String connectionName) throws Exception {
		this.sqlMapClient.delete(PREFIX + "removeHiveConn", connectionName);
	}

	@Override
	public void updateHiveConn(HiveConnection conn) throws Exception {
		this.sqlMapClient.update(PREFIX + "updateHiveConn", conn);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<HiveQuery> getHiveQueryList(String categoryName) throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		if(categoryName != null) {
			paramMap.put("categoryName", categoryName);
		}
		return this.sqlMapClient.queryForList(PREFIX + "selectHiveQuery", paramMap);
	}
	
	@SuppressWarnings("unchecked")
	@Override
  public List<HiveQuery> getHiveQueryListWithEmptyCategory() throws Exception {
	  return this.sqlMapClient.queryForList(PREFIX + "selectHiveQueryOuter");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<HiveQueryCategory> getHiveQueryCategoryList() throws Exception {
		return this.sqlMapClient.queryForList(PREFIX + "selectHiveQueryCategory");
	}
	
	@Override
	public void insertHiveQuery(HiveQuery hiveQuery) throws Exception {
		this.sqlMapClient.insert(PREFIX + "insertHiveQuery", hiveQuery);
	}

	@Override
	public void insertHiveQueryCaterogy(HiveQueryCategory hiveQueryCategory) throws Exception {
		this.sqlMapClient.insert(PREFIX + "insertHiveQueryCategory", hiveQueryCategory);
	}
	
	@Override
	public HiveQuery findHiveQueryByName(String queryName) throws Exception {
		return (HiveQuery)this.sqlMapClient.queryForObject(PREFIX + "findHiveQueryByName", queryName);
	}

	@Override
	public void updateHiveQuery(HiveQuery hiveQuery) throws Exception {
		this.sqlMapClient.update(PREFIX + "updateHiveQuery", hiveQuery);
	}
	
	@Override
	public void renameHiveQueryCategory(String oldName, String newName) throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("oldName", oldName);
		paramMap.put("newName", newName);
		
		this.sqlMapClient.update(PREFIX + "renameHiveQueryCategory", paramMap);
	}

	@Override
	public void renameHiveCategoryAllQuery(String oldName, String newName) throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("oldName", oldName);
		paramMap.put("newName", newName);
		
		this.sqlMapClient.update(PREFIX + "renameHiveCategoryAllQuery", paramMap);
	}

	@Override
	public void deleteHiveQueryCategory(String categoryName) throws Exception {
		this.sqlMapClient.delete(PREFIX + "deleteHiveQueryCategory", categoryName);
	}

	@Override
	public void deleteHiveQueryByCategory(String categoryName) throws Exception {
		this.sqlMapClient.delete(PREFIX + "deleteHiveQueryByCategory", categoryName);
	}

	@Override
	public void deleteHiveQueryById(int id) throws Exception {
		this.sqlMapClient.delete(PREFIX + "deleteHiveQueryById", id);	
	}
	
  @Override
  public void insertQueryTask(QueryTask queryTask) throws Exception {
    this.sqlMapClient.insert(PREFIX + "insertQueryTask", queryTask); 
  }
  
  @Override
  public void updateQueryTask(QueryTask queryTask) throws Exception {
    this.sqlMapClient.delete(PREFIX + "updateQueryTask", queryTask); 
  }
  
  @Override
  public void deleteQueryTask(String taskId) throws Exception {
    this.sqlMapClient.delete(PREFIX + "deleteQueryTask", taskId); 
  }
  
  @Override
  public QueryTask getQueryTask(String taskId) throws Exception {
    return (QueryTask)this.sqlMapClient.queryForObject(PREFIX + "getQueryTask", taskId); 
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public List<QueryTask> getQueryTasksByName(String userName) throws Exception {
    Map<String, Object> paramMap = new HashMap<String, Object>();
    if(userName != null && !userName.isEmpty()) {
      paramMap.put("userName", userName);
    }
    return this.sqlMapClient.queryForList(PREFIX + "getQueryTasksByName", paramMap); 
  }
  
  @Override
  public HiveUser getHiveUser(String userId) throws Exception {
    return (HiveUser)this.sqlMapClient.queryForObject(PREFIX + "getHiveUser", userId); 
  }
  
  @Override
  public String getPassword(String password) throws Exception {
    return (String)this.sqlMapClient.queryForObject(PREFIX + "getPassword", password); 
  }
}
