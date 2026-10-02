package com.crm.BackendApp.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenDenylistService {

    private final StringRedisTemplate redisTemplate;

    private static final String DENYLIST_TOKEN_PREFIX = "denylist:token:";
    private static final String DENYLIST_USER_PREFIX = "denylist:user:";
    private static final Duration ACCESS_TOKEN_EXPIRY = Duration.ofMinutes(30);

    public void denylistUserTokens(Long userId) {
        if (userId == null) {
            return;
        }

        // Place a user-level denylist flag for immediate cut-off across all active sessions
        redisTemplate.opsForValue().set(DENYLIST_USER_PREFIX + userId, "denylisted", ACCESS_TOKEN_EXPIRY);
    }

    public void denylistToken(String token) {
        if (token != null) {
            redisTemplate.opsForValue().set(DENYLIST_TOKEN_PREFIX + token, "denylisted", ACCESS_TOKEN_EXPIRY);
        }
    }

    public boolean isTokenDenylisted(String token, Long userId) {
        if (token != null && Boolean.TRUE.equals(redisTemplate.hasKey(DENYLIST_TOKEN_PREFIX + token))) {
            return true;
        }
        if (userId != null && Boolean.TRUE.equals(redisTemplate.hasKey(DENYLIST_USER_PREFIX + userId))) {
            return true;
        }
        return false;
    }
}
