package com.bhargav.titantrade.stock.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.stock.enums.AssetType;
import com.bhargav.titantrade.wallet.enums.CurrencyType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@Column(nullable = false, unique = true)
	private String ticker;
	@Column(nullable = false)
	private String companyName;
	@Column(nullable = false)
	private BigDecimal lastKnownPrice;
	@Column(nullable = false)
	private LocalDateTime lastPriceUpdatedAt;
	private String exchange;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private CurrencyType currency;
	private String sector;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AssetType assetType;
	private boolean isActive;
	@Column(nullable = false)
	private LocalDateTime createdOn;
	@Column(nullable = false)
	private LocalDateTime updatedOn;
	
	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdOn = now;
		this.updatedOn = now;
		if(this.lastPriceUpdatedAt == null)
			this.lastPriceUpdatedAt = now;
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedOn = LocalDateTime.now();
	}
	

}
