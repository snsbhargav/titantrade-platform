package com.bhargav.titantrade.auth.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.user.enums.UserStatus;

public class CustomUserDetails implements UserDetails{
	
	private final User user;
	
	public CustomUserDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		GrantedAuthority authorities = new SimpleGrantedAuthority("ROLE_"+user.getRole().name());
		return List.of(authorities);
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}
	
	@Override
	public boolean isEnabled() {
		return user.getStatus()== UserStatus.ACTIVE;
	}

}
