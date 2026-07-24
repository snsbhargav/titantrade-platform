package com.bhargav.titantrade.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.user.enums.Role;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${app.jwt.secret}")
	private String secret;
	@Value("${app.jwt.expiration-time-ms}")
	private long expiration_time; // 1 hour

	public String generateToken(String email, UUID id, Role role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", id);
		claims.put("role", role);
		// GenerateKey
		SecretKey key = getSigningKey();
		return Jwts.builder().claims(claims).subject(email).issuedAt(new Date()).signWith(key)
				.expiration(new Date(System.currentTimeMillis() + expiration_time)).compact();

	}

	// extract username
	public String extractUsername(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
	}

	// isTokenExpired
	public boolean isTokenExpired(String token) {
		Date expiration = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload()
				.getExpiration();
		return expiration.before(new Date());
	}

	// validate token
	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);

			return !isTokenExpired(token);
		} catch (Exception ex) {
			return false;
		}
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

}
