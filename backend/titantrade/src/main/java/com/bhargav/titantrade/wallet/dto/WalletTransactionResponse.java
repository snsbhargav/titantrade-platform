package com.bhargav.titantrade.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.wallet.entity.WalletTransaction;
import com.bhargav.titantrade.wallet.enums.TransactionStatus;
import com.bhargav.titantrade.wallet.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionResponse {

	private UUID id;
	private BigDecimal amount;
	private BigDecimal balanceAfterTransaction;
	private TransactionType transactionType;
	private LocalDateTime createdOn;
	private TransactionStatus transactionStatus;
	
	public static WalletTransactionResponse toDto(WalletTransaction walletTransaction) {
		WalletTransactionResponse transaction = new WalletTransactionResponse();
		transaction.setId(walletTransaction.getId());
		transaction.setAmount(walletTransaction.getAmount());
		transaction.setBalanceAfterTransaction(walletTransaction.getBalanceAfterTransaction());
		transaction.setTransactionType(walletTransaction.getTransactionType());
		transaction.setTransactionStatus(walletTransaction.getTransactionStatus());
		return transaction;
	}
	
	
}
