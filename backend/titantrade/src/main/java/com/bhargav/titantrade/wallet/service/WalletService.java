package com.bhargav.titantrade.wallet.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bhargav.titantrade.common.constants.DecimalConstants;
import com.bhargav.titantrade.common.exception.InsufficientFundsException;
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

	public WalletBalanceResponse findWalletByUser() {
		Wallet wallet = currentUserService.getCurrentWallet();
		BigDecimal amount = normalizeMoney(wallet.getBalance());
		return new WalletBalanceResponse(amount, wallet.getCurrency());

	}
	@Transactional
	public WalletBalanceResponse depositAmount(WalletAmountRequest walletAmountRequest) {
		Wallet wallet = creditCurrentUserWallet(walletAmountRequest.getAmount());
		return new WalletBalanceResponse(wallet.getBalance(), wallet.getCurrency());
	}
	@Transactional
	public WalletBalanceResponse withdrawAmount(WalletAmountRequest walletAmountRequest) {
		Wallet wallet = debitCurrentUserWallet(walletAmountRequest.getAmount());
		return new WalletBalanceResponse(wallet.getBalance(), wallet.getCurrency());
	}
	
	public Wallet creditCurrentUserWallet(BigDecimal amount) {
		Wallet wallet = currentUserService.getCurrentWallet();
		amount = normalizeMoney(amount);
		wallet.setBalance(normalizeMoney(wallet.getBalance().add(amount)));
		Wallet savedWallet = walletRepository.save(wallet);

		// Add record to wallet_transaction table
		recordWalletTransaction(savedWallet, amount, TransactionType.DEPOSIT, TransactionStatus.SUCCESS);
		return savedWallet;
	}
	
	public Wallet debitCurrentUserWallet(BigDecimal amount) {
		Wallet wallet = currentUserService.getCurrentWallet();
		amount = normalizeMoney(amount);

		if (wallet.getBalance().compareTo(amount) >= 0) {
			wallet.setBalance(normalizeMoney(wallet.getBalance().subtract(amount)));
			Wallet savedWallet = walletRepository.save(wallet);

			// Add record to wallet_transaction table
			recordWalletTransaction(savedWallet, amount, TransactionType.WITHDRAW, TransactionStatus.SUCCESS);
			return savedWallet;
		}
		//Record failed transaction in order_transaction in future
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
		return amount.setScale(DecimalConstants.MONEY_SCALE, DecimalConstants.ROUNDING_MODE);
	}
	


}
