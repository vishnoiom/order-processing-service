package com.ecomm.orderservice.service;

import com.ecomm.orderservice.model.*;
import com.ecomm.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        order = new Order();
        order.setId("1");
        order.setCustomerId("cust123");
        order.setProductId("prod123");
        order.setQuantity(2);
    }

    @Test
    void testCreateOrder_ValidCustomerAndProduct() {
        CustomerDTO customer = new CustomerDTO("cust123", "John", "john@example.com", true);
        Product product = new Product("prod123", "Laptop", "Gaming Laptop", new BigDecimal("500"));

        when(restTemplate.getForObject("http://customer-service/customers/cust123", CustomerDTO.class)).thenReturn(customer);
        when(restTemplate.getForObject("http://product-service/products/prod123", Product.class)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order created = orderService.createOrder(order);

        assertNotNull(created);
        assertEquals("CREATED", created.getStatus());
        assertEquals(new BigDecimal("1000"), created.getTotalCost());
        verify(kafkaTemplate).send("order-events", created.getId());
    }

    @Test
    void testCreateOrder_InactiveCustomer_ShouldThrowException() {
        CustomerDTO customer = new CustomerDTO("cust123", "John", "john@example.com", false);

        when(restTemplate.getForObject("http://customer-service/customers/cust123", CustomerDTO.class)).thenReturn(customer);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(order));
        assertTrue(ex.getMessage().contains("Invalid or inactive customer"));
    }

    @Test
    void testGetOrderById() {
        when(orderRepository.findById("1")).thenReturn(Optional.of(order));
        Order found = orderService.getOrderById("1");
        assertNotNull(found);
        assertEquals("1", found.getId());
    }
}
