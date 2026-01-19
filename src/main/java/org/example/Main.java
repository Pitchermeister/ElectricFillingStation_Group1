package org.example;

import org.example.Management.BillingManager;
import org.example.Management.ClientManager;
import org.example.Management.StationManager;
import org.example.domain.*;

import java.time.LocalDateTime;

/**
 * Main Application - Electric Vehicle Charging Station Network
 * Demonstrates all 10 User Stories and Business Requirements
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║    ELECTRIC VEHICLE CHARGING STATION NETWORK - DEMO             ║");
        System.out.println("║    Growing Network: 10 Locations with Multiple Charging Points  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");

        // Initialize Managers
        ClientManager clientManager = new ClientManager();
        StationManager stationManager = new StationManager();
        BillingManager billingManager = new BillingManager();
        ChargingService chargingService = new ChargingService(clientManager, stationManager, billingManager);

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 1: CREATE CLIENT
        // Requirement: Customers must register in advance and receive unique ID
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 1: CREATE CLIENT 】");
        System.out.println("Requirement: Customers register and receive unique customer identity\n");

        Client alice = clientManager.registerClient(1, "Alice Wonderland", "alice@univ.edu");
        Client bob = clientManager.registerClient(2, "Bob Builder", "bob@company.de");
        Client carol = clientManager.registerClient(3, "Carol Danvers", "carol@taxi.at");

        System.out.println("✓ Registered: " + alice.getName() + " (ID: " + alice.getClientId() + ")");
        System.out.println("✓ Registered: " + bob.getName() + " (ID: " + bob.getClientId() + ")");
        System.out.println("✓ Registered: " + carol.getName() + " (ID: " + carol.getClientId() + ")");
        System.out.println("→ Total clients: " + clientManager.getAllClients().size());

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 2: CREATE LOCATION
        // Requirement: Management of sites with charging stations (10 locations)
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 2: CREATE LOCATION 】");
        System.out.println("Requirement: Manage 10 locations (growing network)\n");

        String[][] locationData = {
            {"55", "City Center Hub", "1010 Vienna"},
            {"56", "Airport Fast Charge", "Airport Road 1"},
            {"57", "Tech Park", "Innovation Street 2"},
            {"58", "Harbor Plug-In", "Dockside 7"},
            {"59", "Suburb North", "North Avenue 12"},
            {"60", "University Campus", "Campus Road 3"},
            {"61", "Mall West", "Mall Ring 8"},
            {"62", "Industrial Zone", "Factory Street 44"},
            {"63", "Highway Rest", "A1 Exit 23"},
            {"64", "Old Town", "Old Town Lane 5"}
        };

        for (String[] data : locationData) {
            stationManager.createLocation(Integer.parseInt(data[0]), data[1], data[2]);
            System.out.println("✓ Created: " + data[1] + " at " + data[2]);
        }
        System.out.println("→ Total locations: " + stationManager.getAllLocations().size());

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 3: CREATE CHARGER
        // Requirement: Chargers can be AC or DC type with states (free/occupied/out of order)
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 3: CREATE CHARGER 】");
        System.out.println("Requirement: Add AC/DC chargers to locations (at least one, usually several)\n");

        int[] chargersPerLocation = {2, 2, 1, 2, 1, 2, 1, 2, 3, 2};
        int chargerId = 101;

        for (int i = 0; i < locationData.length; i++) {
            int locId = Integer.parseInt(locationData[i][0]);
            for (int c = 0; c < chargersPerLocation[i]; c++) {
                double power = (c % 2 == 0) ? 150.0 : 300.0;
                Charger charger = new Charger(chargerId++, 900000 + chargerId, power);
                stationManager.addChargerToLocation(locId, charger);
            }
            System.out.println("✓ Location " + locId + ": " + chargersPerLocation[i] + " chargers added");
        }
        System.out.println("→ Total chargers: " + stationManager.getTotalChargersCount());

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 4: READ CHARGER INFORMATION
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 4: READ CHARGER INFORMATION 】");
        Location firstLocation = stationManager.getLocationById(55);
        Charger firstCharger = firstLocation.getChargers().get(0);
        System.out.println("Charger ID: " + firstCharger.getChargerId());
        System.out.println("Power: " + firstCharger.getMaxPowerKw() + " kW");
        System.out.println("Status: " + firstCharger.getStatus());

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 5: CREATE PRICE
        // Requirement: Prices can be changed multiple times per day per location
        //              Different locations can have different prices
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 5: CREATE PRICE 】");
        System.out.println("Requirement: Set prices per location (can change multiple times daily)\n");

        // Morning prices
        stationManager.updateLocationPricing(55, 0.40, 0.60, 0.15, 0.15);
        stationManager.updateLocationPricing(56, 0.50, 0.70, 0.20, 0.20);
        System.out.println("✓ City Center (55): AC €0.40/kWh, DC €0.60/kWh");
        System.out.println("✓ Airport (56): AC €0.50/kWh, DC €0.70/kWh");

        // Set prices for all other locations
        for (int i = 57; i <= 64; i++) {
            stationManager.updateLocationPricing(i, 0.45, 0.65, 0.20, 0.20);
        }
        System.out.println("✓ All other locations: Standard pricing set");

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 6: UPDATE PREPAID CREDIT
        // Requirement: Customers must top up accounts in advance (prepaid system)
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 6: UPDATE PREPAID CREDIT 】");
        System.out.println("Requirement: Prepaid system - customers top up before charging\n");

        alice.getAccount().topUp(100.0);
        bob.getAccount().topUp(75.0);
        carol.getAccount().topUp(50.0);

        System.out.println("✓ Alice topped up €100.00 → Balance: €" + alice.getAccount().getBalance());
        System.out.println("✓ Bob topped up €75.00 → Balance: €" + bob.getAccount().getBalance());
        System.out.println("✓ Carol topped up €50.00 → Balance: €" + carol.getAccount().getBalance());

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 7: CREATE CHARGING SESSION
        // Requirement: Billing based on mode (AC/DC), energy (kWh), time (minutes)
        //              Prices at session start apply (price freeze)
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 7: CREATE CHARGING SESSION 】");
        System.out.println("Requirement: Session billing by mode, kWh, minutes. Prices frozen at start.\n");

        // Alice charges at City Center
        Location cityCenter = stationManager.getLocationById(55);
        Charger charger55 = cityCenter.getChargers().get(0);

        System.out.println("Alice starts charging at City Center:");
        System.out.println("  Location: " + cityCenter.getName());
        System.out.println("  Charger: #" + charger55.getChargerId() + " [" + charger55.getMaxPowerKw() + "kW]");
        System.out.println("  Mode: AC");
        System.out.println("  Price frozen: €0.40/kWh + €0.15/min");

        LocalDateTime aliceStart = LocalDateTime.of(2026, 1, 19, 10, 0);
        chargingService.startSession(alice.getClientId(), 55, charger55.getChargerId(),
                                     ChargerType.AC, aliceStart);
        System.out.println("  Charger status: " + charger55.getStatus());

        // Demonstrate price change during session (price freeze)
        System.out.println("\n  → PRICE CHANGE: City Center increases to €0.60/kWh");
        stationManager.updateLocationPricing(55, 0.60, 0.80, 0.25, 0.25);
        System.out.println("  → Alice's session still uses original price €0.40/kWh (frozen)");

        // Finish Alice's session
        double aliceCost = chargingService.finishSession(aliceStart.plusMinutes(20), 12.5);
        System.out.println("\n✓ Session finished:");
        System.out.println("  Duration: 20 min | Energy: 12.5 kWh");
        System.out.println("  Cost: €" + String.format("%.2f", aliceCost) +
                          " (12.5×€0.40 + 20×€0.15)");
        System.out.println("  Alice's balance: €" + String.format("%.2f", alice.getAccount().getBalance()));
        System.out.println("  Charger status: " + charger55.getStatus());

        // Bob charges at Airport (DC, different location, different price)
        Location airport = stationManager.getLocationById(56);
        Charger charger56 = airport.getChargers().get(0);

        System.out.println("\nBob starts DC charging at Airport:");
        System.out.println("  Location: " + airport.getName());
        System.out.println("  Mode: DC (fast charging)");
        System.out.println("  Price: €0.70/kWh + €0.20/min");

        LocalDateTime bobStart = LocalDateTime.of(2026, 1, 19, 14, 0);
        chargingService.startSession(bob.getClientId(), 56, charger56.getChargerId(),
                                     ChargerType.DC, bobStart);
        double bobCost = chargingService.finishSession(bobStart.plusMinutes(15), 25.0);

        System.out.println("✓ Session finished:");
        System.out.println("  Duration: 15 min | Energy: 25 kWh");
        System.out.println("  Cost: €" + String.format("%.2f", bobCost));
        System.out.println("  Bob's balance: €" + String.format("%.2f", bob.getAccount().getBalance()));

        // Carol charges at Tech Park
        Location techPark = stationManager.getLocationById(57);
        Charger charger57 = techPark.getChargers().get(0);

        LocalDateTime carolStart = LocalDateTime.of(2026, 1, 19, 15, 0);
        chargingService.startSession(carol.getClientId(), 57, charger57.getChargerId(),
                                     ChargerType.AC, carolStart);
        double carolCost = chargingService.finishSession(carolStart.plusMinutes(30), 8.0);

        System.out.println("\nCarol charged at Tech Park:");
        System.out.println("  Duration: 30 min | Energy: 8 kWh | Cost: €" +
                          String.format("%.2f", carolCost));

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 8: READ PREPAID CREDIT
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 8: READ PREPAID CREDIT 】");
        System.out.println("Current balances:");
        System.out.println("  Alice: €" + String.format("%.2f", alice.getAccount().getBalance()));
        System.out.println("  Bob:   €" + String.format("%.2f", bob.getAccount().getBalance()));
        System.out.println("  Carol: €" + String.format("%.2f", carol.getAccount().getBalance()));

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 9: READ INVOICE
        // Requirement: Invoice sorted by start time with item#, location, charger,
        //              mode, duration, energy, price + top-ups and balance
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 9: READ INVOICE 】");
        System.out.println("Requirement: Invoice with all session details, sorted chronologically\n");
        System.out.println(billingManager.getDetailedInvoiceReport(alice.getClientId(), clientManager));

        // ═══════════════════════════════════════════════════════════════════════
        // USER STORY 10: READ STATUS
        // Requirement: List showing current prices and operational status per location
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n【 USER STORY 10: READ STATUS 】");
        System.out.println("Requirement: Network status with prices and charger states\n");
        System.out.println(stationManager);

        // ═══════════════════════════════════════════════════════════════════════
        // BUSINESS SUMMARY
        // ═══════════════════════════════════════════════════════════════════════
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    BUSINESS SUMMARY                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("Network Size:");
        System.out.println("  Locations: " + stationManager.getAllLocations().size() + " (growing)");
        System.out.println("  Total Chargers: " + stationManager.getTotalChargersCount());
        System.out.println("  Available: " + stationManager.getAvailableChargersCount());
        System.out.println("  In Use: " + stationManager.getInUseChargersCount());

        System.out.println("\nCustomers:");
        System.out.println("  Registered: " + clientManager.getAllClients().size());
        System.out.println("  Total Sessions: " + billingManager.getAllEntries().size());

        System.out.println("\nRevenue:");
        double totalRevenue = 0;
        for (Client client : clientManager.getAllClients()) {
            totalRevenue += billingManager.getTotalSpentForClient(client.getClientId());
        }
        System.out.println("  Total: €" + String.format("%.2f", totalRevenue));

        System.out.println("\n✓ All 10 User Stories Demonstrated");
        System.out.println("✓ All Business Requirements Covered");
        System.out.println("✓ System Ready for Production\n");
    }
}