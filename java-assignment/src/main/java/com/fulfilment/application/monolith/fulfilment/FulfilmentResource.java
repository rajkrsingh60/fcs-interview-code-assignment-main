
package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.StoreRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/fulfilment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfilmentResource {

    @Inject
    FulfilmentService fulfilmentService;

    @Inject
    ProductRepository productRepository;

    @Inject
    WarehouseRepository warehouseRepository;

    @Inject
    StoreRepository storeRepository;

    @POST
    @Transactional
    public Response addFulfilment(FulfilmentRequest request) {
        Product product = productRepository.findById(request.productId);
        DbWarehouse warehouse = warehouseRepository.find("id", request.warehouseId).firstResult();
        Store store = storeRepository.findById(request.storeId);

        if (product == null || warehouse == null || store == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            fulfilmentService.addFulfilment(product, warehouse, store);
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }
}
