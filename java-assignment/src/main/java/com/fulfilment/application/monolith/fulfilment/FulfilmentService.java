
package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FulfilmentService {

    @Inject
    FulfilmentRepository fulfilmentRepository;

    public void addFulfilment(Product product, DbWarehouse warehouse, Store store) throws Exception {
        if (fulfilmentRepository.count("product = ?1 and store = ?2", product, store) >= 2) {
            throw new Exception("A product can only be fulfilled by a maximum of 2 warehouses per store.");
        }

        Fulfilment fulfilment = new Fulfilment();
        fulfilment.product = product;
        fulfilment.warehouse = warehouse;
        fulfilment.store = store;

        fulfilmentRepository.persist(fulfilment);
    }
}
