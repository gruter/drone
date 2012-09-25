package com.gruter.drone.common.login;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hive.conf.HiveConf;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class UserModel extends User implements Comparable<UserModel> {
	private static final long serialVersionUID = -1214299241823525289L;
	private final Map<String, Object> userAttributes = Collections.synchronizedMap(new HashMap<String, Object>());
	public static final String SESSIONID = "sessionId";
	private HiveConf hiveConf = null;
	public UserModel(String username, String password, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, authorities);
	}

	public Map<String, Object> getUserAttributes() {
		return userAttributes;
	}

	public Object getUserAttribute(String key) {
		return userAttributes.get(key);
	}

	public Object setUserAttribute(String key, Object value) {
		return userAttributes.put(key, value);
	}

	public WebAuthenticationDetails getDetails() {
		return (WebAuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
	}

	public String getSessionId() {
		return (String) getUserAttribute(SESSIONID);
	}

	@Override
	public int compareTo(UserModel obj) {
		if (obj == null) {
			return -1;
		}
		if (!(obj instanceof UserModel)) {
			return -1;
		}
		UserModel o = obj;
		return o.getUsername().compareTo(getUsername()) & o.getSessionId().compareTo(getSessionId());
	}

	@Override
	public boolean equals(Object rhs) {
		if (rhs instanceof UserModel) {
			UserModel o = (UserModel) rhs;
			if (o.getUsername().equals(getUsername()) && o.getSessionId().equals(getSessionId())) {
				return true;
			}
		}
		return false;
	}
}
