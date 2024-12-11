package io.github.jgfurlan2.nttdata_test.service;

import io.github.jgfurlan2.nttdata_test.exception.*;
import io.github.jgfurlan2.nttdata_test.model.Address;
import io.github.jgfurlan2.nttdata_test.model.Client;
import io.github.jgfurlan2.nttdata_test.model.Order;
import io.github.jgfurlan2.nttdata_test.model.Product;
import io.github.jgfurlan2.nttdata_test.repository.OrdersRepository;
import io.github.jgfurlan2.nttdata_test.util.Dates;
import io.github.jgfurlan2.nttdata_test.util.Strings;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdersService {

    private Long latestOrderNumber;
    private final OrdersRepository repository;

    public OrdersService(OrdersRepository repository) {
        this.repository = repository;

        Order highestOrderNumber = repository.findFirstByOrderByOrderNumberDesc();
        if (highestOrderNumber != null) {
            this.latestOrderNumber = highestOrderNumber.getOrderNumber();
        } else {
            this.latestOrderNumber = 0L;
        }
    }

    public List<Order> getOrdersByClientId(Long clientId) throws Exception {
        return repository.listByClientId(clientId);
    }

    public Order getOrderByOrderNumber(Long orderNumber) throws Exception {
        return repository.findByOrderNumber(orderNumber);
    }

    public List<Order> getOrdersByRange(Long begin, Long end) throws Exception {
        if (begin > end) {
            throw new InvalidOrderFilterException("Begin date is after end date");
        }

        if (end - begin > Dates.DAY_TIMESTAMP) {
            throw new InvalidOrderFilterException("Max range exceeded");
        }

        return repository.listByRange(begin, end);
    }

    public Long receiveOrder(Order order) throws Exception {
        if (validateOrder(order)) {
            order.setOrderNumber(++latestOrderNumber);
            order.setOrderedAt(System.currentTimeMillis());
            repository.insert(order);

            return order.getOrderNumber();
        }

        return null;
    }

    private boolean validateOrder(Order order) throws Exception {
        if (Strings.isNullOrEmpty(order.getChecksum())) {
            throw new InvalidOrderException("Missing order checksum");
        }

        Client client = order.getClient();
        if (client == null) {
            throw new InvalidClientException("Missing client");
        } else if (client.getId() == null || client.getId() <= 0) {
            throw new InvalidClientException("Missing or invalid client id");
        } else if (Strings.isNullOrEmpty(client.getName())) {
            throw new InvalidClientException("Null or empty client name");
        } else if (Strings.isNullOrEmpty(client.getTaxId())) {
            throw new InvalidClientException("Null or empty client tax id");
        }

        Address address = client.getAddress();
        if (client.getAddress() == null) {
            throw new InvalidClientAddressException("Missing client address");
        } else if (Strings.isNullOrEmpty(address.getZipCode())) {
            throw new InvalidClientAddressException("Missing client address zip code");
        } else if (Strings.isNullOrEmpty(address.getStreetName())) {
            throw new InvalidClientAddressException("Missing client address street name");
        } else if (Strings.isNullOrEmpty(address.getStreetNumber())) {
            throw new InvalidClientAddressException("Missing client address street number");
        } else if (Strings.isNullOrEmpty(address.getNeighborhood())) {
            throw new InvalidClientAddressException("Missing client address neighborhood");
        } else if (Strings.isNullOrEmpty(address.getCity())) {
            throw new InvalidClientAddressException("Missing client address city");
        } else if (Strings.isNullOrEmpty(address.getState())) {
            throw new InvalidClientAddressException("Missing client address state");
        } else if (Strings.isNullOrEmpty(address.getCountry())) {
            throw new InvalidClientAddressException("Missing client address country");
        }

        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new InvalidProductException("Products list is null or empty");
        } else {
            for (int i = 0; i < order.getProducts().size() - 1; i++) {
                Product product = order.getProducts().get(i);

                if (product == null) {
                    throw new InvalidProductException("Product " + (i + 1) + " is null");
                } else if (product.getId() == null || product.getId() <= 0) {
                    throw new InvalidProductException("Product " + (i + 1) + " missing id");
                } else if (Strings.isNullOrEmpty(product.getGs1())) {
                    throw new InvalidProductException("Product " + (i + 1) + " missing GS1 code");
                } else if (Strings.isNullOrEmpty(product.getName())) {
                    throw new InvalidProductException("Product " + (i + 1) + " missing name");
                } else if (product.getQuantity() == null || product.getQuantity() <= 0) {
                    throw new InvalidProductException("Product " + (i + 1) + " missing or invalid quantity");
                } else if (product.getPrice() == null || product.getPrice() <= 0) {
                    throw new InvalidProductException("Product " + (i + 1) + " missing or invalid price");
                }
            }
        }

        return repository.findByChecksum(order.getChecksum()) == null;
    }

}
