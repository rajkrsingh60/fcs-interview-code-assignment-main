package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;

public interface CreateWarehouseOperation {
  void create(WarehouseDTO warehouse);
}
