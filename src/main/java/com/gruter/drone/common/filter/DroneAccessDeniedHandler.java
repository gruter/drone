package com.gruter.drone.common.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.UrlUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DroneAccessDeniedHandler implements AccessDeniedHandler {

  private static final Logger logger = LoggerFactory.getLogger(DroneAccessDeniedHandler.class);

  private String loginPath;
  private boolean contextRelative;

  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException,
      ServletException {
    String redirectUrl = calculateRedirectUrl(request.getContextPath(), loginPath);
    redirectUrl = response.encodeRedirectURL(redirectUrl);
    logger.warn("Redirecting to '" + redirectUrl + "'");
    boolean ajaxRedirect = request.getHeader("x-requested-with") != null && request.getHeader("x-requested-with").toLowerCase().indexOf("xmlhttprequest") > -1;

    if (ajaxRedirect) {
      response.setContentType("application/json;charset=UTF-8");
      response.setStatus(200);
      response.getWriter().write("{success: false, msg: 'Access Denied', status: 302, url:''}");
    } else {
      response.sendRedirect(redirectUrl);
    }
  }

  private String calculateRedirectUrl(String contextPath, String url) {
    if (!UrlUtils.isAbsoluteUrl(url)) {
      if (contextRelative) {
        return url;
      } else {
        return contextPath + url;
      }
    }

    // Full URL, including http(s)://

    if (!contextRelative) {
      return url;
    }

    // Calculate the relative URL from the fully qualified URL, minus the scheme
    // and base context.
    url = url.substring(url.indexOf("://") + 3); // strip off scheme
    url = url.substring(url.indexOf(contextPath) + contextPath.length());

    if (url.length() > 1 && url.charAt(0) == '/') {
      url = url.substring(1);
    }

    return url;
  }

  /**
   * If <tt>true</tt>, causes any redirection URLs to be calculated minus the
   * protocol and context path (defaults to <tt>false</tt>).
   */
  public void setContextRelative(boolean useRelativeContext) {
    this.contextRelative = useRelativeContext;
  }

  public String getLoginPath() {
    return loginPath;
  }

  public void setLoginPath(String loginPath) {
    this.loginPath = loginPath;
  }
}
