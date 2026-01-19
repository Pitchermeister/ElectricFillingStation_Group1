package org.example.domain;

import java.time.LocalDateTime;

public class InvoiceEntry {

    private int itemNumber;
    private int clientId;
    private long sessionId;

    private String locationName;
    private int chargerId;
    private ChargerType mode;

    private LocalDateTime startTime;

    private long durationMinutes;
    private double chargedKWh;
    private double price;

    public InvoiceEntry(int itemNumber,
                        int clientId,
                        long sessionId,
                        String locationName,
                        int chargerId,
                        ChargerType mode,
                        LocalDateTime startTime,
                        long durationMinutes,
                        double chargedKWh,
                        double price) {

        this.itemNumber = itemNumber;
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.locationName = locationName;
        this.chargerId = chargerId;
        this.mode = mode;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.chargedKWh = chargedKWh;
        this.price = price;
    }

    public int getItemNumber() { return itemNumber; }
    public int getClientId() { return clientId; }
    public long getSessionId() { return sessionId; }
    public String getLocationName() { return locationName; }
    public int getChargerId() { return chargerId; }
    public ChargerType getMode() { return mode; }
    public LocalDateTime getStartTime() { return startTime; }
    public long getDurationMinutes() { return durationMinutes; }
    public double getChargedKWh() { return chargedKWh; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return itemNumber + ") " +
                "Session: " + sessionId +
                ", Location: " + locationName +
                ", Charger: " + chargerId +
                ", Mode: " + mode +
                ", Duration: " + durationMinutes + " min" +
                ", Energy: " + chargedKWh + " kWh" +
                ", Price: " + price;
    }
}
