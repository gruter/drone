package com.gruter.drone.common.login;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.gruter.drone.model.HiveUser;
import com.gruter.drone.service.HiveManagerService;
import com.gruter.drone.service.IHiveManagerService;

public class UserLoginFilter extends UsernamePasswordAuthenticationFilter {
  @Resource(type=HiveManagerService.class)
  private IHiveManagerService hiveManagerService;
  
	protected static boolean isNull(String tempString){
		if(null == tempString || tempString.length() == 0){
			return true;
		}
		
		return false;
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {
		super.setUsernameParameter("userId");
		super.setPasswordParameter("password");
		
		String userId = request.getParameter("userId");
		String password = request.getParameter("password");
		
    if(userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
      request.setAttribute("errorCode", 100); 
      //request.setAttribute("success", false);
      //request.setAttribute("msg", "User Id or Password is empty");
      return super.attemptAuthentication(request, response);
    }
    
    try {
      HiveUser hiveUser = hiveManagerService.getHiveUser(userId);
      if(hiveUser == null) {
        request.setAttribute("errorCode", 101); 
        //request.setAttribute("success", false);
        //request.setAttribute("msg", "User Id or Password not matched");
        return super.attemptAuthentication(request, response);
      }
      String encodedPassword = hiveManagerService.getPassword(password);
      if(!encodedPassword.equals(hiveUser.getPassword())) {
        request.setAttribute("errorCode", 101); 
        //request.setAttribute("success", false);
        //request.setAttribute("msg", "User Id or Password not matched");
        return super.attemptAuthentication(request, response);
      }
      //request.setAttribute("success", true);
      Authentication authentication = super.attemptAuthentication(request, response);
      
      return authentication;
    } catch (Exception e) {
      request.setAttribute("success", false);
      request.setAttribute("msg", e.getMessage());
      return super.attemptAuthentication(request, response);
    }
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, Authentication authResult)
			throws IOException, ServletException {
		super.successfulAuthentication(request, response, authResult);
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException failed)
			throws IOException, ServletException {
		System.out.println("failure:" + request.getAttribute("msg"));
		
		super.unsuccessfulAuthentication(request, response, failed);
	}
}
