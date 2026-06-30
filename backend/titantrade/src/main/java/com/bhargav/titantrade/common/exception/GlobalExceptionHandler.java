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
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, "Validation failed", errors),
				HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ApiResponse> globalException(EmailAlreadyExistsException ex) {
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResponse> userNotFoundException(UserNotFoundException ex) {
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(LoginFailedException.class)
	public ResponseEntity<ApiResponse> loginFailedException(LoginFailedException ex) {
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(WalletNotFoundException.class)
	public ResponseEntity<ApiResponse> walletNotFoundException(WalletNotFoundException ex){
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(InsufficientFundsException.class)
	public ResponseEntity<ApiResponse> insufficientFundsException(InsufficientFundsException ex){
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(StockNotFoundException.class)
	public ResponseEntity<ApiResponse> stockNotFoundException(StockNotFoundException ex){
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(PortfolioHoldingNotFoundException.class)
	public ResponseEntity<ApiResponse> portfolioHoldingNotFoundException(PortfolioHoldingNotFoundException ex){
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(InsufficientHoldingQuantityException.class)
	public ResponseEntity<ApiResponse> insufficientHoldingQuantityException(InsufficientHoldingQuantityException ex){
		return new ResponseEntity<ApiResponse>(new ApiResponse(false, ex.getMessage(), null), HttpStatus.BAD_REQUEST);
	}
}
