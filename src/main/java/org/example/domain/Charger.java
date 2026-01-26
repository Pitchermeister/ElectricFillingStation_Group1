package org.example.domain;

public class Charger {
    private int chargerId;
    private int serialNumber;
    private double maxPowerKw;
    private ChargerStatus status;
    private int locationId; // Reference to Location

    // ADDED: Missing field for pricing
    private PriceConfiguration priceConfiguration;

    public Charger(int id, int serialNumber, double maxPowerKw) {
        this.chargerId = id;
        this.serialNumber = serialNumber;
        this.maxPowerKw = maxPowerKw;
        this.status = ChargerStatus.IN_OPERATION_FREE;
        this.locationId = -1; // No location assigned yet
        this.priceConfiguration = null; // Default to null until set
    }

    // --- GETTERS ---

    public int getChargerId() {
        return chargerId;
    }

    public double getMaxPowerKw() {
        return maxPowerKw;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public ChargerStatus getStatus() {
        return status;
    }

    public int getLocationId() {
        return locationId;
    }

    // ADDED: Getter for pricing
    public PriceConfiguration getPriceConfiguration() {
        return priceConfiguration;
    }

    // --- SETTERS ---
    public void setChargerId(int chargerId) {
        this.chargerId = chargerId;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setMaxPowerKw(double maxPowerKw) {
        this.maxPowerKw = maxPowerKw;
    }

    public void setStatus(ChargerStatus status) {
        this.status = status;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    // ADDED: Setter for pricing
    public void setPriceConfiguration(PriceConfiguration priceConfiguration) {
        this.priceConfiguration = priceConfiguration;
    }

    @Override
    public String toString() {
        return "Charger{" +
                "id=" + chargerId +
                ", serial=" + serialNumber +
                ", maxPower=" + maxPowerKw + "kW" +
                ", status=" + status +
                ", location=" + locationId +
                ", price=" + (priceConfiguration != null ? "SET" : "NONE") +
                '}';
    }
}