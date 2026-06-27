package com.bhargav.titantrade.wallet.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.exception.UserNotFoundException;
import com.bhargav.titantrade.common.exception.WalletNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.user.repository.UserRepository;
import com.bhargav.titantrade.wallet.dto.WalletBalanceResponse;
import com.bhargav.titantrade.wallet.entity.Wallet;
import com.bhargav.titantrade.wallet.repository.WalletRepository;

@Service
public class WalletService {
	
	
	private final UserRepository userRepository;
	
	private final WalletRepository walletRepository;
	
	public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
		this.walletRepository =  walletRepository;
		this.userRepository = userRepository;

	}

	public ResponseEntity<ApiResponse> findWalletByUser() {
		//Get mail from security context
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User not found"));
		
		Wallet wallet = walletRepository.findByUser(user).orElseThrow(()-> new WalletNotFoundException("Wallet not found"));
		
		WalletBalanceResponse walletBalanceResponse = new WalletBalanceResponse(wallet.getBalance(), wallet.getCurrency());
		return new ResponseEntity<ApiResponse>(new ApiResponse(true, "Wallet found successfully", walletBalanceResponse), HttpStatus.OK);
	}


}
