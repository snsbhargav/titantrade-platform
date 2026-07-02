package com.bhargav.titantrade.wallet.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bhargav.titantrade.common.exception.InsufficientFundsException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.common.security.CurrentUserService;
import com.bhargav.titantrade.wallet.dto.WalletAmountRequest;
import com.bhargav.titantrade.wallet.dto.WalletBalanceResponse;
import com.bhargav.titantrade.wallet.entity.Wallet;
import com.bhargav.titantrade.wallet.entity.WalletTransaction;
import com.bhargav.titantrade.wallet.enums.TransactionStatus;
import com.bhargav.titantrade.wallet.enums.TransactionType;
import com.bhargav.titantrade.wallet.repository.WalletRepository;
import com.bhargav.titantrade.wallet.repository.WalletTransactionRepository;

@Service
public class WalletService {

	private final CurrentUserService currentUserService;

	private final WalletRepository walletRepository;

	private final WalletTransactionRepository walletTransactionRepository;

	public WalletService(CurrentUserService currentUserService, WalletRepository walletRepository,
			WalletTransactionRepository walletTransactionRepository) {
		this.walletRepository = walletRepository;
		this.walletTransactionRepository = walletTransactionRepository;
		this.currentUserService = currentUserService;
	}

	public ApiResponse findWalletByUser() {
		Wallet wallet = currentUserService.getCurrentWallet();

		WalletBalanceResponse walletBalanceResponse = new WalletBalanceResponse(wallet.getBalance(),
				wallet.getCurrency());
		return new ApiResponse(true, "Wallet found successfully", walletBalanceResponse);
	}

	@Transactional
	public ApiResponse depositAmount(WalletAmountRequest walletAmountRequest) {
		Wallet wallet = currentUserService.getCurrentWallet();
		wallet.setBalance(wallet.getBalance().add(walletAmountRequest.getAmount()));
		Wallet savedWallet = walletRepository.save(wallet);

		// Add record to wallet_transaction table
		recordWalletTransaction(savedWallet, walletAmountRequest.getAmount(), TransactionType.DEPOSIT,
				TransactionStatus.SUCCESS);

		return new ApiResponse(true, "Amount deposited successfully.",
				new WalletBalanceResponse(savedWallet.getBalance(), savedWallet.getCurrency()));
	}

	@Transactional
	public ApiResponse withdrawAmount(WalletAmountRequest walletAmountRequest) {
		Wallet wallet = currentUserService.getCurrentWallet();
		if (wallet.getBalance().compareTo(walletAmountRequest.getAmount()) >= 0) {
			wallet.setBalance(wallet.getBalance().subtract(walletAmountRequest.getAmount()));
			Wallet savedWallet = walletRepository.save(wallet);

			// Add record to wallet_transaction table
			recordWalletTransaction(savedWallet, walletAmountRequest.getAmount(), TransactionType.WITHDRAW,
					TransactionStatus.SUCCESS);

			return new ApiResponse(true, "Amount withdrawn successfully",
					new WalletBalanceResponse(savedWallet.getBalance(), savedWallet.getCurrency()));
		}
		recordWalletTransaction(wallet, walletAmountRequest.getAmount(), TransactionType.WITHDRAW,
				TransactionStatus.FAILED);

		throw new InsufficientFundsException("Insufficient funds");
	}

	private void recordWalletTransaction(Wallet wallet, BigDecimal amount, TransactionType type,
			TransactionStatus status) {
		WalletTransaction walletTransaction = new WalletTransaction();
		walletTransaction.setAmount(amount);
		walletTransaction.setBalanceAfterTransaction(wallet.getBalance());
		walletTransaction.setWallet(wallet);
		walletTransaction.setTransactionType(type);
		walletTransaction.setTransactionStatus(status);

		walletTransactionRepository.save(walletTransaction);

	}

}
