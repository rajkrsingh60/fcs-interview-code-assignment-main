package com.fulfilment.application.monolith.products;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  @Inject ProductRepository productRepository;

  private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());

  @GET
  public List<Product> get() {
    LOGGER.info("Getting all products");
    return productRepository.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Product getSingle(Long id) {
    LOGGER.infof("Getting single product with id %d", id);
    Product entity = productRepository.findById(id);
    if (entity == null) {
      LOGGER.warnf("Product with id %d does not exist.", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Product product) {
    LOGGER.info("Creating new product");
    if (product.id != null) {
      LOGGER.warn("Id was invalidly set on request.");
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    productRepository.persist(product);
    LOGGER.infof("New product created with id %d", product.id);
    return Response.ok(product).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Product update(Long id, Product product) {
    LOGGER.infof("Updating product with id %d", id);
    if (product.name == null) {
      LOGGER.warn("Product Name was not set on request.");
      throw new WebApplicationException("Product Name was not set on request.", 422);
    }

    Product entity = productRepository.findById(id);

    if (entity == null) {
      LOGGER.warnf("Product with id %d does not exist.", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }

    entity.name = product.name;
    entity.description = product.description;
    entity.price = product.price;
    entity.stock = product.stock;

    productRepository.persist(entity);

    LOGGER.infof("Product with id %d updated.", id);
    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    LOGGER.infof("Deleting product with id %d", id);
    Product entity = productRepository.findById(id);
    if (entity == null) {
      LOGGER.warnf("Product with id %d does not exist.", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    productRepository.delete(entity);
    LOGGER.infof("Product with id %d deleted.", id);
    return Response.status(204).build();
  }

}
