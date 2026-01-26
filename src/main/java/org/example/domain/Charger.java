package org.example.domain;

public class Charger {
    private int chargerId;
    private int serialNumber;
    private double maxPowerKw;
    private ChargerStatus status;
    private int locationId;
    private PriceConfiguration priceConfiguration;

    // ADDED: Type field
    private ChargerType type;

    // Updated Constructor
    public Charger(int id, int serialNumber, double maxPowerKw, ChargerType type) {
        this.chargerId = id;
        this.serialNumber = serialNumber;
        this.maxPowerKw = maxPowerKw;
        this.type = type;
        this.status = ChargerStatus.IN_OPERATION_FREE;
        this.locationId = -1;
        this.priceConfiguration = null;
    }

    // OLD Constructor (Backwards compatibility for other tests if needed, defaults to AC or DC based on power)
    public Charger(int id, int serialNumber, double maxPowerKw) {
        this(id, serialNumber, maxPowerKw, maxPowerKw > 22 ? ChargerType.DC : ChargerType.AC);
    }

    // --- GETTERS ---
    public int getChargerId() { return chargerId; }
    public double getMaxPowerKw() { return maxPowerKw; }
    public int getSerialNumber() { return serialNumber; }
    public ChargerStatus getStatus() { return status; }
    public int getLocationId() { return locationId; }
    public PriceConfiguration getPriceConfiguration() { return priceConfiguration; }
    public ChargerType getType() { return type; }

    // --- SETTERS ---
    public void setChargerId(int chargerId) { this.chargerId = chargerId; }
    public void setSerialNumber(int serialNumber) { this.serialNumber = serialNumber; }
    public void setMaxPowerKw(double maxPowerKw) { this.maxPowerKw = maxPowerKw; }
    public void setStatus(ChargerStatus status) { this.status = status; }
    public void setLocationId(int locationId) { this.locationId = locationId; }
    public void setPriceConfiguration(PriceConfiguration priceConfiguration) { this.priceConfiguration = priceConfiguration; }
    public void setType(ChargerType type) { this.type = type; }

    @Override
    public String toString() {
        return "Charger{" +
                "id=" + chargerId +
                ", type=" + type +
                ", maxPower=" + maxPowerKw + "kW" +
                ", status=" + status +
                ", location=" + locationId +
                ", price=" + (priceConfiguration != null ? "SET" : "NONE") +
                '}';
    }
}