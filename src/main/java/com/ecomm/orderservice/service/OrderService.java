package com.ecomm.orderservice.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ecomm.orderservice.common.aop.LoggingAspect;
import com.ecomm.orderservice.model.CustomerDTO;
import com.ecomm.orderservice.model.Order;
import com.ecomm.orderservice.model.Payment;
import com.ecomm.orderservice.model.Product;
import com.ecomm.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;


@Service
public class OrderService {
	@Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    public Order createOrder(Order order) {
    	
    	CustomerDTO customerResponse=getCustomer(order.getCustomerId());

        if (customerResponse == null || !customerResponse.isActive()) {
        	throw new RuntimeException("Invalid or inactive customer: " + order.getCustomerId());

        }
        Product product=getProduct(order.getProductId());
        
        BigDecimal productPrice=product.getPrice();
      
        BigDecimal totalCost=productPrice.multiply(BigDecimal.valueOf(order.getQuantity()));
        order.setTotalCost(totalCost);
        
        restTemplate.put("http://INVENTORY-SERVICE/inventory/decrease/" + order.getProductId() + "?quantity=" + order.getQuantity(), null);
        
        order.setStatus("CREATED");
        Order savedOrder = orderRepository.save(order);

        kafkaTemplate.send("order-events", savedOrder.getId());

        return savedOrder;
    }

    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }
    

    @KafkaListener(topics = "payment-events")
    public void consumeProductEvent(String message) throws JsonMappingException, JsonProcessingException {
    	Payment payment = objectMapper.readValue(message, Payment.class);
    	Order pendingOrder=getOrderById(payment.getOrderId());
    	pendingOrder.setStatus("PAID");
    	orderRepository.save(pendingOrder);
    }

    @CircuitBreaker(name = "customerService", fallbackMethod = "customerFallback")
    public CustomerDTO getCustomer(String customerId) {
        return restTemplate.getForObject("http://customer-service/customers/" + customerId, CustomerDTO.class);
    }

    public CustomerDTO customerFallback(String customerId, Throwable t) {
        logger.warn("Fallback for getCustomer: {}", t.getMessage());
        return new CustomerDTO(customerId, "Fallback", "unknown@fallback.com", false);
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
    public Product getProduct(String productId) {
        return restTemplate.getForObject("http://product-service/products/" + productId, Product.class);
    }

    public CustomerDTO productFallback(String productId, Throwable t) {
        logger.warn("Fallback for getProduct: {}", t.getMessage());
        return new CustomerDTO(productId, "Fallback", "unknown@fallback.com", false);
    }
    
    

}
