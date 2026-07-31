package com.bhargav.titantrade.trade.specification;

import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.bhargav.titantrade.trade.entity.Order;
import com.bhargav.titantrade.trade.enums.OrderStatus;
import com.bhargav.titantrade.trade.enums.TradeType;

public class OrderSpecification {

	private OrderSpecification() {

	}

	public static Specification<Order> filterByUserId(UUID userId) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
	}

	public static Specification<Order> filterByOnOrderStatus(OrderStatus orderStatus) {
		return (root, query, CriteiaBuilder) -> {
			if (orderStatus == null)
				return CriteiaBuilder.conjunction();
			return CriteiaBuilder.equal(root.get("orderStatus"), orderStatus);
		};
	}

	public static Specification<Order> filterByOnTradeType(TradeType tradeType) {
		return (root, query, CriteiaBuilder) -> {
			if (tradeType == null)
				return CriteiaBuilder.conjunction();

			return CriteiaBuilder.equal(root.get("tradeType"), tradeType);
		};
	}
	
	public static Specification<Order> filterByOnStock(UUID stockId) {
		return (root, query, CriteiaBuilder) -> {
			if (stockId == null)
				return CriteiaBuilder.conjunction();

			return CriteiaBuilder.equal(root.get("stock").get("id"), stockId);
		};
	}

}
