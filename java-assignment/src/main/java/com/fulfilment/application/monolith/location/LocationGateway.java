package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class LocationGateway implements LocationResolver {

    private static final Map<String, Location> locationIdentifierMap = new HashMap<>();

    static {
        locationIdentifierMap.put("ZWOLLE-001", new Location("ZWOLLE-001", 1, 40));
        locationIdentifierMap.put("ZWOLLE-002", new Location("ZWOLLE-002", 2, 50));
        locationIdentifierMap.put("AMSTERDAM-001", new Location("AMSTERDAM-001", 5, 100));
        locationIdentifierMap.put("AMSTERDAM-002", new Location("AMSTERDAM-002", 3, 75));
        locationIdentifierMap.put("TILBURG-001", new Location("TILBURG-001", 1, 40));
        locationIdentifierMap.put("HELMOND-001", new Location("HELMOND-001", 1, 45));
        locationIdentifierMap.put("EINDHOVEN-001", new Location("EINDHOVEN-001", 2, 70));
        locationIdentifierMap.put("VETSBY-001", new Location("VETSBY-001", 1, 90));
    }


    @Override
    public Location resolveByIdentifier(String identifier) {
        return locationIdentifierMap.get(identifier);
    }
}
