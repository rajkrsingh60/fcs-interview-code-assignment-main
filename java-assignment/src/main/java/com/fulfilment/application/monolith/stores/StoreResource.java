package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject
  private Event<StoreLegacySync> storeLegacySyncEvent;

  @Inject
  private StoreRepository storeRepository;


  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    LOGGER.info("Getting all stores");
    return storeRepository.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    LOGGER.infof("Getting single store with id %d", id);
    Store entity = storeRepository.findById(id);
    if (entity == null) {
      LOGGER.warnf("Store with id %d does not exist.", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    LOGGER.info("Creating new store");
    if (store.id != null) {
      LOGGER.warn("Id was invalidly set on request.");
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    storeRepository.persist(store);

    storeLegacySyncEvent.fire(new StoreLegacySync(StoreLegacySyncType.CREATE, store.id));

    LOGGER.infof("New store created with id %d", store.id);
    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    LOGGER.infof("Updating store with id %d", id);
    if (updatedStore.name == null) {
      LOGGER.warn("Store Name was not set on request.");
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = storeRepository.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store with id %d does not exist.", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;

    storeRepository.persist(entity);

    storeLegacySyncEvent.fire(new StoreLegacySync(StoreLegacySyncType.UPDATE, entity.id));

    LOGGER.infof("Store with id %d updated.", id);
    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    LOGGER.infof("Patching store with id %d", id);
    if (updatedStore.name == null) {
      LOGGER.warn("Store Name was not set on request.");
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = storeRepository.findById(id);

    if (entity == null) {
      LOGGER.warnf("Store with id %d does not exist.", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    if (entity.name != null) {
      entity.name = updatedStore.name;
    }

    if (entity.quantityProductsInStock != 0) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }

    storeRepository.persist(entity);

    storeLegacySyncEvent.fire(new StoreLegacySync(StoreLegacySyncType.UPDATE, entity.id));

    LOGGER.infof("Store with id %d patched.", id);
    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    LOGGER.infof("Deleting store with id %d", id);
    Store entity = storeRepository.findById(id);
    if (entity == null) {
      LOGGER.warnf("Store with id %d does not exist.", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    storeRepository.delete(entity);
    LOGGER.infof("Store with id %d deleted.", id);
    return Response.status(204).build();
  }
}
