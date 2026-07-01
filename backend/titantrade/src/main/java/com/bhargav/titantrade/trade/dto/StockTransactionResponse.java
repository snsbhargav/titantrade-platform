package com.bhargav.titantrade.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bhargav.titantrade.stock.entity.Stock;
import com.bhargav.titantrade.trade.entity.StockTransaction;
import com.bhargav.titantrade.trade.enums.TradeStatus;
import com.bhargav.titantrade.trade.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockTransactionResponse {


	private UUID id;
	private UUID stockId;
	private String ticker;
	private String companyName;
	private TradeType tradeType;
	private BigDecimal quantity;
	private BigDecimal pricePerShare;
	private BigDecimal totalAmount;
	private TradeStatus tradeStatus;
	private LocalDateTime executedAt;
	private LocalDateTime createdOn;
	private LocalDateTime updatedOn;
	
	public static StockTransactionResponse toDto(StockTransaction stockTransaction) {
		StockTransactionResponse response = new StockTransactionResponse();
		Stock stock = stockTransaction.getStock();
		response.setId(stockTransaction.getId());
		response.setStockId(stock.getId());
		response.setTicker(stock.getTicker());
		response.setCompanyName(stock.getCompanyName());
		response.setTradeType(stockTransaction.getTradeType());
		response.setQuantity(stockTransaction.getQuantity());
		response.setPricePerShare(stockTransaction.getPricePerShare());
		response.setTotalAmount(stockTransaction.getTotalAmount());
		response.setTradeStatus(stockTransaction.getTradeStatus());
		response.setExecutedAt(stockTransaction.getExecutedAt());
		response.setCreatedOn(stockTransaction.getCreatedOn());
		response.setUpdatedOn(stockTransaction.getUpdatedOn());
		
		return response;
	}
}
