package io.github.jgfurlan2.nttdata_test.service;

import io.github.jgfurlan2.nttdata_test.exception.*;
import io.github.jgfurlan2.nttdata_test.model.Address;
import io.github.jgfurlan2.nttdata_test.model.Client;
import io.github.jgfurlan2.nttdata_test.model.Order;
import io.github.jgfurlan2.nttdata_test.model.Product;
import io.github.jgfurlan2.nttdata_test.repository.OrdersRepository;
import io.github.jgfurlan2.nttdata_test.util.Dates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrdersServiceTests {

    @Mock
    private OrdersRepository repository;

    @InjectMocks
    private OrdersService ordersService;

    @BeforeEach
    public void setUp() {
        // Mock the repository to return the highest order number
        Order mockOrder = new Order();
        mockOrder.setOrderNumber(42L);
        when(repository.findFirstByOrderByOrderNumberDesc()).thenReturn(mockOrder);

        // Initialize the service
        ordersService = new OrdersService(repository);
    }

    @Test
    public void shouldReturnOrdersByClientId() throws Exception {
        Long clientId = 1L;
        List<Order> orders = List.of(new Order(), new Order());
        when(repository.listByClientId(clientId)).thenReturn(orders);

        List<Order> result = ordersService.getOrdersByClientId(clientId);
        assertEquals(orders, result);

        verify(repository).listByClientId(clientId);
    }

    @Test
    public void shouldReturnOrderByNumber() throws Exception {
        Long clientNumber = 43L;
        Order order = createDummyOrder();
        when(repository.findByOrderNumber(clientNumber)).thenReturn(order);

        Order result = ordersService.getOrderByOrderNumber(clientNumber);
        assertEquals(order, result);

        verify(repository).findByOrderNumber(clientNumber);
    }

    @Test
    public void shouldReturnOrdersByRange() throws Exception {
        long begin = 5;
        long end = 6;
        Order o1 = createDummyOrder();
        o1.setOrderedAt(5L);
        Order o2 = createDummyOrder();
        o2.setOrderedAt(5L);
        List<Order> orders = List.of(o1, o2);
        when(repository.listByRange(begin, end)).thenReturn(orders);

        List<Order> result = ordersService.getOrdersByRange(begin, end);
        assertEquals(orders, result);

        verify(repository).listByRange(begin, end);
    }

    @Test
    public void shouldThrowExceptionWhenBeginDateIsHigherThanEndDate() throws Exception {
        Exception e = assertThrows(InvalidOrderFilterException.class, () -> ordersService.getOrdersByRange(6L, 5L));
        assertEquals("Begin date is after end date", e.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMaxRangeExceeds() throws Exception {
        Exception e = assertThrows(InvalidOrderFilterException.class, () -> ordersService.getOrdersByRange(0L, Dates.DAY_TIMESTAMP + 1));
        assertEquals("Max range exceeded", e.getMessage());
    }

    @Test
    public void shouldCreateOrder() throws Exception {
        Order order = createDummyOrder();
        when(repository.insert(order)).thenReturn(order);

        assertEquals(ordersService.receiveOrder(order), 43L);
    }

    @Test
    public void shouldThrowExceptionWhenMissingChecksum() throws Exception {
        Order order = createDummyOrder();
        order.setChecksum(null);
        Exception e = assertThrows(InvalidOrderException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing order checksum", e.getMessage());

        Order order1 = createDummyOrder();
        order1.setChecksum("");
        Exception e1 = assertThrows(InvalidOrderException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Missing order checksum", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.setChecksum(" ");
        Exception e2 = assertThrows(InvalidOrderException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Missing order checksum", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClient() throws Exception {
        Order order = createDummyOrder();
        order.setClient(null);

        Exception e = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing client", e.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientId() throws Exception {
        Order order1 = createDummyOrder();
        order1.getClient().setId(null);
        Exception e1 = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Missing or invalid client id", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().setId(-1L);
        Exception e2 = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Missing or invalid client id", e2.getMessage());

        Order order3 = createDummyOrder();
        order3.getClient().setId(0L);
        Exception e3 = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order3));
        assertEquals("Missing or invalid client id", e3.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientName() throws Exception {
        Order order = createDummyOrder();
        order.getClient().setName(null);
        Exception e = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Null or empty client name", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getClient().setName("");
        Exception e1 = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Null or empty client name", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().setName(" ");
        Exception e2 = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Null or empty client name", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientTaxId() throws Exception {
        Order order = createDummyOrder();
        order.getClient().setTaxId(null);
        Exception e = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Null or empty client tax id", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getClient().setTaxId("");
        Exception e1 = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Null or empty client tax id", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().setTaxId(" ");
        Exception e2 = assertThrows(InvalidClientException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Null or empty client tax id", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientAddress() throws Exception {
        Order order = createDummyOrder();
        order.getClient().setAddress(null);
        Exception e = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing client address", e.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientAddressZipCode() throws Exception {
        Order order = createDummyOrder();
        order.getClient().getAddress().setZipCode(null);
        Exception e = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing client address zip code", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getClient().getAddress().setZipCode("");
        Exception e1 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Missing client address zip code", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().getAddress().setZipCode(" ");
        Exception e2 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Missing client address zip code", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientAddressStreetName() throws Exception {
        Order order = createDummyOrder();
        order.getClient().getAddress().setStreetName(null);
        Exception e = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing client address street name", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getClient().getAddress().setStreetName("");
        Exception e1 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Missing client address street name", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().getAddress().setStreetName(" ");
        Exception e2 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Missing client address street name", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientAddressStreetNumber() throws Exception {
        Order order = createDummyOrder();
        order.getClient().getAddress().setStreetNumber(null);
        Exception e = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing client address street number", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getClient().getAddress().setStreetNumber("");
        Exception e1 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Missing client address street number", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().getAddress().setStreetNumber(" ");
        Exception e2 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Missing client address street number", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientAddressNeighborhood() throws Exception {
        Order order = createDummyOrder();
        order.getClient().getAddress().setNeighborhood(null);
        Exception e = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing client address neighborhood", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getClient().getAddress().setNeighborhood("");
        Exception e1 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Missing client address neighborhood", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().getAddress().setNeighborhood(" ");
        Exception e2 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Missing client address neighborhood", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientAddressCity() throws Exception {
        Order order = createDummyOrder();
        order.getClient().getAddress().setCity(null);
        Exception e = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing client address city", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getClient().getAddress().setCity("");
        Exception e1 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Missing client address city", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().getAddress().setCity(" ");
        Exception e2 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Missing client address city", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientAddressState() throws Exception {
        Order order = createDummyOrder();
        order.getClient().getAddress().setState(null);
        Exception e = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing client address state", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getClient().getAddress().setState("");
        Exception e1 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Missing client address state", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().getAddress().setState(" ");
        Exception e2 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Missing client address state", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingClientAddressCountry() throws Exception {
        Order order = createDummyOrder();
        order.getClient().getAddress().setCountry(null);
        Exception e = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Missing client address country", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getClient().getAddress().setCountry("");
        Exception e1 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Missing client address country", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getClient().getAddress().setCountry(" ");
        Exception e2 = assertThrows(InvalidClientAddressException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Missing client address country", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingProducts() throws Exception {
        Order order1 = createDummyOrder();
        order1.setProducts(null);
        Exception e1 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Products list is null or empty", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.setProducts(Collections.emptyList());
        Exception e2 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Products list is null or empty", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingProduct() throws Exception {
        Order order = createDummyOrder();
        order.getProducts().set(0, null);
        Exception e = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Product 1 is null", e.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingProductId() throws Exception {
        Order order1 = createDummyOrder();
        order1.getProducts().get(0).setId(null);
        Exception e1 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Product 1 missing id", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getProducts().get(0).setId(-1L);
        Exception e2 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Product 1 missing id", e2.getMessage());

        Order order3 = createDummyOrder();
        order3.getProducts().get(0).setId(0L);
        Exception e3 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order3));
        assertEquals("Product 1 missing id", e3.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingProductGS1() throws Exception {
        Order order = createDummyOrder();
        order.getProducts().get(0).setGs1(null);
        Exception e = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Product 1 missing GS1 code", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getProducts().get(0).setGs1("");
        Exception e1 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Product 1 missing GS1 code", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getProducts().get(0).setGs1(" ");
        Exception e2 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Product 1 missing GS1 code", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingProductName() throws Exception {
        Order order = createDummyOrder();
        order.getProducts().get(0).setName(null);
        Exception e = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Product 1 missing name", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getProducts().get(0).setName("");
        Exception e1 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Product 1 missing name", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getProducts().get(0).setName(" ");
        Exception e2 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Product 1 missing name", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingProductQuantity() throws Exception {
        Order order = createDummyOrder();
        order.getProducts().get(0).setQuantity(null);
        Exception e = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Product 1 missing or invalid quantity", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getProducts().get(0).setQuantity(0L);
        Exception e1 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Product 1 missing or invalid quantity", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getProducts().get(0).setQuantity(-1L);
        Exception e2 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Product 1 missing or invalid quantity", e2.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenMissingProductPrice() throws Exception {
        Order order = createDummyOrder();
        order.getProducts().get(0).setPrice(null);
        Exception e = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order));
        assertEquals("Product 1 missing or invalid price", e.getMessage());

        Order order1 = createDummyOrder();
        order1.getProducts().get(0).setPrice(0d);
        Exception e1 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order1));
        assertEquals("Product 1 missing or invalid price", e1.getMessage());

        Order order2 = createDummyOrder();
        order2.getProducts().get(0).setPrice(-1d);
        Exception e2 = assertThrows(InvalidProductException.class, () -> ordersService.receiveOrder(order2));
        assertEquals("Product 1 missing or invalid price", e2.getMessage());
    }

    private Order createDummyOrder() {
        Order order = new Order();
        order.setOrderNumber(43L);
        order.setChecksum(UUID.randomUUID().toString());
        order.setClient(new Client());
        order.getClient().setId(1L);
        order.getClient().setName("Foo Bar");
        order.getClient().setTaxId("12345678900");
        order.getClient().setAddress(new Address());
        order.getClient().getAddress().setZipCode("01310200");
        order.getClient().getAddress().setStreetName("Avenida Paulista");
        order.getClient().getAddress().setStreetNumber("1578");
        order.getClient().getAddress().setNeighborhood("Bela Vista");
        order.getClient().getAddress().setCity("Sao Paulo");
        order.getClient().getAddress().setState("Sao Paulo");
        order.getClient().getAddress().setCountry("Brazil");
        order.setProducts(new ArrayList<>());
        Product p1 = new Product();
        p1.setId(1L);
        p1.setGs1("7908887777776");
        p1.setName("Something Product");
        p1.setQuantity(3L);
        p1.setPrice(10.9);
        order.getProducts().add(p1);
        Product p2 = new Product();
        p2.setId(2L);
        p2.setGs1("7908884443339");
        p2.setName("Another Product");
        p2.setQuantity(10L);
        p2.setPrice(5.28);
        order.getProducts().add(p2);
        order.setOrderedAt(System.currentTimeMillis());

        return order;
    }

}
