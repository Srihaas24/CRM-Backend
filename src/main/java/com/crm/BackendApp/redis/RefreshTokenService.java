package com.crm.BackendApp.redis;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.crm.BackendApp.entity.User;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class RefreshTokenService 
{
	
	private final StringRedisTemplate redisTemplate;
	
	private static final long REFRESH_EXPIRY_DAYS = 7;
	
	//Creating refresh token 
	public String createRefreshToken(User user)
	{
		String token = UUID.randomUUID().toString();
		
		redisTemplate.opsForValue().set(
				"refresh:" + token, 
				user.getId().toString(), 
				Duration.ofDays(REFRESH_EXPIRY_DAYS));
		
		redisTemplate.opsForSet().add("user_refresh:" + user.getId().toString(), token);
		redisTemplate.expire("user_refresh:" + user.getId().toString(), Duration.ofDays(REFRESH_EXPIRY_DAYS));
		
		validateRefreshToken(token);
		return token;
	}
	
	//Validate Refresh Token
	public String validateRefreshToken(String refreshToken)
	{	
		return redisTemplate.opsForValue().get("refresh:" + refreshToken);
	}
	
	//Deleting Refresh Token
	public void deleteRefreshToken(String token)
	{
		String userId = redisTemplate.opsForValue().get("refresh:" + token);
		if (userId != null)
		{
			redisTemplate.delete("refresh:" + token);
			redisTemplate.opsForSet().remove("user_refresh:" + userId, token);
		}
	}

	//Deleting all refresh tokens for a user
	public void deleteUserRefreshTokens(User user)
	{
		String key = "user_refresh:" + user.getId().toString();
		Set<String> tokens = redisTemplate.opsForSet().members(key);
		if (tokens != null)
		{
			for (String token : tokens)
			{
				redisTemplate.delete("refresh:" + token);
			}
			redisTemplate.delete(key);
		}
	}
}
