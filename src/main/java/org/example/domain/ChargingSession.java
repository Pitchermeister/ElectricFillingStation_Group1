package org.example.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents one charging process of a client at a specific charger.
 * Prices are "frozen" at session start time (priceAtStart).
 */
public class ChargingSession {
    private final long sessionId;
    private final int clientId;
    private final int locationId;
    private final int chargerId;
    private final ChargerType mode;
    private final PriceConfiguration priceAtStart;
    private final LocalDateTime startTime;

    private LocalDateTime endTime;
    private double chargedKWh;
    private boolean finished;

    public ChargingSession(long sessionId,
                           int clientId,
                           int locationId,
                           int chargerId,
                           ChargerType mode,
                           PriceConfiguration priceAtStart,
                           LocalDateTime startTime) {
        this.sessionId = sessionId;
        this.clientId = clientId;
        this.locationId = locationId;
        this.chargerId = chargerId;
        this.mode = mode;
        this.priceAtStart = priceAtStart;
        this.startTime = startTime;
        this.finished = false;
        this.chargedKWh = 0.0;
        this.endTime = null;
    }

    /**
     * Ends the session with endTime and charged energy.
     */
    public void finish(LocalDateTime endTime, double chargedKWh) {
        if (finished) throw new IllegalStateException("Session already finished");
        Objects.requireNonNull(endTime, "endTime must not be null");
        if (endTime.isBefore(startTime)) throw new IllegalArgumentException("endTime must not be before startTime");
        if (chargedKWh < 0) throw new IllegalArgumentException("chargedKWh must be >= 0");

        this.endTime = endTime;
        this.chargedKWh = chargedKWh;
        this.finished = true;
    }

    /**
     * Returns the duration in minutes.
     * Billing-friendly default: round up to full minutes (e.g. 10:00:01 -> 1 minute).
     * If session is not finished, duration is calculated until 'now'.
     */
    public long getDurationMinutesRoundedUp() {
        LocalDateTime effectiveEnd = (endTime != null) ? endTime : LocalDateTime.now();
        long seconds = Duration.between(startTime, effectiveEnd).getSeconds();
        if (seconds <= 0) return 0;
        return (seconds + 59) / 60; // round up
    }

    /**
     * Calculates the total price for this session using priceAtStart.
     * total = kWh * pricePerKWh + minutes * pricePerMinute
     *
     * If the session is not finished yet, this returns an estimate using current duration and chargedKWh (usually 0 until finished).
     */
    public double calculateTotalPrice() {
        double pricePerKWh = (mode == ChargerType.AC) ? priceAtStart.getAcPricePerKWh() : priceAtStart.getDcPricePerKWh();
        double pricePerMinute = (mode == ChargerType.AC) ? priceAtStart.getAcPricePerMinute() : priceAtStart.getDcPricePerMinute();
        return (chargedKWh * pricePerKWh) + (getDurationMinutesRoundedUp() * pricePerMinute);
    }

    // Getters
    public long getSessionId() { return sessionId; }
    public int getClientId() { return clientId; }
    public int getLocationId() { return locationId; }
    public int getChargerId() { return chargerId; }
    public ChargerType getMode() { return mode; }
    public PriceConfiguration getPriceAtStart() { return priceAtStart; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public double getChargedKWh() { return chargedKWh; }
    public boolean isFinished() { return finished; }

    /**
     * Creates an {@link InvoiceEntry} for this session using the frozen prices and recorded times.
     * This keeps the price, start and end times clearly tied to the session.
     */
    public InvoiceEntry toInvoiceEntry(int itemNumber, String locationName) {
        return new InvoiceEntry(
                itemNumber,
                clientId,
                sessionId,
                locationName,
                chargerId,
                mode,
                startTime,
                getDurationMinutesRoundedUp(),
                chargedKWh,
                calculateTotalPrice()
        );
    }

    @Override
    public String toString() {
        return "ChargingSession{" +
                "sessionId=" + sessionId +
                ", clientId=" + clientId +
                ", locationId=" + locationId +
                ", chargerId=" + chargerId +
                ", mode=" + mode +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", chargedKWh=" + chargedKWh +
                ", finished=" + finished +
                '}';
    }
}
