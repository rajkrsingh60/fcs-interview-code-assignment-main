package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;

public interface ArchiveWarehouseOperation {
  void archive(WarehouseDTO warehouse);
}
