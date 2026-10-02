package com.crm.BackendApp.security;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.crm.BackendApp.entity.User;


public class CustomUserDetails implements UserDetails 
{
	private static final long serialVersionUID = 1L;
	private User user;
	
	public CustomUserDetails(User user)
	{
		this.user = user;
	}
	
	public User getUser()
	{
		return this.user;
	}
	
	public Long getId()
	{
		return user.getId();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() 
	{
		return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
	}

	@Override
	public @Nullable String getPassword() 
	{
		return user.getPassword();
	}

	@Override
	public String getUsername() 
	{
		return user.getEmail();
	}

	@Override
	public boolean isEnabled() 
	{
		return user.getUserStatus() != com.crm.BackendApp.enums.UserStatus.DEACTIVATED 
				&& user.getUserStatus() != com.crm.BackendApp.enums.UserStatus.INACTIVE;
	}
}
