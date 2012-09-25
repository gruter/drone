package com.gruter.drone.service;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.gruter.drone.common.DroneUtil;
import com.gruter.drone.model.QueryResult;
import com.gruter.drone.model.QueryTask;

public class QueryRunner extends Thread {
  protected static final Log LOG = LogFactory.getLog(QueryRunner.class);
  
  private QueryTask queryTask;
  private HiveManagerService service;
  private IQueryFinishListener queryFinishListener;
  
  public QueryRunner(HiveManagerService service, IQueryFinishListener queryFinishListener, QueryTask queryTask) {
    this.service = service;
    this.queryTask = queryTask;
    this.queryFinishListener = queryFinishListener;
  }
  
  public void run() {
    queryTask.setStatus("RUNNING");
    try {
      Connection conn = service.getConnection(queryTask.getConnectionName());
      if(conn == null) {
        throw new Exception("No connection info:" + queryTask.getConnectionName());
      }
      Statement stmt = null;
      ResultSet rs = null;
      try {
        stmt = conn.createStatement();
        
        LOG.info("[" + queryTask.getQuery() + "]");
        rs = stmt.executeQuery(queryTask.getQuery());
        
        ResultSetMetaData rsmd = rs.getMetaData();
        
        List<String> columnNames = new ArrayList<String>();
        
        for(int i = 0; i < rsmd.getColumnCount(); i++) {
          columnNames.add(rsmd.getColumnName(i + 1));
        }
        
        List<List<Object>> resultData = new ArrayList<List<Object>>();
        
        long totalCount = 0;
        while(rs.next()) {
          totalCount++;
          if(totalCount >= service.getMaxRow()) {
           //continue;
            break;
          }
          //Map<String, Object> resultRow = new HashMap<String, Object>();
          List<Object> rowData = new ArrayList<Object>();
          for(int i = 0; i < rsmd.getColumnCount(); i++) {
            //String columnName = rsmd.getColumnName(i + 1);
            Object value = rs.getObject(i + 1);
            rowData.add(value);
            //resultRow.put(columnName, value);
          } 
          
          resultData.add(rowData);
        }
        QueryResult queryResult = new QueryResult();
        queryResult.setColumnNames(columnNames);
        queryResult.setResultDatas(resultData);
        queryResult.setResultPath(queryTask.getResultPath());
        queryResult.setTotalRows(totalCount);
        
//        queryTask.setQueryResult(queryResult);
        
        saveQueryResult(queryResult);
        queryTask.setError(false);
        
        LOG.info("Query Result:" + queryResult.getTotalRows());
      } finally {
        service.closeConn(conn, stmt, rs);
      }
    } catch (Exception e) {
      queryTask.setErrorMessage(DroneUtil.stringifyException(e));
      queryTask.setError(true);
      LOG.error(e.getMessage(), e);
    } finally {
      queryTask.setStatus("FINISHED");
      queryTask.setFinished(true);
      queryTask.setEndTime(new Timestamp(System.currentTimeMillis()));
      queryFinishListener.finishedQuery(queryTask);
    }
  }
  
  private void saveQueryResult(QueryResult queryResult) throws Exception {
    if(!queryTask.isSaveResult()) {
      FileSystem fs = service.getFileSystem(queryTask.getConnectionName());
      if(fs == null) {
        return;
      }
      boolean success = fs.mkdirs(new Path(queryTask.getResultPath()));
      if(!success) {
        throw new Exception("Can't mkdir result path:" + (new Path(queryTask.getResultPath())));
      }
      Writer writer = new OutputStreamWriter(fs.create(new Path(queryTask.getResultPath() + "/part-00000")));
      try {
        HiveManagerService.om.writeValue(writer, queryResult);
      } finally {
        if(writer != null) {
          writer.close();
        }
      }
    }
  }
}
