package com.ecomm.orderservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ecomm.orderservice.model.Order;

public interface OrderRepository extends MongoRepository<Order, String> {

}
