package com.bhargav.titantrade.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.auth.dto.RegisterUserRequest;
import com.bhargav.titantrade.common.exception.EmailAlreadyExistsException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.user.enums.Role;
import com.bhargav.titantrade.user.enums.UserStatus;
import com.bhargav.titantrade.user.repository.UserRepository;

@Service
public class AuthService {
	
	private final UserRepository userRepo;
	
	private final BCryptPasswordEncoder passwordEncoder;
	
	public AuthService(UserRepository userRepo, BCryptPasswordEncoder passwordEncoder){
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}
	
		
	public ApiResponse registerUser(RegisterUserRequest userRequest) {
		if(!userRepo.existsByEmail(userRequest.getEmail())) {
			User tempUser = new User();
			tempUser.setFirstName(userRequest.getFirstName());
			tempUser.setLastName(userRequest.getLastName());
			tempUser.setEmail(userRequest.getEmail());
			tempUser.setGender(userRequest.getGender());
			tempUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
			tempUser.setRole(Role.CUSTOMER);
			tempUser.setStatus(UserStatus.ACTIVE);
			tempUser.setCreatedOn(LocalDateTime.now());
			tempUser.setUpdatedOn(LocalDateTime.now());
			
			userRepo.save(tempUser);
			return new ApiResponse(true, "User registered successfully", null);
		} else {
			throw new EmailAlreadyExistsException("Email already exists");
		}
	}


}
