package com.bhargav.titantrade.trade.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.common.constants.DecimalConstants;
import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.trade.enums.OrderStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders", uniqueConstraints = {
		@UniqueConstraint(name = "uk_order_user_idempotency", columnNames = { "user_id", "idempotency_key" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id", nullable = false)
	private Stock stock;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private TradeType tradeType;
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private OrderStatus orderStatus;
	@Column(nullable = false, precision = 19, scale = DecimalConstants.QUANTITY_SCALE)
	private BigDecimal quantity;
	@Column(nullable = false, precision = 19, scale = DecimalConstants.PRICE_SCALE)
	private BigDecimal requestedPrice;
	@Column(precision = 19, scale = DecimalConstants.PRICE_SCALE)
	private BigDecimal executionPrice;
	@Column(precision = 19, scale = DecimalConstants.PRICE_SCALE)
	private BigDecimal totalAmount;
	@Column(length = 500)
	private String rejectionReason;
	@Column(nullable = false)
	private UUID idempotencyKey;
	@Column(nullable = false)
	private LocalDateTime createdOn;
	@Column(nullable = false)
	private LocalDateTime updatedOn;
	private LocalDateTime executedAt;

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdOn = now;
		this.updatedOn = now;
		if (this.orderStatus == null)
			this.orderStatus = OrderStatus.PENDING;
	}

	@PreUpdate
	protected void onUpdate() {
		LocalDateTime now = LocalDateTime.now();
		this.updatedOn = now;
	}

	public static Order createPendingOrder(User user, Stock stock, BigDecimal quantity, UUID idempotencyKey,
			TradeType tradeType) {
		Order order = new Order();
		order.setUser(user);
		order.setStock(stock);
		order.setOrderStatus(OrderStatus.PENDING);
		order.setQuantity(quantity);
		order.setIdempotencyKey(idempotencyKey);
		order.setTradeType(tradeType);
		order.setRequestedPrice(
				stock.getLastKnownPrice().setScale(DecimalConstants.PRICE_SCALE, DecimalConstants.ROUNDING_MODE));
		return order;
	}

	public void markExecuted(BigDecimal executionPrice, BigDecimal totalAmount) {
		this.executionPrice = executionPrice;
		this.totalAmount = totalAmount;
		this.executedAt = LocalDateTime.now();
		this.orderStatus = OrderStatus.EXECUTED;
	}

	public void markRejected(String reason) {
		this.orderStatus = OrderStatus.REJECTED;
		this.rejectionReason = reason;
	}

}
