package com.sanedge.pointofsale.category.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryYearTotalPrice {
    private String year;
    private Long totalRevenue;
}
