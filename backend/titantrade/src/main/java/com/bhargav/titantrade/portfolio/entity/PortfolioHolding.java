package com.bhargav.titantrade.portfolio.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "portfolio_holdings", uniqueConstraints = {
		@UniqueConstraint(name = "uk_portfolio_user_stock", columnNames = { "user_id", "stock_id" }) })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioHolding {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id", nullable = false)
	private Stock stock;
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal averageBuyPrice;
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal quantity;
	private LocalDateTime createdOn;
	private LocalDateTime updatedOn;

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdOn = now;
		this.updatedOn = now;

	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedOn = LocalDateTime.now();
	}

	public PortfolioHolding(User user, Stock stock, BigDecimal avgBuyPrice, BigDecimal quantity) {
		this.user = user;
		this.stock = stock;
		this.averageBuyPrice = avgBuyPrice;
		this.quantity = quantity;

	}

}
