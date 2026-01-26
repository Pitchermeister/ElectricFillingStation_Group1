package org.example.domain;

import org.example.Management.BillingManager;
import org.example.Management.ChargingManager;
import org.example.Management.ClientManager;
import org.example.Management.StationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class ChargingService {

    private final ClientManager clientManager;
    private final StationManager stationManager;
    private final BillingManager billingManager;
    private final ChargingManager chargingManager;

    private long nextSessionId;
    private ChargingSession activeSession; // RESTORED: Single session

    public ChargingService(ClientManager clientManager, StationManager stationManager, BillingManager billingManager) {
        this.clientManager = clientManager;
        this.stationManager = stationManager;
        this.billingManager = billingManager;
        this.chargingManager = new ChargingManager();
        this.nextSessionId = 1;
        this.activeSession = null;
    }

    // --- HELPER METHODS ---

    private Client requireClient(int clientId) {
        Client client = clientManager.getClientById(clientId);
        if (client == null) throw new IllegalArgumentException("Client not found: " + clientId);
        return client;
    }

    private Charger requireAvailableCharger(int chargerId) {
        Charger charger = stationManager.findChargerById(chargerId);
        if (charger == null) throw new IllegalArgumentException("Charger not found: " + chargerId);
        if (charger.getStatus() == ChargerStatus.OUT_OF_ORDER) throw new IllegalStateException("Charger is out of service");
        if (charger.getStatus() != ChargerStatus.IN_OPERATION_FREE) throw new IllegalStateException("Charger is not available");
        return charger;
    }

    private PriceConfiguration requirePricing(int locationId) {
        PriceConfiguration price = stationManager.getPricingForLocation(locationId);
        if (price == null) {
            throw new IllegalStateException("No price configuration set for location: " + locationId);
        }
        return price;
    }

    // --- MAIN ACTIONS ---

    public ChargingSession startSession(int clientId, int locationId, int chargerId, ChargerType mode, LocalDateTime startTime) {
        if (activeSession != null) throw new IllegalStateException("There is already an active session. Finish it first.");

        Client client = requireClient(clientId);
        Charger charger = requireAvailableCharger(chargerId);

        if (client.getAccount().getBalance() < 1.0) {
            throw new IllegalStateException("Insufficient balance. Client must have at least EUR 1.00");
        }

        long sessionId = nextSessionId++;

        PriceConfiguration currentPrice = requirePricing(locationId);
        PriceConfiguration priceSnapshot = new PriceConfiguration(currentPrice);

        activeSession = new ChargingSession(
                sessionId, clientId, locationId, chargerId, mode, priceSnapshot, startTime
        );

        charger.setStatus(ChargerStatus.OCCUPIED);
        return activeSession;
    }

    // RESTORED: Standard signature
    public double finishSession(LocalDateTime endTime, double chargedKWh) {
        if (activeSession == null) throw new IllegalStateException("No active session to finish.");

        activeSession.finish(endTime, chargedKWh);

        double totalCost = activeSession.calculateTotalPrice();
        Client client = requireClient(activeSession.getClientId());
        client.getAccount().debit(totalCost);

        Charger charger = stationManager.findChargerById(activeSession.getChargerId());
        if (charger != null && charger.getStatus() != ChargerStatus.OUT_OF_ORDER) {
            charger.setStatus(ChargerStatus.IN_OPERATION_FREE);
        }

        Location loc = stationManager.getLocationById(activeSession.getLocationId());
        String locationName = (loc != null) ? loc.getName() : ("LocationId " + activeSession.getLocationId());

        billingManager.createEntryFromSession(activeSession, locationName);
        chargingManager.addSession(activeSession);

        activeSession = null;

        return totalCost;
    }

    // --- GETTERS ---

    public ChargingSession getActiveSession() { return activeSession; }
    public boolean hasActiveSession() { return activeSession != null; }
    public boolean isVehicleCharging() { return activeSession != null; }
    public ChargingManager getChargingManager() { return chargingManager; }
    public BillingManager getBillingManager() { return billingManager; }

    // ==========================================
    // INNER CLASS: ChargingSession
    // ==========================================
    public static class ChargingSession {
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

        public ChargingSession(long sessionId, int clientId, int locationId, int chargerId,
                               ChargerType mode, PriceConfiguration priceAtStart, LocalDateTime startTime) {
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

        public void finish(LocalDateTime endTime, double chargedKWh) {
            if (finished) throw new IllegalStateException("Session already finished");
            Objects.requireNonNull(endTime, "endTime must not be null");
            if (endTime.isBefore(startTime)) throw new IllegalArgumentException("endTime must not be before startTime");
            if (chargedKWh < 0) throw new IllegalArgumentException("chargedKWh must be >= 0");

            this.endTime = endTime;
            this.chargedKWh = chargedKWh;
            this.finished = true;
        }

        public long getDurationMinutesRoundedUp() {
            LocalDateTime effectiveEnd = (endTime != null) ? endTime : LocalDateTime.now();
            long seconds = Duration.between(startTime, effectiveEnd).getSeconds();
            if (seconds <= 0) return 0;
            return (seconds + 59) / 60;
        }

        public double calculateTotalPrice() {
            double pricePerKWh = (mode == ChargerType.AC) ? priceAtStart.getAcPricePerKWh() : priceAtStart.getDcPricePerKWh();
            double pricePerMinute = (mode == ChargerType.AC) ? priceAtStart.getAcPricePerMinute() : priceAtStart.getDcPricePerMinute();
            return (chargedKWh * pricePerKWh) + (getDurationMinutesRoundedUp() * pricePerMinute);
        }

        public InvoiceEntry toInvoiceEntry(int itemNumber, String locationName) {
            return new InvoiceEntry(itemNumber, clientId, sessionId, locationName, chargerId, mode,
                    startTime, getDurationMinutesRoundedUp(), chargedKWh, calculateTotalPrice());
        }

        public long getSessionId() { return sessionId; }
        public int getClientId() { return clientId; }
        public int getChargerId() { return chargerId; }
        public int getLocationId() { return locationId; }
        public ChargerType getMode() { return mode; }
        public boolean isFinished() { return finished; }
        public LocalDateTime getStartTime() { return startTime; }
        public double getChargedKWh() { return chargedKWh; }
        public double getPrice() { return calculateTotalPrice(); }
        @Override
        public String toString() {
            return "ChargingSession{" + "sessionId=" + sessionId + ", clientId=" + clientId + ", finished=" + finished + '}';
        }
    }
}