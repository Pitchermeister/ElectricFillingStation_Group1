package org.example;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== EV CHARGING APP (MVP 1) ===");

        // --------------------------------------------------
        // SHOWCASE 1: Client Management
        // --------------------------------------------------
        System.out.println("\n--- SCENARIO: Registering a Client ---");
        ClientManager clientManager = new ClientManager();

        // 1. Register a client
        Client myClient = clientManager.registerClient(1, "Alice Wonderland", "alice@univ.edu");

        // 2. Load some money (using the Account object inside Client)
        myClient.getAccount().topUp(50.0);

        // 3. Verify
        System.out.println("Created Client: " + myClient.getName());
        System.out.println("Current Balance: EUR " + myClient.getAccount().getBalance());


        // --------------------------------------------------
        // SHOWCASE 2: Station & Charger Management
        // --------------------------------------------------
        System.out.println("\n--- SCENARIO: Setting up a Station ---");
        StationManager stationManager = new StationManager();

        // 1. Create Location
        Location cityCenter = stationManager.createLocation(55, "City Center Hub", "1010 Vienna");
        System.out.println("Location Created: " + cityCenter.getName());

        // 2. Create Charger
        // (ID: 101, Serial: 99999, Power: 150kW)
        Charger fastCharger = new Charger(101, 99999, 150.0);

        // 3. Set Price Configuration (AC: 0.45, DC: 0.65)
        PriceConfiguration prices = new PriceConfiguration(1, 0.45, 0.65, 0.2, 0.2);
        fastCharger.setPriceConfiguration(prices);

        // 4. Link Charger to Location
        stationManager.addChargerToLocation(55, fastCharger);

        // 5. Verify logic by retrieving it
        // (We manually check the list here to prove it works)
        Charger retreivedCharger = cityCenter.getChargers().get(0);
        System.out.println("Charger [" + retreivedCharger.getChargerId() + "] is active.");
        System.out.println("DC Price is: EUR " + retreivedCharger.getPriceConfiguration().getDcPricePerKWh());

        //added charging service
        BillingService billingService = new BillingService();

        ChargingService chargingService = new ChargingService(clientManager, stationManager, billingService);

        // 8) Session starten
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 18, 10, 0);

        ChargingSession session = chargingService.startSession(
                1,              // clientId
                55,             // locationId
                101,            // chargerId
                ChargerType.AC, // AC oder DC
                startTime
        );

        System.out.println("Session started. SessionId: " + session.getSessionId());
        System.out.println("Charger status after start: " + fastCharger.getStatus());

        // 9) Session beenden (z.B. nach 20 Minuten, 12.5 kWh geladen)
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 18, 10, 20);

        double totalCost = chargingService.finishSession(endTime, 12.5);

        System.out.println("Session finished. Total cost: " + totalCost);
        System.out.println("Charger status after finish: " + fastCharger.getStatus());

        billingService.printInvoiceForClient(1, clientManager);

        //end of adding charging service

        System.out.println("\n=== EXECUTION FINISHED ===");
    }
}