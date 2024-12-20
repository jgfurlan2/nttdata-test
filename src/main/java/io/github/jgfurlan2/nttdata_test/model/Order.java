package io.github.jgfurlan2.nttdata_test.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    private Long orderNumber;
    private Client client;
    private List<Product> products;
    private Long orderedAt;
    private String checksum;

}
