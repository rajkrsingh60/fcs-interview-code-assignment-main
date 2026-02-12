package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreLegacySyncListener {

  @Inject StoreRepository storeRepository;
  @Inject LegacyStoreManagerGateway legacy;

  public void afterCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreLegacySync evt) {
    Store store = storeRepository.findById(evt.storeId());
    if (store == null) {
      // If it was deleted or missing, decide what you want to do. For now, skip.
      return;
    }

    switch (evt.type()) {
      case CREATE -> legacy.createStoreOnLegacySystem(store);
      case UPDATE -> legacy.updateStoreOnLegacySystem(store);
    }
  }
}
