package com.bhargav.titantrade.trade.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bhargav.titantrade.common.response.ApiResponse;
import com.bhargav.titantrade.common.security.CurrentUserService;
import com.bhargav.titantrade.trade.dto.OrderHistoryResponse;
import com.bhargav.titantrade.trade.dto.OrderResponse;
import com.bhargav.titantrade.trade.entity.Order;
import com.bhargav.titantrade.trade.repository.OrderRepository;
import com.bhargav.titantrade.user.entity.User;


@Service
public class OrderService {

	private final CurrentUserService currentUserService;

	private final OrderRepository orderRepository;

	public OrderService(CurrentUserService currentUserService, OrderRepository orderRepository) {
		this.currentUserService = currentUserService;
		this.orderRepository = orderRepository;
		// TODO Auto-generated constructor stub
	}

	@Transactional(readOnly = true)
	public ApiResponse getOrderHistoryByUser(int page, int size) {
		if (size > 100)
			size = 100;
		if (size <= 0)
			size = 10;
		if (page < 0)
			page = 0;
		User user = currentUserService.getCurrentUser();
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdOn").descending());
		Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);
		List<OrderResponse> response = new ArrayList<>();
		for (Order order : orders) {
			response.add(OrderResponse.toDto(order));
		}
		OrderHistoryResponse historyResponse = new OrderHistoryResponse(response, page, orders.getTotalPages(), size,
				orders.isLast(), orders.getTotalElements());
		return new ApiResponse(true, "Orders retrieved successfully", historyResponse);
	}

}
