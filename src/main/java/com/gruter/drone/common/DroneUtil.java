package com.gruter.drone.common;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DroneUtil {
  public static String stringifyException(Throwable e) {
    StringWriter stm = new StringWriter();
    PrintWriter wrt = new PrintWriter(stm);
    e.printStackTrace(wrt);
    wrt.close();
    return stm.toString();
  }
}
