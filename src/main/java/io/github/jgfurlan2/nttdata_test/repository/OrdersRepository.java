package io.github.jgfurlan2.nttdata_test.repository;

import io.github.jgfurlan2.nttdata_test.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OrdersRepository extends MongoRepository<Order, Long> {

    Order findFirstByOrderByOrderNumberDesc();

    Order findByChecksum(String checksum);

    Order findByOrderNumber(Long orderNumber);

    @Query("{ 'client._id': ?0 }")
    List<Order> listByClientId(Long clientId);

    @Query("{ 'orderedAt': { $gte: ?0, $lte: ?1 } }")
    List<Order> listByRange(Long begin, Long end);
}
