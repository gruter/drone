package com.gruter.drone.common.login;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import com.gruter.drone.dao.IHiveManagerDAO;
import com.gruter.drone.model.HiveUser;

@Service("userService")
public class UserService implements UserDetailsService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  @Autowired
  private IHiveManagerDAO hiveManagerDao;
  
	@Override
	public UserDetails loadUserByUsername(String userName)
	throws UsernameNotFoundException, DataAccessException {
		boolean enabled = true;
		boolean accountNonExpired = true;
		boolean credentialsNonExpired = true;
		boolean accountNonLocked = true;
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		UserModel userModel = null;

		try{
	    HiveUser user = hiveManagerDao.getHiveUser(userName);
	    if(user == null) {
	      throw new UsernameNotFoundException("User Not Founded");
	    }
	    authorities.add(new GrantedAuthorityImpl("ROLE_USER"));

	    if (logger.isDebugEnabled()) {
				logger.debug("try to sign on :'" + userName + "'");
			}
			userModel = new UserModel(user.getUserId(), user.getPassword(), enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		}catch (Exception e) {
		  logger.error(e.getMessage(), e);
			throw new UsernameNotFoundException(userName + "," + e.getMessage());
		}

		return userModel;
	}

	public UserModel getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserModel) {
			return (UserModel) principal;
		} else {
			return null;
		}
	}

	public WebAuthenticationDetails getCurrentDetails() {
		Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
		if (details instanceof WebAuthenticationDetails) {
			return (WebAuthenticationDetails) details;
		} else {
			return null;
		}
	}
}
