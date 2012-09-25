package com.gruter.drone.common.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.UrlUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DroneRedirectStrategy implements RedirectStrategy {

  private static final Logger logger = LoggerFactory.getLogger(DroneRedirectStrategy.class);

  private boolean contextRelative;

  /**
   * Redirects the response to the supplied URL.
   * <p>
   * If <tt>contextRelative</tt> is set, the redirect value will be the value
   * after the request context path. Note that this will result in the loss of
   * protocol information (HTTP or HTTPS), so will cause problems if a redirect
   * is being performed to change to HTTPS, for example.
   */
  public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
    String redirectUrl = calculateRedirectUrl(request.getContextPath(), url);
    redirectUrl = response.encodeRedirectURL(redirectUrl);

    if (logger.isDebugEnabled()) {
      logger.debug("Redirecting to '" + redirectUrl + "'");
    }
    boolean ajaxRedirect = request.getHeader("x-requested-with") != null && request.getHeader("x-requested-with").toLowerCase().indexOf("xmlhttprequest") > -1;

    if (ajaxRedirect) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{success: false, msg: '', status: 302, url:'" + redirectUrl + "'}");
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
}
