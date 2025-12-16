package org.example;

public class PriceConfiguration {
    private int priceConfigId;
    private double acPricePerKWh;
    private double dcPricePerKWh;

    public PriceConfiguration(int id, double acPrice, double dcPrice) {
        this.priceConfigId = id;
        this.acPricePerKWh = acPrice;
        this.dcPricePerKWh = dcPrice;
    }

    public int getPriceConfigId() {
        return priceConfigId;
    }

    public void setPriceConfigId(int priceConfigId) {
        this.priceConfigId = priceConfigId;
    }



    public void setAcPricePerKWh(double acPricePerKWh) {
        this.acPricePerKWh = acPricePerKWh;
    }


    public void setDcPricePerKWh(double dcPricePerKWh) {
        this.dcPricePerKWh = dcPricePerKWh;
    }
    public double getDcPricePerKWh() { return dcPricePerKWh; }
    public double getAcPricePerKWh() { return acPricePerKWh; }

}