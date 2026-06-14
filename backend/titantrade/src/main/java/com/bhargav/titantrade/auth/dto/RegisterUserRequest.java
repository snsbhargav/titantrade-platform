package com.bhargav.titantrade.auth.dto;

import com.bhargav.titantrade.user.enums.GenderType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {
	
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private GenderType gender;
	

}
