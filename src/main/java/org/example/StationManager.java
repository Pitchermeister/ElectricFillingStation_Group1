package org.example;

import java.util.ArrayList;
import java.util.List;

public class StationManager {
    private List<Location> locationDatabase = new ArrayList<>();
    private List<Charger> chargerDatabase = new ArrayList<>(); // Optional global list

    // Logic: Create Location
    public Location createLocation(int id, String name, String address) {
        Location newLoc = new Location(id, name, address);
        locationDatabase.add(newLoc);
        return newLoc;
    }

    // Logic: Add Charger to Location
    public void addChargerToLocation(int locationId, Charger charger) {
        // 1. Find the location
        Location loc = locationDatabase.stream()
                .filter(l -> l.getLocationId() == locationId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Location not found"));

        // 2. Add charger to that location
        loc.addCharger(charger);

        // 3. (Optional) Add to global list
        chargerDatabase.add(charger);

        System.out.println("Charger " + charger.getChargerId() + " added to " + loc.getName());
    }
    // Helper to find a charger across all locations
    public Charger findChargerById(int id) {
        for (Location loc : locationDatabase) {
            for (Charger c : loc.getChargers()) {
                if (c.getChargerId() == id) {
                    return c;
                }
            }
        }
        return null; // Not found
    }
}
