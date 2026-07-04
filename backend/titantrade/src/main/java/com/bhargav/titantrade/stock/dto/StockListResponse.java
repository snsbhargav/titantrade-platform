package com.bhargav.titantrade.stock.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockListResponse {

	private List<StockResponse> stocks;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean last;
	

}
