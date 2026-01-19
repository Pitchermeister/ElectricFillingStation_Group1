package org.example.domain;

import org.example.Management.BillingManager;
import org.example.Management.ChargingManager;
import org.example.Management.ClientManager;
import org.example.Management.StationManager;

import java.time.LocalDateTime;

public class ChargingService {

    private final ClientManager clientManager;
    private final StationManager stationManager;
    private final BillingManager billingManager;
    private final ChargingManager chargingManager;

    private long nextSessionId;
    private ChargingSession activeSession;

    public ChargingService(ClientManager clientManager, StationManager stationManager, BillingManager billingManager) {
        this.clientManager = clientManager;
        this.stationManager = stationManager;
        this.billingManager = billingManager;
        this.chargingManager = new ChargingManager();
        this.nextSessionId = 1;
        this.activeSession = null;
    }

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
        if (charger.getPriceConfiguration() == null) throw new IllegalStateException("No price configuration set for charger");
        return charger;
    }

    public ChargingSession startSession(int clientId,
                                        int locationId,
                                        int chargerId,
                                        ChargerType mode,
                                        LocalDateTime startTime) {

        if (activeSession != null) throw new IllegalStateException("There is already an active session. Finish it first.");

        Client client = requireClient(clientId);
        Charger charger = requireAvailableCharger(chargerId);

        if (client.getAccount().getBalance() < 1.0) {
            throw new IllegalStateException("Insufficient balance. Client must have at least EUR 1.00");
        }

        long sessionId = nextSessionId++;


        PriceConfiguration priceSnapshot = new PriceConfiguration(charger.getPriceConfiguration());

        activeSession = new ChargingSession(
                sessionId,
                clientId,
                locationId,
                chargerId,
                mode,
                priceSnapshot,  // Snapshot instead of current reference
                startTime
        );

        charger.setStatus(ChargerStatus.OCCUPIED);
        return activeSession;
    }

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

    public ChargingSession getActiveSession() {
        return activeSession;
    }

    public boolean hasActiveSession() {
        return activeSession != null;
    }

    public boolean isVehicleCharging() {
        return activeSession != null;
    }

    public long getSessionDurationMinutes() {
        return (activeSession != null) ? activeSession.getDurationMinutesRoundedUp() : 0;
    }

    public double getEstimatedCost() {
        return (activeSession != null) ? activeSession.calculateTotalPrice() : 0;
    }

    public ChargingManager getChargingManager() {
        return chargingManager;
    }

    public BillingManager getBillingManager() {
        return billingManager;
    }
}
