package com.bhargav.titantrade.trade.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryResponse {
	
	private List<OrderResponse> orders;
	private int page;
	private int totalPages;
	private int size;
	private boolean last;
	private long totalElements;

}
