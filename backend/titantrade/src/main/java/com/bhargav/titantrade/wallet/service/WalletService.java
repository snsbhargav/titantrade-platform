package com.bhargav.titantrade.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
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

	private static final int MONEY_SCALE = 2;
	private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

	public WalletService(CurrentUserService currentUserService, WalletRepository walletRepository,
			WalletTransactionRepository walletTransactionRepository) {
		this.walletRepository = walletRepository;
		this.walletTransactionRepository = walletTransactionRepository;
		this.currentUserService = currentUserService;
	}

	public ApiResponse findWalletByUser() {
		Wallet wallet = currentUserService.getCurrentWallet();
		BigDecimal amount = normalizeMoney(wallet.getBalance());
		WalletBalanceResponse walletBalanceResponse = new WalletBalanceResponse(amount, wallet.getCurrency());
		return new ApiResponse(true, "Wallet found successfully", walletBalanceResponse);
	}

	@Transactional
	public ApiResponse depositAmount(WalletAmountRequest walletAmountRequest) {
		Wallet wallet = currentUserService.getCurrentWallet();
		BigDecimal amount = normalizeMoney(walletAmountRequest.getAmount());
		wallet.setBalance(normalizeMoney(wallet.getBalance().add(amount)));
		Wallet savedWallet = walletRepository.save(wallet);

		// Add record to wallet_transaction table
		recordWalletTransaction(savedWallet, amount, TransactionType.DEPOSIT, TransactionStatus.SUCCESS);

		return new ApiResponse(true, "Amount deposited successfully.",
				new WalletBalanceResponse(savedWallet.getBalance(), savedWallet.getCurrency()));
	}

	@Transactional
	public ApiResponse withdrawAmount(WalletAmountRequest walletAmountRequest) {
		Wallet wallet = currentUserService.getCurrentWallet();
		BigDecimal amount = normalizeMoney(walletAmountRequest.getAmount());

		if (wallet.getBalance().compareTo(amount) >= 0) {
			wallet.setBalance(normalizeMoney(wallet.getBalance().subtract(amount)));
			Wallet savedWallet = walletRepository.save(wallet);

			// Add record to wallet_transaction table
			recordWalletTransaction(savedWallet, amount, TransactionType.WITHDRAW, TransactionStatus.SUCCESS);

			return new ApiResponse(true, "Amount withdrawn successfully",
					new WalletBalanceResponse(savedWallet.getBalance(), savedWallet.getCurrency()));
		}
		recordWalletTransaction(wallet, amount, TransactionType.WITHDRAW, TransactionStatus.FAILED);

		throw new InsufficientFundsException("Insufficient funds");
	}

	private void recordWalletTransaction(Wallet wallet, BigDecimal amount, TransactionType type,
			TransactionStatus status) {
		WalletTransaction walletTransaction = new WalletTransaction();
		walletTransaction.setAmount(normalizeMoney(amount));
		walletTransaction.setBalanceAfterTransaction(normalizeMoney(wallet.getBalance()));
		walletTransaction.setWallet(wallet);
		walletTransaction.setTransactionType(type);
		walletTransaction.setTransactionStatus(status);

		walletTransactionRepository.save(walletTransaction);

	}

	private BigDecimal normalizeMoney(BigDecimal amount) {
		return amount.setScale(MONEY_SCALE, ROUNDING_MODE);
	}

}
