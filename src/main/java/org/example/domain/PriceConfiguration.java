package org.example.domain;

import java.time.LocalDateTime;

public class PriceConfiguration {
    private int priceConfigId;

    private double acPricePerKWh;
    private double dcPricePerKWh;

    private double acPricePerMinute;
    private double dcPricePerMinute;

    private LocalDateTime lastUpdated;  // Timestamp for price history

    public PriceConfiguration(int id,
                              double acPricePerKWh,
                              double dcPricePerKWh,
                              double acPricePerMinute,
                              double dcPricePerMinute) {
        this.priceConfigId = id;
        this.acPricePerKWh = acPricePerKWh;
        this.dcPricePerKWh = dcPricePerKWh;
        this.acPricePerMinute = acPricePerMinute;
        this.dcPricePerMinute = dcPricePerMinute;
        this.lastUpdated = LocalDateTime.now();
    }

    // Copy constructor for snapshot (frozen prices at session start)
    public PriceConfiguration(PriceConfiguration original) {
        this.priceConfigId = original.priceConfigId;
        this.acPricePerKWh = original.acPricePerKWh;
        this.dcPricePerKWh = original.dcPricePerKWh;
        this.acPricePerMinute = original.acPricePerMinute;
        this.dcPricePerMinute = original.dcPricePerMinute;
        this.lastUpdated = original.lastUpdated;  // Keeps original timestamp
    }

    public int getPriceConfigId() {
        return priceConfigId;
    }

    public void setPriceConfigId(int priceConfigId) {
        this.priceConfigId = priceConfigId;
    }

    public double getAcPricePerKWh() {
        return acPricePerKWh;
    }

    public void setAcPricePerKWh(double acPricePerKWh) {
        this.acPricePerKWh = acPricePerKWh;
        this.lastUpdated = LocalDateTime.now();  // Update timestamp
    }

    public double getDcPricePerKWh() {
        return dcPricePerKWh;
    }

    public void setDcPricePerKWh(double dcPricePerKWh) {
        this.dcPricePerKWh = dcPricePerKWh;
        this.lastUpdated = LocalDateTime.now();  // Update timestamp
    }

    public double getAcPricePerMinute() {
        return acPricePerMinute;
    }

    public void setAcPricePerMinute(double acPricePerMinute) {
        this.acPricePerMinute = acPricePerMinute;
        this.lastUpdated = LocalDateTime.now();  // Update timestamp
    }

    public double getDcPricePerMinute() {
        return dcPricePerMinute;
    }

    public void setDcPricePerMinute(double dcPricePerMinute) {
        this.dcPricePerMinute = dcPricePerMinute;
        this.lastUpdated = LocalDateTime.now();  // Update timestamp
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
