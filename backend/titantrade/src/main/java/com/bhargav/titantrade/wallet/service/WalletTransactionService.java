package com.bhargav.titantrade.wallet.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.exception.UserNotFoundException;
import com.bhargav.titantrade.common.exception.WalletNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.user.repository.UserRepository;
import com.bhargav.titantrade.wallet.dto.WalletTransactionResponse;
import com.bhargav.titantrade.wallet.entity.Wallet;
import com.bhargav.titantrade.wallet.entity.WalletTransaction;
import com.bhargav.titantrade.wallet.repository.WalletRepository;
import com.bhargav.titantrade.wallet.repository.WalletTransactionRepository;

@Service
public class WalletTransactionService {

	private final UserRepository userRepository;

	private final WalletRepository walletRepository;

	private final WalletTransactionRepository walletTransactionRepository;

	public WalletTransactionService(WalletRepository walletRepository, UserRepository userRepository,
			WalletTransactionRepository walletTransactionRepository) {
		this.walletRepository = walletRepository;
		this.userRepository = userRepository;
		this.walletTransactionRepository = walletTransactionRepository;
	}

	public ResponseEntity<ApiResponse> findTransactionsByWallet() {
		Wallet wallet = getWalletFromContext();
		List<WalletTransaction> transactions = walletTransactionRepository.findByWalletOrderByCreatedOnDesc(wallet);
		List<WalletTransactionResponse> responseTransactions = new ArrayList<>();
		for (WalletTransaction transaction : transactions) {
			responseTransactions.add(WalletTransactionResponse.toDto(transaction));
		}
		return new ResponseEntity<ApiResponse>(
				new ApiResponse(true, "Wallet transactions retrieved successfully", responseTransactions), HttpStatus.OK);
	}

	private Wallet getWalletFromContext() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

		Wallet wallet = walletRepository.findByUser(user)
				.orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

		return wallet;
	}

}
