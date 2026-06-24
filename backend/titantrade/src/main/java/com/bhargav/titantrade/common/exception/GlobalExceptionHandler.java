package com.bhargav.titantrade.common.exception;

import java.util.ArrayList;
import org.springframework.http.HttpStatus;
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
		for (ObjectError i : ex.getBindingResult().getAllErrors()) {
			errors.add(i.getDefaultMessage());
		}
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Validation failed", errors), HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ApiResponse> globalException(EmailAlreadyExistsException ex) {
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
	}
}
