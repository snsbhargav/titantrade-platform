package com.bhargav.titantrade.trade.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.trade.enums.TradeStatus;
import com.bhargav.titantrade.trade.enums.TradeType;
import com.bhargav.titantrade.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "stock_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockTransaction {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id", nullable = false)
	private Stock stock;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TradeType tradeType;
	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal quantity;
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal pricePerShare;
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal totalAmount;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TradeStatus tradeStatus;
	@Column(nullable = false)
	private LocalDateTime executedAt;
	@Column(nullable = false)
	private LocalDateTime createdOn;
	@Column(nullable = false)
	private LocalDateTime updatedOn;

}
