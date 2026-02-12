package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LocationGatewayTest {

  private final LocationGateway locationGateway = new LocationGateway();

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
     Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");
     assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturnNull() {
    assertNull(locationGateway.resolveByIdentifier("NOLOCATION-001"));
  }

  @Test
  public void testWhenResolveLocationAsNullShouldReturnNull() {
    assertNull(locationGateway.resolveByIdentifier(null));
  }
}
