package com.bhargav.titantrade.portfolio.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryResponse {
	
	private List<PortfolioHoldingResponse> holdings;
	private BigDecimal totalPortfolioValue;

}
