package com.gruter.drone.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.gruter.drone.common.BaseController;
import com.gruter.drone.model.HiveConnection;
import com.gruter.drone.model.HiveQuery;
import com.gruter.drone.model.HiveQueryCategory;
import com.gruter.drone.model.HiveTreeNode;
import com.gruter.drone.model.QueryResult;
import com.gruter.drone.model.QueryTask;
import com.gruter.drone.service.HiveManagerService;
import com.gruter.drone.service.IHiveManagerService;

@Controller("hiveManagerController")
public class HiveManagerController extends BaseController {
  @Resource(type=HiveManagerService.class)
  private IHiveManagerService hiveManagerService;

  @RequestMapping("hive/managerView.do")
  protected ModelAndView getHiveManagerView(){
    ModelAndView mav = new ModelAndView();
    mav.setViewName("hive/hiveManager");
    return mav;
  }
  
  @RequestMapping(value = "hive/listCategory.do")
  protected @ResponseBody Object listCategory() throws Exception {
    try {
      return hiveManagerService.listCategory();
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  } 

//  @RequestMapping(value="/hive/login.do")
//  protected @ResponseBody Object login(@RequestParam(value="userId") String userId,
//                     @RequestParam(value="password") String password) throws Exception {
//    Map<String, Object> result = new HashMap<String, Object>();
//    
//    if(userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
//      result.put("success", false);
//      result.put("msg", "User Id or Password is empty");
//      return result;
//    }
//    
//    HiveUser hiveUser = hiveManagerService.getHiveUser(userId);
//    if(hiveUser == null) {
//      result.put("success", false);
//      result.put("msg", "User Id or Password not matched");
//      return result;
//    }
//    String encodedPassword = hiveManagerService.getPassword(password);
//    if(!encodedPassword.equals(hiveUser.getPassword())) {
//      result.put("success", false);
//      result.put("msg", "User Id or Password not matched");
//      return result;
//    }
//    result.put("success", true);
//    return result;
//  }
  
  @RequestMapping(value = "hive/saveQuery.do")
  protected @ResponseBody Object saveQuery(
      @RequestParam(value = "categoryName", required = true) String categoryName,
      @RequestParam(value = "queryName", required = true) String queryName,
      @RequestParam(value = "query", required = true) String query
      ) throws Exception {
    try {
      HiveQuery hiveQuery = new HiveQuery();
      hiveQuery.setQueryName(queryName);
      hiveQuery.setQuery(query);
      hiveQuery.setCategoryName(categoryName);
      hiveQuery.setCreateDate(new Timestamp(System.currentTimeMillis()));
      
      hiveManagerService.saveHiveQuery(hiveQuery);
      return this.createSuccessResponse("Added", "");
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }

  @RequestMapping(value = "hive/downloadResult.do")
  public ModelAndView download(HttpServletRequest request,
              HttpServletResponse response) throws Exception {
    String taskId = ServletRequestUtils.getRequiredStringParameter(request, "taskId");
    String pathName = ServletRequestUtils.getRequiredStringParameter(request, "path");

    BufferedInputStream bis = null;
    BufferedOutputStream bos = null;
    try {
      QueryTask queryTask = hiveManagerService.getQueryTask(taskId);
      if(queryTask == null) {
        throw new IOException("No query task:" + taskId);
      }
      FileSystem fs = hiveManagerService.getFileSystem(queryTask.getConnectionName());
      
      if(fs == null) {
        throw new IOException("Can't get file system");
      }
      
      Path hPath = new Path(queryTask.getResultPath() + "/" + pathName);
      if(!fs.exists(hPath)) {
        logger.warn("No file:" + taskId + "," + pathName + "," + hPath);
        response.getWriter().println("No file:" + taskId + "," + pathName);
        return null;
      }
      logger.info("File downloaded:" + taskId + "," + pathName);
      response.setHeader("Cache-Control", "no-cache");
      response.setHeader("Content-Type", "application/octet-stream");
      response.setHeader("Content-Disposition", "attachment; filename=" + hPath.getName());

      bis = new BufferedInputStream(fs.open(hPath));

      bos = new BufferedOutputStream(response.getOutputStream());

      int length = -1;

      byte[] buffer = new byte[4096];
      while ((length = bis.read(buffer)) > 0) {
        bos.write(buffer, 0, length);
        bos.flush();
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    } finally {
      if (bis != null) {
        try {
          bis.close();
        } catch (IOException e) {
        }
      }
      if (bos != null) {
        try {
          bos.close();
        } catch (IOException e) {
          logger.error(e.getMessage(), e);
        }
      }
    }
    return null;
  }

  
  /*
  @RequestMapping(value = "hive/removeQuery.do")
  protected @ResponseBody Object removeQuery(
      @RequestParam(value = "queryId", required = true) String queryId
      ) throws Exception {
    try {
     
      hiveManagerService.removeHiveQuery(queryId);
      return this.createSuccessResponse("Removed", "");
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }
  */
  
  @RequestMapping(value = "hive/addQueryCategory.do")
  protected @ResponseBody Object addQueryCategory(
      @RequestParam(value = "categoryName", required = true) String categoryName
      ) throws Exception {
    try {
      HiveQueryCategory hiveQueryCategory = new HiveQueryCategory();
      hiveQueryCategory.setCategoryName(categoryName);
      
      hiveManagerService.addQueryCategory(hiveQueryCategory);
      return this.createSuccessResponse("Added", "");
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }

  @RequestMapping(value = "hive/removeQuery.do")
  protected @ResponseBody Object removeQuery(
      @RequestParam(value = "categoryName", required = true) String categoryName,
      @RequestParam(value = "queryId", required = false) Integer queryId
      ) throws Exception {
    try {
     if(queryId == null) {
       queryId = -1;
     }
      hiveManagerService.removeQuery(categoryName, queryId);
      return this.createSuccessResponse("Removed", "");
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }
  
  @RequestMapping(value = "hive/getQueryListForCategory.do")
  protected @ResponseBody Object getQueryListForCategory() throws Exception {
    try {
      return hiveManagerService.getQueryListForCategory();
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }  
  
  @RequestMapping(value = "hive/tableDetail.do")
  protected @ResponseBody Object tableDetail(
      @RequestParam(value = "connectionName", required = true) String connectionName,
      @RequestParam(value = "tableName", required = true) String tableName) throws Exception {
    try {
      return hiveManagerService.getTable(connectionName, tableName);
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  } 
    
  @RequestMapping(value = "hive/tree.do")
  protected @ResponseBody Object getClusterTreeData(
      @RequestParam(value = "value", required = false) String nodeValue, 
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "connectionName", required = false) String connectionName) throws Exception {

    List<HiveTreeNode> response = new ArrayList<HiveTreeNode>();
    try {
      if (category == null || category.trim().isEmpty()) {
        HiveTreeNode treeNode = new HiveTreeNode();
        treeNode.setText("Connections");
        treeNode.setValue("conns-root");
        treeNode.setCategory("root");
        treeNode.setLeaf(false);
        response.add(treeNode);
        return response;
      } else {
        return hiveManagerService.getConnectionTree(nodeValue, category, connectionName);
      }
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }
  
  @RequestMapping(value = "hive/removeConn.do", method = RequestMethod.POST)
  protected @ResponseBody
  Object removeConnection(@RequestParam(value = "connectionName") String connectionName) throws Exception {
    try {
      hiveManagerService.removeHiveConnection(connectionName);
      return this.createSuccessResponse("successfully deleted", null);
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }

  @RequestMapping(value = "hive/createConn.do", method = RequestMethod.POST)
  protected @ResponseBody
  Object createConnection(@RequestParam(value = "params") String param) throws Exception {
    try {
      ObjectMapper mapper = new ObjectMapper();
      HashMap<String, String> map = mapper.readValue(param, HashMap.class);
      HiveConnection conn = new HiveConnection();
      conn.setConnectionName(map.get("connectionName"));
      conn.setDriverClass(map.get("driverClass"));
      conn.setFileSystemUri(map.get("fileSystemUri"));
      conn.setConnectionUri(map.get("connectionUri"));
      conn.setDescription(map.get("description"));
      hiveManagerService.addConnection(conn);

      return this.createSuccessResponse("successfully created", null);
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }  
  
  @RequestMapping(value = "hive/getConn.do", method = RequestMethod.POST)
  protected @ResponseBody
  Object getConnection(@RequestParam(value = "connectionName") String connectionName) throws Exception {
    try {
      return hiveManagerService.getConnectionInfo(connectionName);
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }  
  
  @RequestMapping(value = "hive/saveConn.do", method = RequestMethod.POST)
  protected @ResponseBody
  Object saveConnection(@RequestParam(value = "params") String param) throws Exception {
    try {
      ObjectMapper mapper = new ObjectMapper();
      HashMap<String, String> map = mapper.readValue(param, HashMap.class);
      HiveConnection conn = new HiveConnection();
      conn.setConnectionName(map.get("connectionName"));
      conn.setDriverClass(map.get("driverClass"));
      conn.setFileSystemUri(map.get("fileSystemUri"));
      conn.setConnectionUri(map.get("connectionUri"));
      conn.setDescription(map.get("description"));
      hiveManagerService.saveConnection(conn);

      return this.createSuccessResponse("successfully saved", null);
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  }  
  
  @RequestMapping(value = "hive/executeQuery.do")
  protected @ResponseBody Object executeQuery(
      @RequestParam(value = "connectionName", required = true) String connectionName,
      @RequestParam(value = "query", required = true) String query,
      @RequestParam(value = "saveFile", required = false) Boolean saveFile) throws Exception {
    try {
      if(saveFile == null) {
        saveFile = new Boolean(false);
      }
      
      return this.createSuccessResponse(hiveManagerService.executeQuery(userService.getCurrentUser().getUsername(), 
          connectionName, query, saveFile));
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  } 
  
  @RequestMapping(value = "hive/explain.do")
  protected @ResponseBody Object explain(
      @RequestParam(value = "connectionName", required = true) String connectionName,
      @RequestParam(value = "query", required = true) String query) throws Exception {
    try {
      return this.createSuccessResponse(hiveManagerService.explain(connectionName, query));
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  } 
  
  @RequestMapping(value = "hive/getQueryResult.do")
  protected @ResponseBody Object getQueryResult(
      @RequestParam(value = "taskId", required = true) String taskId) throws Exception {
    try {
      QueryResult queryResult = hiveManagerService.getQueryResult(taskId);

      HashMap<String, Object> metaData = new HashMap<String, Object>();
      List<HashMap<String, Object>> metaFields = new ArrayList<HashMap<String, Object>>();

      if(queryResult.isError()) {
        metaData.put("queryError", "true");
        return this.createSuccessResponse(queryResult.getErrorMessage(), "error", metaData);
      } else if(queryResult.getTotalRows() >= 0) {
        int i = 0;
        for (String columnName: queryResult.getColumnNames()) {
          HashMap<String, Object> fieldData = new HashMap<String, Object>();
          fieldData.put("dataIndex", columnName);
          fieldData.put("mapping", i);
          fieldData.put("name", columnName);
          fieldData.put("header", columnName);
          metaFields.add(fieldData);
          i++;
        }
        metaData.put("fields", metaFields);
        return this.createSuccessResponse("success", queryResult.getResultDatas(), metaData);
      } else {
        return this.createRetryResponse("running", null, metaData);
      }
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  } 
  
  @RequestMapping(value = "hive/getQueryResultFiles.do")
  protected @ResponseBody Object getQueryResultFiles (
      @RequestParam(value = "taskId", required = true) String taskId) throws Exception {
    try {
      return hiveManagerService.getResultFileList(taskId);
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  } 
  
  @RequestMapping(value = "hive/getQueryTasks.do")
  protected @ResponseBody Object getQueryTasks() throws Exception {
    try {
      return hiveManagerService.getQueryTasksByUser(userService.getCurrentUser().getUsername());
    } catch (Exception e) {
      return this.createFailureResponse(e.getMessage(), e);
    }
  } 
}
