package org.example;

import org.example.ChargerType;
import org.example.PriceConfiguration;

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

    // AC or DC (mode)
    private final ChargerType mode;

    // Price configuration valid at the START of this session
    private final PriceConfiguration priceAtStart;

    private final LocalDateTime startTime;
    private LocalDateTime endTime;

    // Energy charged in kWh (set at end)
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
        if (finished) {
            throw new IllegalStateException("Session already finished");
        }
        Objects.requireNonNull(endTime, "endTime must not be null");

        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("endTime must not be before startTime");
        }
        if (chargedKWh < 0) {
            throw new IllegalArgumentException("chargedKWh must be >= 0");
        }

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

        Duration d = Duration.between(startTime, effectiveEnd);
        long seconds = d.getSeconds();

        if (seconds <= 0) return 0;

        // round up to full minutes
        return (seconds + 59) / 60;
    }

    /**
     * Calculates the total price for this session using priceAtStart.
     * total = kWh * pricePerKWh + minutes * pricePerMinute
     *
     * If the session is not finished yet, this returns an estimate using current duration and chargedKWh (usually 0 until finished).
     */
    public double calculateTotalPrice() {
        double pricePerKWh = getPricePerKWhForMode();
        double pricePerMinute = getPricePerMinuteForMode();

        long minutes = getDurationMinutesRoundedUp();

        return (chargedKWh * pricePerKWh) + (minutes * pricePerMinute);
    }

    private double getPricePerKWhForMode() {
        return switch (mode) {
            case AC -> priceAtStart.getAcPricePerKWh();
            case DC -> priceAtStart.getDcPricePerKWh();
        };
    }

    private double getPricePerMinuteForMode() {
        return switch (mode) {
            case AC -> priceAtStart.getAcPricePerMinute();
            case DC -> priceAtStart.getDcPricePerMinute();
        };
    }

    // -------- Getters --------

    public long getSessionId() {
        return sessionId;
    }

    public int getClientId() {
        return clientId;
    }

    public int getLocationId() {
        return locationId;
    }

    public int getChargerId() {
        return chargerId;
    }

    public ChargerType getMode() {
        return mode;
    }

    public PriceConfiguration getPriceAtStart() {
        return priceAtStart;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public double getChargedKWh() {
        return chargedKWh;
    }

    public boolean isFinished() {
        return finished;
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
