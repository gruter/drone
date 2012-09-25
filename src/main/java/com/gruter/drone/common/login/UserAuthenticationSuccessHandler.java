package com.gruter.drone.common.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class UserAuthenticationSuccessHandler extends
SavedRequestAwareAuthenticationSuccessHandler {
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, 
			HttpServletResponse response, 
			Authentication authentication) throws IOException, ServletException{

		WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
		UserModel model = (UserModel) authentication.getPrincipal();
		model.setUserAttribute(UserModel.SESSIONID, details.getSessionId());
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
