package com.fulfilment.application.monolith.warehouses.domain.models;

import java.time.LocalDateTime;

public class WarehouseDTO {

  // unique identifier
  public String businessUnitCode;

  public String location;

  public Integer capacity;

  public Integer stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  public void archive() {
    if (this.archivedAt != null) {
      throw new IllegalStateException("Warehouse is already archived.");
    }
    this.archivedAt = LocalDateTime.now();
  }
}
