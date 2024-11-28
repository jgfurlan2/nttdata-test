package io.github.jgfurlan2.nttdata_test.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Product {

    private Long id;
    private String gs1;
    private String name;
    private Long quantity;
    private Double price;

}
