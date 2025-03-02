package com.product.productservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {

    @org.springframework.data.annotation.Id
    private String id;

    private String name;

    private String description;

    private BigDecimal price;

}
