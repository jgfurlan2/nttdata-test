package io.github.jgfurlan2.nttdata_test.ws;

import com.google.gson.Gson;
import io.github.jgfurlan2.nttdata_test.model.Order;
import io.github.jgfurlan2.nttdata_test.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class OrdersWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersWebSocketHandler.class);
    private static final Gson PARSER = new Gson();
    private final OrdersService ordersService;

    public OrdersWebSocketHandler(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();

            Long order = ordersService.receiveOrder(PARSER.fromJson(payload, Order.class));
            if (order == null) {
                session.sendMessage(new TextMessage("Duplicated order detected!"));
            } else {
                session.sendMessage(new TextMessage("Successfully registered order " + order + "!"));
            }
        } catch (Exception e) {
            session.sendMessage(new TextMessage("An error occurred on register order: " + e.getMessage()));
            LOGGER.error(e.getMessage(), e);
        }
    }

}
