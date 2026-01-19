package org.example.domain;

public class Charger {
    private int chargerId;
    private int serialNumber;
    private double maxPowerKw;
    private ChargerStatus status;
    private PriceConfiguration priceConfig;
    private int locationId; // Reference to Location

    public Charger(int id, int serialNumber, double maxPowerKw) {
        this.chargerId = id;
        this.serialNumber = serialNumber;
        this.maxPowerKw = maxPowerKw;
        this.status = ChargerStatus.IN_OPERATION_FREE;
        this.locationId = -1; // No location assigned yet
    }

    // --- GETTERS ---

    public PriceConfiguration getPriceConfiguration() {
        return priceConfig;
    }

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

    // --- SETTERS ---

    public void setPriceConfiguration(PriceConfiguration config) {
        this.priceConfig = config;
    }

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

    @Override
    public String toString() {
        return "Charger{" +
                "id=" + chargerId +
                ", serial=" + serialNumber +
                ", maxPower=" + maxPowerKw + "kW" +
                ", status=" + status +
                ", location=" + locationId +
                '}';
    }
}