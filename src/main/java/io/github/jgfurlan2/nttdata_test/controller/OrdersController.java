package io.github.jgfurlan2.nttdata_test.controller;

import io.github.jgfurlan2.nttdata_test.model.Order;
import io.github.jgfurlan2.nttdata_test.service.OrdersService;
import io.github.jgfurlan2.nttdata_test.util.Dates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class OrdersController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersController.class);

    private final OrdersService ordersService;

    public OrdersController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @GetMapping("/orders")
    public ResponseEntity<?> listOrders(
            @RequestParam(required = false) Long begin,
            @RequestParam(required = false) Long end
    ) {
        try {
            if (end == null) {
                end = System.currentTimeMillis();
            }

            if (begin == null) {
                begin = end - Dates.DAY_TIMESTAMP;
            }

            List<Order> orders = ordersService.getOrdersByRange(begin, end);
            return ResponseEntity.status(200).body(orders);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/orders/order/{orderNumber}")
    public ResponseEntity<?> findOrderByOrderNumber(@PathVariable("orderNumber") Long orderNumber) {
        try {
            Order order = ordersService.getOrderByOrderNumber(orderNumber);
            return ResponseEntity.status(200).body(order);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/orders/client/{clientId}")
    public ResponseEntity<?> listOrdersByClientId(@PathVariable("clientId") Long clientId) {
        try {
            List<Order> orders = ordersService.getOrdersByClientId(clientId);
            return ResponseEntity.status(200).body(orders);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

}
