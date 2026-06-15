package com.bhargav.titantrade.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.auth.dto.RegisterUserRequest;
import com.bhargav.titantrade.auth.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	
	@Autowired
	AuthService authService;
	
	@PostMapping("/register")
	public String registerUser(@RequestBody RegisterUserRequest userRequest) {
		return authService.registerUser(userRequest);
	}
	

}
