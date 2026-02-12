package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseDTO;
import java.util.List;

public interface WarehouseStore {

  List<WarehouseDTO> getAll();

  void create(WarehouseDTO warehouse);

  void update(WarehouseDTO warehouse);

  void remove(WarehouseDTO warehouse);

  WarehouseDTO findByBusinessUnitCode(String buCode);
}
