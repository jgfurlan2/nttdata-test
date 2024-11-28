package io.github.jgfurlan2.nttdata_test.config;

import io.github.jgfurlan2.nttdata_test.service.OrdersService;
import io.github.jgfurlan2.nttdata_test.ws.OrdersWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrdersService ordersService;

    public WebSocketConfig(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new OrdersWebSocketHandler(ordersService), "/order").setAllowedOrigins("*");
    }

}
