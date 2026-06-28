package com.bhargav.titantrade.wallet.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bhargav.titantrade.common.exception.InsufficientFundsException;
import com.bhargav.titantrade.common.exception.UserNotFoundException;
import com.bhargav.titantrade.common.exception.WalletNotFoundException;
import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.user.entity.User;
import com.bhargav.titantrade.user.repository.UserRepository;
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

	private final UserRepository userRepository;

	private final WalletRepository walletRepository;

	private final WalletTransactionRepository walletTransactionRepository;

	public WalletService(WalletRepository walletRepository, UserRepository userRepository,
			WalletTransactionRepository walletTransactionRepository) {
		this.walletRepository = walletRepository;
		this.userRepository = userRepository;
		this.walletTransactionRepository = walletTransactionRepository;

	}

	public ResponseEntity<ApiResponse> findWalletByUser() {
		// Get mail from security context
		Wallet wallet = getWalletFromContext();

		WalletBalanceResponse walletBalanceResponse = new WalletBalanceResponse(wallet.getBalance(),
				wallet.getCurrency());
		return new ResponseEntity<ApiResponse>(
				new ApiResponse(true, "Wallet found successfully", walletBalanceResponse), HttpStatus.OK);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ResponseEntity<ApiResponse> depositAmount(WalletAmountRequest walletAmountRequest) {
		Wallet wallet = getWalletFromContext();
		wallet.setBalance(wallet.getBalance().add(walletAmountRequest.getAmount()));
		wallet.setUpdatedOn(LocalDateTime.now());
		Wallet savedWallet = walletRepository.save(wallet);

		// Add record to wallet_transaction table
		recordWalletTransaction(savedWallet, walletAmountRequest.getAmount(), TransactionType.CREDIT,
				TransactionStatus.SUCCESS);

		return new ResponseEntity<ApiResponse>(new ApiResponse(true, "Amount deposited successfully.",
				new WalletBalanceResponse(savedWallet.getBalance(), savedWallet.getCurrency())), HttpStatus.OK);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ResponseEntity<ApiResponse> withdrawAmount(WalletAmountRequest walletAmountRequest) {
		Wallet wallet = getWalletFromContext();
		if (wallet.getBalance().compareTo(walletAmountRequest.getAmount()) >= 0) {
			wallet.setBalance(wallet.getBalance().subtract(walletAmountRequest.getAmount()));
			wallet.setUpdatedOn(LocalDateTime.now());
			Wallet savedWallet = walletRepository.save(wallet);

			// Add record to wallet_transaction table
			recordWalletTransaction(savedWallet, walletAmountRequest.getAmount(), TransactionType.DEBIT,
					TransactionStatus.SUCCESS);

			return new ResponseEntity<ApiResponse>(
					new ApiResponse(true, "Amount withdrawn successfully",
							new WalletBalanceResponse(savedWallet.getBalance(), savedWallet.getCurrency())),
					HttpStatus.OK);
		}
		recordWalletTransaction(wallet, walletAmountRequest.getAmount(), TransactionType.DEBIT,
				TransactionStatus.FAILED);

		throw new InsufficientFundsException("Insufficient funds");
	}

	private Wallet getWalletFromContext() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

		Wallet wallet = walletRepository.findByUser(user)
				.orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

		return wallet;
	}

	private void recordWalletTransaction(Wallet wallet, BigDecimal amount, TransactionType type,
			TransactionStatus status) {
		WalletTransaction walletTransaction = new WalletTransaction();
		walletTransaction.setAmount(amount);
		walletTransaction.setBalanceAfterTransaction(wallet.getBalance());
		walletTransaction.setCreatedOn(LocalDateTime.now());
		walletTransaction.setWallet(wallet);
		walletTransaction.setTransactionType(type);
		walletTransaction.setTransactionStatus(status);
		
		walletTransactionRepository.save(walletTransaction);

	}

}
