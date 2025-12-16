package org.example;

import java.util.ArrayList;
import java.util.List;

public class Location {
    private int locationId;
    private String name;
    private String address;
    private double latitude;
    private double longitude;

    // Relationship: One Location has many Chargers
    private List<Charger> chargers;

    public Location(int id, String name, String address) {
        this.locationId = id;
        this.name = name;
        this.address = address;
        this.chargers = new ArrayList<>();
    }

    public void addCharger(Charger charger) {
        this.chargers.add(charger);
    }

    public List<Charger> getChargers() {
        return chargers;
    }

    public String getName() { return name; }
    public int getLocationId() { return locationId; }
}
