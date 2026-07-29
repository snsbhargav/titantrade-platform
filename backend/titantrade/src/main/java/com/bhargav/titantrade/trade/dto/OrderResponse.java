package com.bhargav.titantrade.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.trade.entity.Order;
import com.bhargav.titantrade.trade.enums.OrderStatus;
import com.bhargav.titantrade.trade.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

	private UUID id;
	private UUID stockId;
	private TradeType tradeType;
	private OrderStatus orderStatus;
	private BigDecimal quantity;
	private BigDecimal requestedPrice;
	private BigDecimal executionPrice;
	private BigDecimal totalAmount;
	private String rejectionReason;
	private UUID idempotencyKey;
	private LocalDateTime createdOn;
	private LocalDateTime executedAt;
	
	public static OrderResponse toDto(Order order) {
		OrderResponse orderResponse = new OrderResponse();
		orderResponse.setId(order.getId());
		orderResponse.setStockId(order.getStock().getId());
		orderResponse.setTradeType(order.getTradeType());
		orderResponse.setOrderStatus(order.getOrderStatus());
		orderResponse.setQuantity(order.getQuantity());
		orderResponse.setRequestedPrice(order.getRequestedPrice());
		orderResponse.setExecutionPrice(order.getExecutionPrice());
		orderResponse.setTotalAmount(order.getTotalAmount());
		orderResponse.setRejectionReason(order.getRejectionReason());
		orderResponse.setIdempotencyKey(order.getIdempotencyKey());
		orderResponse.setCreatedOn(order.getCreatedOn());
		orderResponse.setExecutedAt(order.getExecutedAt());
		return orderResponse;
	}
}
