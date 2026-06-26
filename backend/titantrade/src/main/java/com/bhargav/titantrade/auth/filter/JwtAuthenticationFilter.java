package com.bhargav.titantrade.auth.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bhargav.titantrade.auth.service.JwtService;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.user.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepo;

	public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepo) {
		this.jwtService = jwtService;
		this.userRepo = userRepo;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = request.getHeader("Authorization");
		if (token == null || !token.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		token = token.substring(7);
		if (jwtService.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
			User user = userRepo.findByEmail(jwtService.extractUsername(token))
					.orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
			List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()));
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getEmail(),
					null, authorities);

			SecurityContextHolder.getContext().setAuthentication(authToken);
		}
		filterChain.doFilter(request, response);
	}

}
