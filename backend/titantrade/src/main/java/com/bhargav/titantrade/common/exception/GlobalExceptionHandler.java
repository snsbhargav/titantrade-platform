package com.bhargav.titantrade.common.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.bhargav.titantrade.common.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse> validationExceptionHandler(MethodArgumentNotValidException ex) {
		ArrayList<String> errors = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();
		for (ObjectError i : ex.getBindingResult().getAllErrors()) {
			errors.add(i.getDefaultMessage());
		}
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Validation failed", errors), HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ApiResponse> globalException(EmailAlreadyExistsException ex) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", false);
		response.put("message", ex.getMessage());
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
	}
}
