package org.example.domain;

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

    // Getters
    public List<Charger> getChargers() { return chargers; }
    public int getLocationId() { return locationId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + locationId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", lat=" + latitude +
                ", lng=" + longitude +
                ", chargers=" + chargers.size() +
                '}';
    }
}
