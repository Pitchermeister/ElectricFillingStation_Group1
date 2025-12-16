package org.example;

public class Charger {
    private int chargerId;
    private int serialNumber;
    private double maxPowerKw;
    private ChargerStatus status;
    private PriceConfiguration priceConfig; // Relationship: 1 Charger -> 1 PriceConfig

    public Charger(int id, int serialNumber, double maxPowerKw) {
        this.chargerId = id;
        this.serialNumber = serialNumber;
        this.maxPowerKw = maxPowerKw;
        this.status = ChargerStatus.AVAILABLE; // Default status
    }

    // --- GETTERS (Fixed names to match your tests) ---

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
}