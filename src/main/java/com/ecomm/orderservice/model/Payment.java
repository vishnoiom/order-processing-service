package com.ecomm.orderservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    private String id;
    private String orderId;
    private String customerId;
    private BigDecimal amount;
    private String status;  // e.g. PENDING, SUCCESS, FAILED
    private LocalDateTime paymentDate;
}