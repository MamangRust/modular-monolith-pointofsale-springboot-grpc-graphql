package com.sanedge.pointofsale.category.domain.responses;

import java.util.List;

import com.sanedge.pointofsale.category.models.CategoryMonthPrice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesMonthPriceResponse {
    private String month;
    private Integer categoryId;
    private String categoryName;
    private Integer orderCount;
    private Integer itemsSold;
    private Long totalRevenue;

    public static CategoriesMonthPriceResponse from(CategoryMonthPrice response) {
        return CategoriesMonthPriceResponse.builder()
                .month(response.getMonth())
                .categoryId(response.getCategoryId())
                .categoryName(response.getCategoryName())
                .orderCount(response.getOrderCount())
                .itemsSold(response.getItemsSold())
                .totalRevenue((long) response.getTotalRevenue())
                .build();
    }

    public static List<CategoriesMonthPriceResponse> fromList(List<CategoryMonthPrice> responses) {
        if (responses == null)
            return List.of();
        return responses.stream().map(CategoriesMonthPriceResponse::from).toList();
    }
}