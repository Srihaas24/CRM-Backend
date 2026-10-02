package com.crm.BackendApp.security.jwt;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService 
{
	@Value("${jwt.secret}")
	private String secret;
	
	public String generateAccessToken(String email) {
		return Jwts.builder()
				.subject(email)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
				.signWith(generateKey())
				.compact();
	}

	private Key generateKey() 
	{
		byte[] b = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(b);
	}

	public String extractUsername(String token) 
	{
		return extractClaims(token).getSubject();
	}

	public boolean validateToken(UserDetails userDetails, String token) 
	{
		String tokenUsername = extractUsername(token);
		return (tokenUsername.equals(userDetails.getUsername()) && !isTokenExpired(token)) ;
	}

	private boolean isTokenExpired(String token) 
	{
		return extractClaims(token).getExpiration().before(new Date());
	}

	private Claims extractClaims(String token) 
	{
		return Jwts.parser()
				.verifyWith((SecretKey) generateKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}	
}