package com.ecomm.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;


@SpringBootApplication
@ServletComponentScan
public class OrderProcessingServiceApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(OrderProcessingServiceApplication.class, args);
    }

}
