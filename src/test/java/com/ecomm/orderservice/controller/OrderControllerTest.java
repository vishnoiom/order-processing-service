package com.ecomm.orderservice.controller;

import com.ecomm.orderservice.model.Order;
import com.ecomm.orderservice.service.OrderService;
import com.ecomm.orderservice.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateOrder_ValidToken() throws Exception {
        Order order = new Order(null, "prod123", "cust123", 2, null, null);
        Order savedOrder = new Order("1", "prod123", "cust123", 2, "CREATED", new BigDecimal("1000"));

        when(tokenService.validateToken(anyString(), eq("CUSTOMER"))).thenReturn("cust123");
        when(orderService.createOrder(any())).thenReturn(savedOrder);

        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer testtoken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void testCreateOrder_InvalidToken() throws Exception {
        Order order = new Order(null, "prod123", "cust123", 2, null, null);

        when(tokenService.validateToken(anyString(), eq("CUSTOMER")))
                .thenThrow(new org.springframework.web.reactive.function.client.WebClientResponseException(
                        401, "Unauthorized", null, null, null));

        mockMvc.perform(post("/orders")
                .header("Authorization", "Bearer badtoken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetOrderById() throws Exception {
        Order order = new Order("1", "prod123", "cust123", 2, "CREATED", new BigDecimal("1000"));

        when(orderService.getOrderById("1")).thenReturn(order);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }
}
