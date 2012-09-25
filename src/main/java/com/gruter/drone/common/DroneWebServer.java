package com.gruter.drone.common;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class DroneWebServer {
  CommonHttpServer webServer;
  
  public DroneWebServer(int port) throws Exception {
  	Properties conf = new Properties();
  	try {
  		conf.load(this.getClass().getResourceAsStream("/settings.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
  	File userDir = new File(System.getProperty("user.dir"));

    String contextPath = (String)conf.get("drone.jetty.webcontext.path");
    if(contextPath == null) {
    	contextPath = "/src/main/webapp";
    }
    
    webServer = new CommonHttpServer("/", userDir.getCanonicalPath() + contextPath, 
        userDir.getCanonicalPath() + contextPath + "/WEB-INF/webdefault.xml",
        "0.0.0.0", port, false);
    webServer.start();
  }
  
  public static void main(String[] args) throws Exception {
  	int port = 8091;
  	if(args.length > 0) {
//      System.out.println("Usage java IdbcManagerWebServer <port>");
//      return;
  		port = Integer.parseInt(args[0]);
    }
    new DroneWebServer(port);
  }
}
