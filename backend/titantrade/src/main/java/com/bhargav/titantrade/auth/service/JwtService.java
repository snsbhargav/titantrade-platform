package com.bhargav.titantrade.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.bhargav.titantrade.user.enums.Role;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private static final String SECRET = "titan-trade-super-secret-key-2026";
	private static final long EXPIRATION_TIME = 60 * 60 * 1000; // 1 hour

	public static String generateToken(String email, UUID id, Role role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", id);
		claims.put("role", role);
		// GenerateKey
		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder().claims(claims).subject(email).issuedAt(new Date()).signWith(key)
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)).compact();

	}

	// extract username
	public String extractUsername(String token) {
		return Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
			.build()
			.parseSignedClaims(token)
			.getPayload().getSubject();
	}

	// isTokenExpired
	public boolean isTokenExpired(String token) {
		Date expiration = Jwts.parser()
		.verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
		.build()
		.parseSignedClaims(token)
		.getPayload()
		.getExpiration();
		return expiration.before(new Date());
	}
	// validate token
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
			.build()
			.parseSignedClaims(token);
			
			return !isTokenExpired(token);
		} catch(Exception ex) {
			return false;
		}
	}

}
