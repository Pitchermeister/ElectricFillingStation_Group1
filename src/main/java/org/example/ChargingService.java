package org.example;

import java.time.LocalDateTime;

public class ChargingService {

    private final ClientManager clientManager;
    private final StationManager stationManager;
    private final BillingService billingService;

    private long nextSessionId;

    // Es gibt genau 0 oder 1 aktive Session (pro Account/Client)
    private ChargingSession activeSession;

    public ChargingService(ClientManager clientManager, StationManager stationManager, BillingService billingService) {
        this.clientManager = clientManager;
        this.stationManager = stationManager;
        this.billingService = billingService;
        this.nextSessionId = 1;
        this.activeSession = null;
    }

    public ChargingSession startSession(int clientId,
                                        int locationId,
                                        int chargerId,
                                        ChargerType mode,
                                        LocalDateTime startTime) {

        if (activeSession != null) {
            throw new IllegalStateException("There is already an active session. Finish it first.");
        }

        Client client = clientManager.getClientById(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }

        Charger charger = stationManager.findChargerById(chargerId);
        if (charger == null) {
            throw new IllegalArgumentException("Charger not found: " + chargerId);
        }

        if (charger.getStatus() == ChargerStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Charger is out of service");
        }
        if (charger.getStatus() != ChargerStatus.AVAILABLE) {
            throw new IllegalStateException("Charger is not available");
        }

        PriceConfiguration priceConfig = charger.getPriceConfiguration();
        if (priceConfig == null) {
            throw new IllegalStateException("No price configuration set for charger");
        }

        long sessionId = nextSessionId;
        nextSessionId++;

        activeSession = new ChargingSession(
                sessionId,
                clientId,
                locationId,
                chargerId,
                mode,
                priceConfig,
                startTime
        );

        charger.setStatus(ChargerStatus.IN_USE);

        return activeSession;
    }

    public double finishSession(LocalDateTime endTime, double chargedKWh) {

        if (activeSession == null) {
            throw new IllegalStateException("No active session to finish.");
        }

        // Session abschließen
        activeSession.finish(endTime, chargedKWh);

        // Kosten berechnen
        double totalCost = activeSession.calculateTotalPrice();

        // Geld abbuchen (Prepaid)
        Client client = clientManager.getClientById(activeSession.getClientId());
        if (client == null) {
            throw new IllegalStateException("Client not found for this session");
        }
        client.getAccount().debit(totalCost);

        // Charger wieder freigeben
        Charger charger = stationManager.findChargerById(activeSession.getChargerId());
        if (charger != null && charger.getStatus() != ChargerStatus.OUT_OF_SERVICE) {
            charger.setStatus(ChargerStatus.AVAILABLE);
        }

        Location loc = stationManager.getLocationById(activeSession.getLocationId());
        String locationName = (loc != null) ? loc.getName() : ("LocationId " + activeSession.getLocationId());

        billingService.addEntryFromSession(activeSession, locationName, totalCost);

        // aktive Session "löschen"
        activeSession = null;

        return totalCost;
    }

    public ChargingSession getActiveSession() {
        return activeSession;
    }

    public boolean hasActiveSession() {
        return activeSession != null;
    }
}
