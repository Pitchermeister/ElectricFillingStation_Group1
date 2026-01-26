package org.example.Management;

import org.example.domain.Charger;
import org.example.domain.ChargerStatus;
import org.example.domain.Location;
import org.example.domain.PriceConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages charging locations and chargers.
 */
public class StationManager {
    private final List<Location> locations = new ArrayList<>();
    private int id = 1;

    // CREATE
    public Location createLocation(String name, String address) {
        Location location = new Location(this.id, name, address);
        locations.add(location);
        this.id += 1;
        return location;
    }

    public Location createLocation(int id, String name, String address) {
        Location location = new Location(id, name, address);
        locations.add(location);
        return location;
    }

    public void addChargerToLocation(int locationId, Charger charger) {
        Location location = getLocationById(locationId);
        if (location != null) {
            charger.setLocationId(locationId);
            location.addCharger(charger);
        }
    }

    // READ
    public Location getLocationById(int id) {
        for (Location loc : locations) {
            if (loc.getLocationId() == id) return loc;
        }
        return null;
    }

    public Charger findChargerById(int id) {
        for (Location loc : locations) {
            for (Charger charger : loc.getChargers()) {
                if (charger.getChargerId() == id) return charger;
            }
        }
        return null;
    }

    public List<Location> getAllLocations() {
        return new ArrayList<>(locations);
    }

    // UPDATE
    public void updateLocation(int id, String name, String address) {
        Location location = getLocationById(id);
        if (location != null) {
            location.setName(name);
            location.setAddress(address);
        }
    }

    // DELETE
    public void deleteLocation(int id) {
        locations.removeIf(loc -> loc.getLocationId() == id);
    }

    // --- PRICING FIX ---
    public void updateLocationPricing(int locationId, double acKWh, double dcKWh, double acMin, double dcMin) {
        Location location = getLocationById(locationId);

        if (location == null) return;

        // 1. Create the pricing object
        PriceConfiguration pricing = new PriceConfiguration(
                location.getLocationId(),
                acKWh, dcKWh, acMin, dcMin);

        // 2. ROOT CAUSE FIX: Save it to the Location object!
        location.setPriceConfiguration(pricing);

        // 3. Also propagate to existing chargers (as before)
        for (Charger charger : location.getChargers()) {
            charger.setPriceConfiguration(pricing);
        }
    }

    public PriceConfiguration getPricingForLocation(int locationId) {
        Location location = getLocationById(locationId);
        if (location == null) return null;

        // FIX: Return the configuration directly from the location
        return location.getPriceConfiguration();
    }
    // -------------------

    // NETWORK STATUS
    public int getAvailableChargersCount() {
        int count = 0;
        for (Location loc : locations) {
            for (Charger c : loc.getChargers()) {
                if (c.getStatus() == ChargerStatus.IN_OPERATION_FREE) count++;
            }
        }
        return count;
    }

    public int getInUseChargersCount() {
        int count = 0;
        for (Location loc : locations) {
            for (Charger c : loc.getChargers()) {
                if (c.getStatus() == ChargerStatus.OCCUPIED) count++;
            }
        }
        return count;
    }

    public int getTotalChargersCount() {
        int count = 0;
        for (Location loc : locations) {
            count += loc.getChargers().size();
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== CHARGING NETWORK ===\n\n");

        if (locations.isEmpty()) {
            sb.append("No locations.\n");
            return sb.toString();
        }

        for (Location loc : locations) {
            sb.append(loc.getName()).append(" [").append(loc.getAddress()).append("]\n");

            // Now this will work correctly because the Location has the price
            PriceConfiguration p = loc.getPriceConfiguration();

            if (p != null) {
                sb.append("  AC: €").append(p.getAcPricePerKWh()).append("/kWh+€")
                        .append(p.getAcPricePerMinute()).append("/min | DC: €")
                        .append(p.getDcPricePerKWh()).append("/kWh+€")
                        .append(p.getDcPricePerMinute()).append("/min\n");
            } else {
                sb.append("  Pricing: Not configured\n");
            }

            int available = 0;
            for (Charger c : loc.getChargers()) {
                String status = c.getStatus() == ChargerStatus.IN_OPERATION_FREE ? "AVAILABLE" :
                        c.getStatus() == ChargerStatus.OCCUPIED ? "OCCUPIED" : "OUT OF ORDER";
                sb.append("  #").append(c.getChargerId()).append(" [")
                        .append(c.getMaxPowerKw()).append("kW] - ").append(status).append("\n");
                if (c.getStatus() == ChargerStatus.IN_OPERATION_FREE) available++;
            }
            sb.append("  (").append(available).append("/").append(loc.getChargers().size())
                    .append(" available)\n\n");
        }

        sb.append("Total: ").append(locations.size()).append(" locations, ")
                .append(getTotalChargersCount()).append(" chargers\n");

        return sb.toString();
    }
}