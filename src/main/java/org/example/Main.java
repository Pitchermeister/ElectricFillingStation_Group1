package org.example;

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
        PriceConfiguration prices = new PriceConfiguration(1, 0.45, 0.65);
        fastCharger.setPriceConfiguration(prices);

        // 4. Link Charger to Location
        stationManager.addChargerToLocation(55, fastCharger);

        // 5. Verify logic by retrieving it
        // (We manually check the list here to prove it works)
        Charger retreivedCharger = cityCenter.getChargers().get(0);
        System.out.println("Charger [" + retreivedCharger.getChargerId() + "] is active.");
        System.out.println("DC Price is: EUR " + retreivedCharger.getPriceConfiguration().getDcPricePerKWh());

        System.out.println("\n=== EXECUTION FINISHED ===");
    }
}