package org.example;

public class PriceConfiguration {
    private int priceConfigId;

    private double acPricePerKWh;
    private double dcPricePerKWh;

    private double acPricePerMinute;
    private double dcPricePerMinute;

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
    }

    public double getDcPricePerKWh() {
        return dcPricePerKWh;
    }

    public void setDcPricePerKWh(double dcPricePerKWh) {
        this.dcPricePerKWh = dcPricePerKWh;
    }

    public double getAcPricePerMinute() {
        return acPricePerMinute;
    }

    public void setAcPricePerMinute(double acPricePerMinute) {
        this.acPricePerMinute = acPricePerMinute;
    }

    public double getDcPricePerMinute() {
        return dcPricePerMinute;
    }

    public void setDcPricePerMinute(double dcPricePerMinute) {
        this.dcPricePerMinute = dcPricePerMinute;
    }
}
