package com.bhargav.titantrade.wallet.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.common.security.CurrentUserService;
import com.bhargav.titantrade.wallet.dto.WalletTransactionResponse;
import com.bhargav.titantrade.wallet.entity.Wallet;
import com.bhargav.titantrade.wallet.entity.WalletTransaction;
import com.bhargav.titantrade.wallet.repository.WalletTransactionRepository;

@Service
public class WalletTransactionService {

	private final CurrentUserService currentUserService;
	private final WalletTransactionRepository walletTransactionRepository;

	public WalletTransactionService(CurrentUserService currentUserService,
			WalletTransactionRepository walletTransactionRepository) {
		this.walletTransactionRepository = walletTransactionRepository;
		this.currentUserService = currentUserService;
	}

	public ApiResponse findTransactionsByWallet() {
		Wallet wallet = currentUserService.getCurrentWallet();
		List<WalletTransaction> transactions = walletTransactionRepository.findByWalletOrderByCreatedOnDesc(wallet);
		List<WalletTransactionResponse> responseTransactions = new ArrayList<>();
		for (WalletTransaction transaction : transactions) {
			responseTransactions.add(WalletTransactionResponse.toDto(transaction));
		}
		return new ApiResponse(true, "Wallet transactions retrieved successfully", responseTransactions);
	}

}
