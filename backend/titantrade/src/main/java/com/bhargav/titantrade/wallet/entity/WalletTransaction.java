package com.bhargav.titantrade.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.wallet.enums.TransactionStatus;
import com.bhargav.titantrade.wallet.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransaction {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@ManyToOne
	@JoinColumn(name = "wallet_id", nullable = false)
	private Wallet wallet;
	@Column(precision = 19, scale = 2, nullable = false)
	private BigDecimal amount;
	@Column(precision = 19, scale = 2, nullable = false)
	private BigDecimal balanceAfterTransaction;
	@Enumerated(EnumType.STRING)
	private TransactionType transactionType;
	@Column(nullable = false)
	private LocalDateTime createdOn;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionStatus transactionStatus;
	

}
