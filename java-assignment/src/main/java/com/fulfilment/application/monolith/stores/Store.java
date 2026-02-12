package com.fulfilment.application.monolith.stores;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Cacheable
public class Store {

    @Id
    @GeneratedValue
    public Long id;

    @Column(length = 40, unique = true)
    public String name;

    public int quantityProductsInStock;

    public Store() {
    }

    public Store(String name) {
        this.name = name;
    }
}
