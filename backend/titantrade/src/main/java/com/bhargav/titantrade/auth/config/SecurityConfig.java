package com.bhargav.titantrade.auth.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.bhargav.titantrade.auth.filter.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter authenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter authenticationFilter) {
		this.authenticationFilter = authenticationFilter;
	}

	@Bean
	BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/v1/auth/**").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/stocks").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/v1/stocks/*/price").hasRole("ADMIN")

						.requestMatchers(HttpMethod.GET, "/api/v1/stocks/**").authenticated()
						.requestMatchers("/api/v1/admin/**").hasRole("ADMIN").anyRequest().authenticated())
				.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setAllowedMethods(List.of("POST", "GET", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedOrigins(List.of("http://localhost:5173"));
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
