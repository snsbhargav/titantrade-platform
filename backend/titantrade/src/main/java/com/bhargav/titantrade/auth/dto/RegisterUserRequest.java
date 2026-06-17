package com.bhargav.titantrade.auth.dto;

import com.bhargav.titantrade.user.enums.GenderType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {
	@NotEmpty(message = "First name is required.")
	private String firstName;
	@NotEmpty(message = "Last name is required.")
	private String lastName;
	@Email
	@NotEmpty(message = "Email is required.")
	private String email;
	@NotEmpty(message = "Password is required.")
	@Size(min = 8, message = "Password must be at least 8 characters")
	private String password;
	private GenderType gender;
	

}
