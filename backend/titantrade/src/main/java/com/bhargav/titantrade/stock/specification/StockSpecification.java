package com.bhargav.titantrade.stock.specification;

import org.springframework.data.jpa.domain.Specification;

import com.bhargav.titantrade.stock.entity.Stock;

public class StockSpecification {

	public StockSpecification() {
	}

	public static Specification<Stock> isActive() {
		return (root, query, CriteriaBuilder) -> CriteriaBuilder.isTrue(root.get("isActive"));
	}

	public static Specification<Stock> searchByTickerOrCompanyName(String search) {
		return (root, qurey, CriteriaBuilder) -> {
			if (search == null || search.isBlank()) {
				return CriteriaBuilder.conjunction();
			}

			String searchKey = "%" + search.trim().toLowerCase() + "%";
			return CriteriaBuilder.or(CriteriaBuilder.like(CriteriaBuilder.lower(root.get("ticker")), searchKey),
					CriteriaBuilder.like(CriteriaBuilder.lower(root.get("companyName")), searchKey));
		};
	}

}
