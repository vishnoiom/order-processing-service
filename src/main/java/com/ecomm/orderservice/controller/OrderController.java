package com.ecomm.orderservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.ecomm.orderservice.model.Order;
import com.ecomm.orderservice.service.OrderService;
import com.ecomm.orderservice.service.TokenService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    TokenService tokenService;
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	
	final String userType="CUSTOMER";

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order, @RequestHeader("Authorization") String tokenValue) {
    	String phone = null;
    	try
        {
            phone =  tokenService.validateToken(tokenValue, userType);
        }
        catch (WebClientResponseException e)
        {
            logger.info("Token validation failed: " + e.getMessage());
            return ResponseEntity.status(401).body(e.getResponseBodyAsString());
        }
        Order savedOrder=orderService.createOrder(order);
        return ResponseEntity.status(201).body(savedOrder);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable String id) {
        return orderService.getOrderById(id);
    }



}
