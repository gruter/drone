package com.gruter.drone.common.login;

import javax.annotation.Resource;

import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.gruter.drone.service.IHiveManagerService;

public class DBPasswordEncoder implements PasswordEncoder {

	@Resource(name = "hiveManagerService")
	public IHiveManagerService hiveManagerService;

	@Override
	public String encodePassword(String rawPass, Object salt) {
		try {
			return hiveManagerService.getPassword(rawPass);
		} catch (Exception e) {
			return rawPass;
		}
	}

	@Override
	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		return encodePassword(rawPass, salt).equals(encPass);
	}
}
