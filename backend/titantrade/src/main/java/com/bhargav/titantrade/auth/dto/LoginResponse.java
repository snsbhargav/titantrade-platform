package com.bhargav.titantrade.auth.dto;

import java.util.UUID;

import com.bhargav.titantrade.user.enums.GenderType;
import com.bhargav.titantrade.user.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
	
	private UUID userId;
	private String firstName;
	private String lastName;
	private String email;
	private GenderType gender;
	private String token;
	private Role role;

}
