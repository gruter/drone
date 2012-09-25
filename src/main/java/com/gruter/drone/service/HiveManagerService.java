package com.gruter.drone.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.service.HiveClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gruter.drone.dao.IHiveManagerDAO;
import com.gruter.drone.model.HiveConnection;
import com.gruter.drone.model.HiveQuery;
import com.gruter.drone.model.HiveQueryCategory;
import com.gruter.drone.model.HiveTable;
import com.gruter.drone.model.HiveTableColumn;
import com.gruter.drone.model.HiveTreeNode;
import com.gruter.drone.model.HiveUser;
import com.gruter.drone.model.QueryResult;
import com.gruter.drone.model.QueryTask;
import com.gruter.drone.model.QueryTaskForView;
import com.gruter.drone.model.ResultFile;

@Service("hiveManagerService")
public class HiveManagerService implements IHiveManagerService, IQueryFinishListener {
  protected static final Log LOG = LogFactory.getLog(HiveManagerService.class);
  
  public static ObjectMapper om = new ObjectMapper();
  static {
    om.getDeserializationConfig().disable(
        DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
  }
  
  private static final String OVERWRITE_PREFIX = "INSERT OVERWRITE DIRECTORY ";
  
  @Autowired
  private IHiveManagerDAO hiveManagerDao;
  
  private Map<String, HiveConnection> connections = new HashMap<String, HiveConnection>();
  
  private Configuration conf;
  
  private int maxRow = 1000;
  
  private int queryResultStoreTime;
  
  private ExecutorService queryExecutor = Executors.newFixedThreadPool(5);
  
  private Map<String, QueryTask> queryTasks = new HashMap<String, QueryTask>();
  
  private String queryResultHdfsPathRoot;
  
  public HiveManagerService() {
    conf = new Configuration();
    conf.addResource("drone-site.xml");
    
    maxRow = conf.getInt("drone.max.row", 1000);
    
    queryResultHdfsPathRoot = conf.get("drone.result.hdfs.path", "/tmp/drone");
    
    queryResultStoreTime = conf.getInt("drone.query.result.store.time", 3 * 24 * 60) * 60 * 1000;  //3day
    
    queryExecutor = Executors.newFixedThreadPool(conf.getInt("drone.query.concurrency", 5));
    
//    momory cleaner    
//    Thread taskCleaner = new Thread() {
//      public void run() {
//        while(true) {
//          try { 
//            Collection<QueryTask> tmpQueryTasks = null;
//            
//            List<String> cleanTargets = new ArrayList<String>();
//            synchronized(queryTasks) {
//              tmpQueryTasks = queryTasks.values();
//            }
//            
//            for(QueryTask queryTask: tmpQueryTasks) {
//              if(queryTask.isFinished() && ((System.currentTimeMillis() - queryTask.getEndTime().getTime()) > queryResultStoreTime)) {
//                cleanTargets.add(queryTask.getTaskId());
//              }
//            }
//            
//            synchronized(queryTasks) {
//              for(String eachId: cleanTargets) {
//                LOG.info("Job cleaned: " + eachId);
//                queryTasks.remove(eachId);
//              }
//            }
//          } catch (Exception e) {
//            LOG.error(e.getMessage(), e);
//          }
//          
//          try {
//            Thread.sleep(5 * 60 * 1000);
//          } catch (InterruptedException e) {
//          }
//        }
//      }
//    };
    
      Thread taskCleaner = new Thread() {
      public void run() {
        while(true) {
          try { 
            if(hiveManagerDao == null) {
              Thread.sleep(10 * 1000);
              continue;
            }
            List<QueryTask> queryTasks = hiveManagerDao.getQueryTasksByName(null);
            
            for(QueryTask queryTask: queryTasks) {
              if(queryTask.isFinished() && ((System.currentTimeMillis() - queryTask.getEndTime().getTime()) > queryResultStoreTime)) {
                deleteTaskResult(queryTask);
              }
            }
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
          }
          
          try {
            Thread.sleep(5 * 60 * 1000);
          } catch (InterruptedException e) {
          }
        }
      }
    };    
    
    taskCleaner.start();
  }
  
  private void deleteTaskResult(QueryTask queryTask) {
    try {
      FileSystem fs = HiveManagerService.this.getFileSystem(queryTask.getConnectionName());
      if(fs == null) {
        return;
      }
      if(queryTask.getResultPath().indexOf(queryResultHdfsPathRoot) < 0) {
        LOG.warn("Wrong result data path:" + queryTask.getResultPath());
        return;
      }
      fs.delete(new Path(queryTask.getResultPath()), true);
      hiveManagerDao.deleteQueryTask(queryTask.getTaskId());
      LOG.info("Query reuslt cleaned: " + queryTask.getTaskId());
    } catch (Exception e) {
      LOG.error(queryTask.getTaskId() + "," + e.getMessage(), e);
    }
  }
  
  protected int getMaxRow() {
    return maxRow;
  }
  
  @Override
  public List<HiveQueryCategory> listCategory() throws Exception {
    return this.hiveManagerDao.getHiveQueryCategoryList();
  }

  @Override
  public List<HiveTreeNode> getConnectionTree(String nodeValue, String category, String connName) throws Exception {
    List<HiveTreeNode> result = new ArrayList<HiveTreeNode>();
    HiveTreeNode node;
    if ("root".equals(category)) {
      for (HiveConnection conn : this.getConnections()) {
        node = new HiveTreeNode();
        node.setValue(conn.getConnectionName());
        node.setText(conn.getConnectionName());
        node.setConnectionName(conn.getConnectionName());
        node.setHiveConn(conn);
        node.setCategory("connection");
        node.setIconCls("cluster-running");
        node.setLeaf(false);
        result.add(node);
      }
    } else if ("connection".equals(category)) {
      List<HiveTable> tables = getTables(connName, false);
      
      for (HiveTable eachTable : tables) {
        node = new HiveTreeNode();
        node.setConnectionName(connName);
        node.setValue(eachTable.getTableName());
        node.setText(eachTable.getTableName());
        node.setLeaf(true);
        node.setCategory("table");
        node.setIconCls("databaseicon");
        result.add(node);
      }
    }
    return result;
  }
  
  protected HiveConnection getHiveConnection(String connectionName) throws Exception {
    synchronized(connections) {
      if(connections.isEmpty() || connections.get(connectionName) == null) {
        for(HiveConnection eachConn: getConnections()) {
          connections.put(eachConn.getConnectionName(), eachConn);
        }
      }
      
      return connections.get(connectionName);
    }
  }
  
  protected Connection getConnection(String connectionName) throws Exception {
    if(getHiveConnection(connectionName) == null) {
      throw new Exception("No connection info:" + connectionName);
    }
    
    HiveConnection hConn = connections.get(connectionName);
    try {
      Class.forName(hConn.getDriverClass());
    } catch (ClassNotFoundException e) {
      LOG.error(e.getMessage(), e);
      throw e;
    }
    Connection conn = DriverManager.getConnection(hConn.getConnectionUri(), "", "");
    
    return conn;
  }
  
  protected void closeConn(Connection conn, Statement stmt, ResultSet rs) {
    if(rs != null) {
      try {
        rs.close();
      } catch (Exception e) {
      }
    }
    if(stmt != null) {
      try {
        stmt.close();
      } catch (Exception e) {
      }
    }
    if(conn != null) {
      try {
        conn.close();
      } catch (Exception e) {
      }
    }
  }
  
  @Override
  public HiveTable getTable(String connectionName, String tableName) throws Exception {
    Connection conn = getConnection(connectionName);
    try {
      HiveTable hiveTable = new HiveTable();
      hiveTable.setTableName(tableName);
      hiveTable.setColumns(getTableColumns(conn, tableName));
      
      return hiveTable;
    } finally {
      closeConn(conn, null, null);
    }
  }
    
  @Override
  public List<HiveTable> getTables(String connectionName, boolean includeColumn) throws Exception {
    Connection conn = getConnection(connectionName);
    ResultSet rs1 = null;
    List<HiveTable> tables = new ArrayList<HiveTable>();
    try {
      DatabaseMetaData meta = conn.getMetaData();
      rs1 = meta.getTables(null, null, null, null);
      while(rs1.next()) {
        String tableName = rs1.getString("TABLE_NAME");
        HiveTable hiveTable = new HiveTable();
        hiveTable.setTableName(tableName);
        tables.add(hiveTable);
        
        if(includeColumn) {
          hiveTable.setColumns(getTableColumns(conn, tableName));
        }
      }
    } finally {
      closeConn(conn, null, rs1);
    }
    return tables;
  }
  
  private List<HiveTableColumn> getTableColumns(Connection conn, String tableName) throws Exception {
    DatabaseMetaData meta = conn.getMetaData();
    ResultSet rs2 = null;
    try {
      rs2 = meta.getColumns(null, null, tableName, null);
      List<HiveTableColumn> columns = new ArrayList<HiveTableColumn>();
      while(rs2.next()) {
        HiveTableColumn column = new HiveTableColumn();
        column.setColumnName(rs2.getString("COLUMN_NAME"));
        column.setDataType(rs2.getString("TYPE_NAME"));

        columns.add(column);
      }
      return columns;
    } finally {
      if(rs2 != null) {
        rs2.close();
      }
    }

  }
  @Override
  public List<HiveConnection> getConnections() throws Exception {
    return hiveManagerDao.getHiveConnections();
  }
  
  @Override
  public void addConnection(HiveConnection conn) throws Exception {
    hiveManagerDao.addHiveConnection(conn);
  }

  @Override
  public void removeHiveConnection(String connectionName) throws Exception {
    hiveManagerDao.removeHiveConn(connectionName);
  }

  @Override
  public String executeQuery(String userName, String connectionName, String query, boolean saveFile) throws Exception {
    String taskId = userName + "_" + System.currentTimeMillis();
    String resultPath = queryResultHdfsPathRoot + "/" + userName + "/" + taskId;
    
    if(saveFile) {
      query = OVERWRITE_PREFIX + "'" + resultPath + "' " + query;
    }

    QueryTask queryTask = new QueryTask();
    queryTask.setTaskId(taskId);
    queryTask.setConnectionName(connectionName);
    queryTask.setResultPath(resultPath);
    queryTask.setUserName(userName);
    queryTask.setQuery(query);
    queryTask.setStatus("WAITING");
    queryTask.setStartTime(new Timestamp(System.currentTimeMillis()));
    queryTask.setSaveResult(saveFile);
    
    QueryRunner queryRunner = new QueryRunner(this, this, queryTask);
    queryTask.setQueryRunner(queryRunner);

    queryExecutor.execute(queryRunner);
    
    synchronized(queryTasks) {
      queryTasks.put(taskId, queryTask);
    }
    hiveManagerDao.insertQueryTask(queryTask);
    
    return taskId;
  }
  
  @Override
  public void finishedQuery(QueryTask queryTask) {
    LOG.info("Query [" + queryTask.getTaskId() + " finished");
    try {
      hiveManagerDao.updateQueryTask(queryTask);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    synchronized(queryTasks) {
      queryTasks.remove(queryTask.getTaskId());
    }
  }
  
  @Override
  public QueryTask getQueryTask(String taskId) throws Exception {
    QueryTask queryTask = null;
    synchronized(queryTasks) {
      queryTask = queryTasks.get(taskId);
    }
    
    if(queryTask == null) {
      queryTask = this.hiveManagerDao.getQueryTask(taskId);
    } 
    
    return queryTask;
  }

  @Override
  public List<ResultFile> getResultFileList(String taskId) throws Exception {
    QueryTask queryTask = this.getQueryTask(taskId);
    
    List<ResultFile> result = new ArrayList<ResultFile>();
    if(queryTask == null || !queryTask.isFinished()) {
      return result;
    }
    
    FileSystem fs = this.getFileSystem(queryTask.getConnectionName());
    if(fs == null) {
      return result;
    }
    
    FileStatus[] fileStatusList = fs.listStatus(new Path(queryTask.getResultPath()));
    
    DecimalFormat df = new DecimalFormat("###,###,###.#");
    if(fileStatusList != null) {
      for(FileStatus eachFile: fileStatusList) {
        String path = eachFile.getPath().getName();
        if(path.startsWith("_")) {
          continue;
        }
        
        ResultFile resultFile = new ResultFile();
        resultFile.setPath(path);
        resultFile.setCreateTime(eachFile.getModificationTime());
        
        long size = eachFile.getLen();
        if(size < 1024) {
          resultFile.setLength(df.format(size) + " Bytes");
        } else if(size >= 1024 && size < 1024 * 1024) {
          resultFile.setLength(df.format(((double)size/1024.0)) + " KB");
        } else if(size >= 1024 * 1024 && size < 1024 * 1024 * 1024) {
          resultFile.setLength(df.format(((double)size/(1024.0 * 1024.0))) + " MB");
        } else {
          resultFile.setLength(df.format(((double)size/(1024.0 * 1024.0 * 1024.0))) + " GB");
        }
        
        result.add(resultFile);
      }
    }
    
    return result;
  }
  
  @Override
  public void deleteQueryTask(String taskId) throws Exception {
    QueryTask queryTask = this.getQueryTask(taskId);
    if(queryTask != null) {
      this.deleteTaskResult(queryTask);
    }
  }
  
  @Override
  public List<QueryTaskForView> getQueryTasksByUser(String userName) throws Exception {
    List<QueryTaskForView> queryTaskResult = new ArrayList<QueryTaskForView>();
    
    Set<String> memoryTaskIds = new HashSet<String>();
    synchronized(queryTasks) {
      for(QueryTask eachQueryTask: queryTasks.values()) {
        if(eachQueryTask.getUserName().equals(userName)) {
          queryTaskResult.add(eachQueryTask.copyQueryTask());
          memoryTaskIds.add(eachQueryTask.getTaskId());
        }
      }
    }

    //get task from db
    List<QueryTask> dbQueryTasks = this.hiveManagerDao.getQueryTasksByName(userName);
    if(dbQueryTasks != null) {
      for(QueryTask eachQueryTask: dbQueryTasks) {
        if(!memoryTaskIds.contains(eachQueryTask.getTaskId())) {
          queryTaskResult.add(eachQueryTask.copyQueryTask());
        }
      }
    }
    
    Collections.sort(queryTaskResult, new Comparator<QueryTaskForView>() {
      @Override
      public int compare(QueryTaskForView o1, QueryTaskForView o2) {
        return o2.getTaskId().compareTo(o1.getTaskId());
      }
    });
    return queryTaskResult;
  }

//  public FileSystem getFileSystemByTaskId(String taskId) throws Exception {
//    QueryTask queryTask = this.getQueryTask(taskId);
//    if(queryTask == null) {
//      return null;
//    }
//    
//    return this.getFileSystem(queryTask.getConnectionName());
//  }
  
  @Override
  public QueryResult getQueryResult(String taskId) throws Exception {
    QueryTask queryTask = this.getQueryTask(taskId);
    if(queryTask == null) {
      QueryResult queryResult = new QueryResult();
      queryResult.setTotalRows(-1);
      return queryResult;
    }
    
    if(!queryTask.isFinished()) {
      QueryResult queryResult = new QueryResult();
      queryResult.setTotalRows(-1);
      return queryResult;
    } else {
      if(queryTask.isError()) {
        QueryResult queryResult = new QueryResult();
        queryResult.setError(true);
        queryResult.setErrorMessage(queryTask.getErrorMessage());
        return queryResult;
      }
      if(queryTask.isSaveResult()) {
        QueryResult queryResult = new QueryResult();
        queryResult.setResultPath(queryTask.getResultPath());
        queryResult.setColumnNames(Arrays.asList(new String[]{"data"}));
        List<List<Object>> data = readDataFromResultFile(queryTask);
        
        if(data != null && !data.isEmpty()) {
          int columnCount = data.get(0).size();
          if(columnCount > 0) {
            queryResult.setColumnNames(new ArrayList<String>());
            for(int i = 0; i < columnCount; i++) {
              queryResult.getColumnNames().add("data" + (i+1));
            }
          }
        }
        queryResult.setResultDatas(data);
        return queryResult;
      } else {
        FileSystem fs = this.getFileSystem(queryTask.getConnectionName());
        if(fs == null) {
          QueryResult queryResult = new QueryResult();
          queryResult.setTotalRows(-1);
          return queryResult;
        }
        InputStream in = fs.open(new Path(queryTask.getResultPath() + "/part-00000"));
        try {
          QueryResult queryResult = om.readValue(in, QueryResult.class);
          return queryResult;
        } finally {
          if(in != null) {
            in.close();
          }
        }
      }
    }
  }

  private List<List<Object>> readDataFromResultFile(QueryTask queryTask) throws Exception {
    List<List<Object>> result = new ArrayList<List<Object>>();
    
    FileSystem fs = this.getFileSystem(queryTask.getConnectionName());
    if(fs == null) {
      return result;
    }
    
    BufferedReader reader = null;
    try {
      Path parentPath = new Path(queryTask.getResultPath());
      if(!fs.exists(parentPath)) {
        LOG.warn("No output file: " + parentPath);
        return result;
      }
      FileStatus[] fileStatusList = fs.listStatus(new Path(queryTask.getResultPath()));
      if(fileStatusList == null || fileStatusList.length == 0) {
        LOG.warn("No output file in: " + parentPath);
        return result;
      }
      Path path = null;
      for(FileStatus eachFile: fileStatusList) {
        if(!eachFile.getPath().getName().startsWith("_")) {
          path = eachFile.getPath();
          break;
        }
      }
      
      if(path == null) {
        LOG.warn("All files start with '_' in: " + parentPath);
        return result;
      }
      reader = new BufferedReader(new InputStreamReader(fs.open(path)));
      int count = 0;
      
      String line = null;

      while( (line = reader.readLine()) != null) {
        String[] tokens = line.split("\\0001");
        List<Object> row = new ArrayList<Object>();
        for(String eachToken: tokens) {
          row.add(eachToken);
        }
        
        result.add(row);
        count++;
        if(count >= maxRow) {
          break;
        }
      }
    } finally {
      if(reader != null) {
        reader.close();
      }
    }
    
    return result;
  }
  
  private static Object[] getHiveServerNetInfo(String jdbcUri) throws Exception {
    if(!jdbcUri.startsWith("jdbc:")) {
      return null;
    }
    URI uri = new URI(jdbcUri.substring(5));
    
    return new Object[]{uri.getHost(), uri.getPort()};
  }
  
  @Override
  public synchronized String explain(String connectionName, String query) throws Exception {
    LOG.info("Explain:" + query);
    HiveConnection hiveConn = getConnectionInfo(connectionName);
    Object[] connInfo = getHiveServerNetInfo(hiveConn.getConnectionUri());
    
    if(connInfo == null) {
      throw new Exception("Wrong connection uri:" + hiveConn.getConnectionUri());
    }
    TTransport transport = null;
    HiveClient client = null;
    try {
      transport = new TSocket(connInfo[0].toString(), (Integer)connInfo[1]);
      TProtocol protocol = new TBinaryProtocol(transport);
      client = new HiveClient(protocol);
      transport.open();
      
      client.execute("explain " + query);
      
      StringBuilder sb = new StringBuilder(1024);
      List<String> result = client.fetchAll();
      for(String eachResult: result) {
        sb.append(eachResult).append("\n");
      }
      
      return sb.toString();
    } finally {
      if(transport != null) {
        transport.close();
      }
    }
  }
  
  @Override
  public void saveHiveQuery(HiveQuery hiveQuery) throws Exception {
    this.hiveManagerDao.insertHiveQuery(hiveQuery);
  }
  
  @Override
  public void removeHiveQuery(String queryId) throws Exception {
    this.hiveManagerDao.deleteHiveQueryById(Integer.parseInt(queryId));
  }

  @Override
  public void addQueryCategory(HiveQueryCategory hiveQueryCategory) throws Exception {
    this.hiveManagerDao.insertHiveQueryCaterogy(hiveQueryCategory);
  }

  @Override
  public void removeQuery(String categoryName, int id) throws Exception {
    if(id <= 0) {
      this.hiveManagerDao.deleteHiveQueryCategory(categoryName);
    } else {
      this.hiveManagerDao.deleteHiveQueryById(id);
    }
  }
  
  @Override
  public List<HiveQuery> getQueryListForCategory() throws Exception {
    List<HiveQuery> queryList = this.hiveManagerDao.getHiveQueryListWithEmptyCategory();
  
    List<HiveQuery> result = new ArrayList<HiveQuery>();
    
    if(queryList == null) {
      return result;
    }
    
    String previousCategoryName = null;
    for(HiveQuery eachQuery: queryList) {
      if(previousCategoryName == null || !previousCategoryName.equals(eachQuery.getCategoryName())) {
        HiveQuery hiveQuery = new HiveQuery();
        hiveQuery.setCategoryName(eachQuery.getCategoryName());
        result.add(hiveQuery);
      }
      if(eachQuery.getQueryId() >= 0) {
        result.add(eachQuery);
      }
      previousCategoryName = eachQuery.getCategoryName();
    }
    
    return result;
  }
  
  @Override
  public FileSystem getFileSystem(String connectionName) throws Exception {
    HiveConnection hiveConn = getHiveConnection(connectionName);
    if(hiveConn == null) {
      throw new Exception("No conneciton info:" + connectionName);
    }
    if(hiveConn.getFileSystemUri() == null || hiveConn.getFileSystemUri().isEmpty()) {
      LOG.warn("No file system uri: " + connectionName);
      return null;
    }
    FileSystem fs = FileSystem.get(new URI(hiveConn.getFileSystemUri()), conf);
    return fs;
  }
  

  @Override
  public HiveConnection getConnectionInfo(String connectionName) throws Exception {
    return getHiveConnection(connectionName);
  }

  @Override
  public void saveConnection(HiveConnection conn) throws Exception {
    hiveManagerDao.updateHiveConn(conn);
  }
  
  @Override
  public HiveUser getHiveUser(String userId) throws Exception {
    return hiveManagerDao.getHiveUser(userId);
  }
  
  public String getPassword(String password) throws Exception {
    return hiveManagerDao.getPassword(password);
  }
  
  public static void main(String[] args) throws Exception {
    Object[] serverInfo = HiveManagerService.getHiveServerNetInfo("jdbc:hive://localhost:10000/default");
    
    System.out.println(serverInfo[0] + "," + serverInfo[1]);
//    try {
//      Class.forName("org.apache.hadoop.hive.jdbc.HiveDriver");
//    } catch (ClassNotFoundException e) {
//      
//    }
//    Connection conn = DriverManager.getConnection("jdbc:hive://localhost:10000/default", "", "");
//    Statement stmt = conn.createStatement();
//    ResultSet rs = stmt.executeQuery("explain select * from test order by col2");
////    ResultSet rs = stmt.executeQuery("select * from test");
//    ResultSetMetaData rsmd = rs.getMetaData();
//    
//    System.out.println(">>>>>>>>>>>>ColumnCount:" + rsmd.getColumnCount());
//    for(int i = 0; i < rsmd.getColumnCount(); i++) {
//      System.out.println("COLUMN>" + rsmd.getColumnName(i + 1));
//    }
//    
//    int count = 0;
//    while(rs.next()) {
//      System.out.println(">>>>>>>>>>>" + rs.getObject(1));
//      count++;
//      
//      if(count > 10) {
//        break;
//      }
//    }
  }
}
