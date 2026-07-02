package com.bhargav.titantrade.trade.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeHistoryResponse {
	
	private List<StockTransactionResponse> trades;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean last;
	

}
