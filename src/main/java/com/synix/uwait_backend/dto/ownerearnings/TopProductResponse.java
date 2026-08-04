package com.synix.uwait_backend.dto.ownerearnings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Setter
@Getter
public class TopProductResponse {

    private String name;
    private BigDecimal productPrice;
    private long quantitySold;
    private BigDecimal revenue;
}

