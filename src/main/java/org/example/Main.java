package org.example;

import org.example.Management.BillingManager;
import org.example.Management.ClientManager;
import org.example.Management.StationManager;
import org.example.domain.*;

import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== EV CHARGING APP (MVP 1) ===");
        System.out.println("--- PREPAID SYSTEM: Kunden müssen vorab registriert sein und ihr Kundenkonto aufladen ---\n");

        // 1) Mehrere Kunden registrieren und Prepaid-Konten aufladen
        ClientManager clientManager = new ClientManager();

        Client alice = clientManager.registerClient(1, "Alice Wonderland", "alice@univ.edu");
        alice.getAccount().topUp(100.0);
        System.out.println("✓ " + alice.getName() + " registered | Balance: EUR " + alice.getAccount().getBalance());

        Client bob = clientManager.registerClient(2, "Bob Builder", "bob@company.de");
        bob.getAccount().topUp(75.0);
        System.out.println("✓ " + bob.getName() + " registered | Balance: EUR " + bob.getAccount().getBalance());

        Client carol = clientManager.registerClient(3, "Carol Danvers", "carol@taxi.at");
        carol.getAccount().topUp(50.0);
        System.out.println("✓ " + carol.getName() + " registered | Balance: EUR " + carol.getAccount().getBalance());

        // 2) Station & Charger Netz auf 10 Standorte ausrollen
        StationManager stationManager = new StationManager();
        int[][] stationPlan = {
                {55, 2}, {56, 2}, {57, 1}, {58, 2}, {59, 1},
                {60, 2}, {61, 1}, {62, 2}, {63, 3}, {64, 2}
        };
        String[] stationNames = {
                "City Center Hub", "Airport Fast Charge", "Tech Park", "Harbor Plug-In", "Suburb North",
                "University Campus", "Mall West", "Industrial Zone", "Highway Rest", "Old Town"
        };
        String[] stationAddresses = {
                "1010 Vienna", "Airport Road 1", "Innovationsstrasse 2", "Dockside 7", "Nordallee 12",
                "Campusweg 3", "Mallring 8", "Werkstrasse 44", "A1 Exit 23", "Altstadtgasse 5"
        };

        int nextChargerId = 101;
        Location demoLocation = null;
        Charger demoCharger = null;
        for (int i = 0; i < stationPlan.length; i++) {
            int locId = stationPlan[i][0];
            int chargerCount = stationPlan[i][1];
            Location location = stationManager.createLocation(locId, stationNames[i], stationAddresses[i]);
            for (int c = 0; c < chargerCount; c++) {
                Charger charger = new Charger(nextChargerId, 900000 + nextChargerId, c % 2 == 0 ? 150.0 : 300.0);
                charger.setPriceConfiguration(defaultPricing(locId * 10 + c));
                stationManager.addChargerToLocation(locId, charger);
                if (demoLocation == null) { // nutze ersten Standort für Demo-Session
                    demoLocation = location;
                    demoCharger = charger;
                }
                nextChargerId++;
            }
        }
        System.out.println("Network seeded: " + stationManager.getAllLocations().size() + " locations, chargers: " + stationManager.getTotalChargersCount());

        // Ensure demo location and charger are set
        if (demoLocation == null || demoCharger == null) {
            System.err.println("ERROR: Demo location or charger not initialized!");
            return;
        }

        // 3) Services initialisieren
        BillingManager billingManager = new BillingManager();
        ChargingService chargingService = new ChargingService(clientManager, stationManager, billingManager);

        // 4) Demo-Ladesession: Alice lädt AC für 20 Min mit 12.5 kWh
        System.out.println("\n--- CHARGING SESSION DEMO (Billing: AC/DC mode + kWh + minutes) ---");
        System.out.println("Kunde: " + alice.getName() + " | Standort: " + demoLocation.getName() + " | Modus: AC");
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 18, 10, 0);
        ChargingSession session = chargingService.startSession(alice.getClientId(), demoLocation.getLocationId(), demoCharger.getChargerId(), ChargerType.AC, startTime);
        System.out.println("✓ Session gestartet: ID " + session.getSessionId() + " | Charger: " + demoCharger.getStatus());

        // 5) Ladesession beenden
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 18, 10, 20);
        double totalCost = chargingService.finishSession(endTime, 12.5);
        System.out.println("✓ Session beendet | Berechnung:");
        System.out.println("  - Lademodus: AC | kWh: 12.5 | Dauer: 20 min");
        System.out.println("  - Preis: EUR " + String.format("%.2f", totalCost) + " (kWh * EUR " +
                String.format("%.2f", demoCharger.getPriceConfiguration().getAcPricePerKWh()) + " + min * EUR " +
                String.format("%.2f", demoCharger.getPriceConfiguration().getAcPricePerMinute()) + ")");
        System.out.println("✓ Charger status: " + demoCharger.getStatus());

        // 6) Rechnungen & Kontostand
        System.out.println("\n--- ABRECHNUNG (Invoice nach Modus/kWh/Minuten) ---");
        System.out.println("Kunde: " + alice.getName());
        billingManager.getEntriesForClient(alice.getClientId()).forEach(entry ->
            System.out.println("  Item #" + entry.getItemNumber() + " | Session: " + entry.getSessionId() +
                             " | Mode: " + entry.getMode() + " | kWh: " + entry.getChargedKWh() +
                             " | Min: " + entry.getDurationMinutes() + " | EUR " + String.format("%.2f", entry.getPrice()))
        );
        double totalSpent = billingManager.getTotalSpentForClient(alice.getClientId());
        System.out.println("Summe Ausgegeben: EUR " + String.format("%.2f", totalSpent));
        double remainingBalance = alice.getAccount().getBalance();
        System.out.println("Restguthaben: EUR " + String.format("%.2f", remainingBalance));

        System.out.println("\n--- BUSINESS STATUS ---");
        System.out.println("Standorte registriert: " + stationManager.getAllLocations().size());
        System.out.println("Charger verfügbar: " + stationManager.getAvailableChargersCount());
        System.out.println("Charger in Nutzung: " + stationManager.getInUseChargersCount());
        System.out.println("Charger gesamt: " + stationManager.getTotalChargersCount());

        // 7) Weitere Demo-Sessions für Bob und Carol (verschiedene Lademodi/Ladezeiten)
        System.out.println("\n--- ADDITIONAL DEMO SESSIONS (Multi-Client Billing) ---");

        // Bob lädt DC für 15 Min mit 25 kWh
        System.out.println("\nKunde: " + bob.getName() + " | Modus: DC | kWh: 25 | Min: 15");
        Location bobLocation = stationManager.getAllLocations().get(1); // Zweiter Standort
        Charger bobCharger = bobLocation.getChargers().get(0);
        chargingService.startSession(bob.getClientId(), bobLocation.getLocationId(), bobCharger.getChargerId(), ChargerType.DC, LocalDateTime.of(2026, 1, 18, 14, 0));
        double bobCost = chargingService.finishSession(LocalDateTime.of(2026, 1, 18, 14, 15), 25.0);
        System.out.println("✓ Bob: EUR " + String.format("%.2f", bobCost) + " | Restguthaben: EUR " + String.format("%.2f", bob.getAccount().getBalance()));

        // Carol lädt AC für 30 Min mit 8 kWh
        System.out.println("\nKunde: " + carol.getName() + " | Modus: AC | kWh: 8 | Min: 30");
        Location carolLocation = stationManager.getAllLocations().get(2); // Dritter Standort
        Charger carolCharger = carolLocation.getChargers().get(0);
        chargingService.startSession(carol.getClientId(), carolLocation.getLocationId(), carolCharger.getChargerId(), ChargerType.AC, LocalDateTime.of(2026, 1, 18, 15, 0));
        double carolCost = chargingService.finishSession(LocalDateTime.of(2026, 1, 18, 15, 30), 8.0);
        System.out.println("✓ Carol: EUR " + String.format("%.2f", carolCost) + " | Remaining balance: EUR " + String.format("%.2f", carol.getAccount().getBalance()));

        // 8) Summary: All customers and their billing
        System.out.println("\n--- CLIENT MANAGEMENT & BILLING SUMMARY ---");
        for (Client client : clientManager.getAllClients()) {
            List<InvoiceEntry> entries = billingManager.getEntriesForClient(client.getClientId());
            double spent = billingManager.getTotalSpentForClient(client.getClientId());
            System.out.println("Customer: " + client.getName() + " (ID " + client.getClientId() + ")");
            System.out.println("  Invoice entries: " + entries.size());
            System.out.println("  Total spent: EUR " + String.format("%.2f", spent));
            System.out.println("  Remaining balance: EUR " + String.format("%.2f", client.getAccount().getBalance()));
        }

        // 9) NEW FEATURE: Network status for customers and operators
        System.out.println("\n" + stationManager);

        // 10) NEW FEATURE: Invoice overview for customers
        System.out.println("\n" + billingManager.getDetailedInvoiceReport(1, clientManager));

        System.out.println("\n=== EXECUTION FINISHED ===");
    }

    private static PriceConfiguration defaultPricing(int id) {
        return new PriceConfiguration(id, 0.45, 0.65, 0.2, 0.2);
    }
}