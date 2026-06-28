package com.bhargav.titantrade.common.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.exception.UserNotFoundException;
import com.bhargav.titantrade.common.exception.WalletNotFoundException;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.user.repository.UserRepository;
import com.bhargav.titantrade.wallet.entity.Wallet;
import com.bhargav.titantrade.wallet.repository.WalletRepository;

@Service
public class CurrentUserService {

	private final UserRepository userRepository;

	private final WalletRepository walletRepository;

	public CurrentUserService(UserRepository userRepository, WalletRepository walletRepository) {
		this.userRepository = userRepository;
		this.walletRepository = walletRepository;
	}


	public String getCurrentEmail() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public User getCurrentUser() {
		return userRepository.findByEmail(getCurrentEmail())
				.orElseThrow(() -> new UserNotFoundException("User not found"));
	}
	
	public Wallet getCurrentWallet() {
		return walletRepository.findByUser(getCurrentUser())
				.orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
	}

}
