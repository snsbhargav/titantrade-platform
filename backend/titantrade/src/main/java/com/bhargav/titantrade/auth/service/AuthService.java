package com.bhargav.titantrade.auth.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.auth.dto.LoginRequest;
import com.bhargav.titantrade.auth.dto.LoginResponse;
import com.bhargav.titantrade.auth.dto.RegisterUserRequest;
import com.bhargav.titantrade.common.exception.EmailAlreadyExistsException;
import com.bhargav.titantrade.common.exception.LoginFailedException;
import com.bhargav.titantrade.common.exception.UserNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.user.enums.Role;
import com.bhargav.titantrade.user.enums.UserStatus;
import com.bhargav.titantrade.user.repository.UserRepository;
import com.bhargav.titantrade.wallet.entity.Wallet;
import com.bhargav.titantrade.wallet.enums.CurrencyType;
import com.bhargav.titantrade.wallet.repository.WalletRepository;

@Service
public class AuthService {

	private final UserRepository userRepo;

	private final BCryptPasswordEncoder passwordEncoder;
	
	private final WalletRepository walletRepository;

	public AuthService(UserRepository userRepo, BCryptPasswordEncoder passwordEncoder, WalletRepository walletRepository) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.walletRepository = walletRepository;
	}

	public ApiResponse registerUser(RegisterUserRequest userRequest) {
		if (!userRepo.existsByEmail(userRequest.getEmail())) {
			User tempUser = new User();
			tempUser.setFirstName(userRequest.getFirstName());
			tempUser.setLastName(userRequest.getLastName());
			tempUser.setEmail(userRequest.getEmail());
			tempUser.setGender(userRequest.getGender());
			tempUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
			tempUser.setRole(Role.CUSTOMER);
			tempUser.setStatus(UserStatus.ACTIVE);

			User user = userRepo.save(tempUser);
			
			//Create Wallet
			Wallet wallet = new Wallet(new BigDecimal("100000.00"), user, CurrencyType.USD);
			walletRepository.save(wallet);
			
			return new ApiResponse(true, "User registered successfully", null);
		} else {
			throw new EmailAlreadyExistsException("Email already exists");
		}
	}

	public ApiResponse validateUserLogin(LoginRequest loginRequest) {
		User user = userRepo.findByEmail(loginRequest.getEmail())
				.orElseThrow(() -> new UserNotFoundException("User not found."));
		// Checking password
		if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {

			// Get Token from JwtService
			String token = JwtService.generateToken(user.getEmail(), user.getId(), user.getRole());

			LoginResponse loginResponse = new LoginResponse(user.getId(), user.getFirstName(), user.getLastName(),
					user.getEmail(), user.getGender(), token, user.getRole());
			return new ApiResponse(true, "User login successful", loginResponse);
		} else {
			throw new LoginFailedException("Invalid email or password");
		}

	}

}
