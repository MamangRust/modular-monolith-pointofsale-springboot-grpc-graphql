package com.sanedge.pointofsale.common.domain.responses.api;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationMeta {
    private int page;
    private int pageSize;
    private long totalItems;
    private int totalPages;

    public static PaginationMeta fromSpringPage(Page<?> pageData) {
        return new PaginationMeta(
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages());
    }
}