package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.BillingManager;
import org.example.Management.ClientManager;
import org.example.Management.StationManager;
import org.example.domain.*;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateChargingSessionSteps {

    private StationManager stationManager;
    private ClientManager clientManager;
    private BillingManager billingManager;
    private ChargingService chargingService;

    private Exception lastException;

    private Map<String, Client> clientsByName;
    private Map<String, Location> locationsByName;

    private int nextClientId;
    private int nextLocationId;

    @Before
    public void setup() {
        stationManager = new StationManager();
        clientManager = new ClientManager();
        billingManager = new BillingManager();
        chargingService = new ChargingService(clientManager, stationManager, billingManager);

        lastException = null;

        clientsByName = new HashMap<>();
        locationsByName = new HashMap<>();
        nextClientId = 1;
        nextLocationId = 1;
    }

    @Given("the charging service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
        Assertions.assertNotNull(clientManager);
        Assertions.assertNotNull(chargingService);
    }

    // -----------------------------
    // Customer steps (no IDs)
    // -----------------------------

    @Given("a charging customer {string} exists with balance {double} EUR")
    public void charging_customer_exists_with_balance(String name, Double balance) {
        Client client = ensureCustomerExists(name);
        client.getAccount().topUp(balance);
    }

    // -----------------------------
    // Location steps (no IDs)
    // -----------------------------

    // NOTE: changed wording to avoid duplicate step definition clash with ReadInvoiceSteps
    @Given("a charging location named {string} exists with {int} chargers")
    public void charging_location_named_exists_with_chargers(String locationName, Integer count) {
        Location loc = ensureLocationExists(locationName);

        // deterministic charger ids: 101, 102, ...
        for (int i = 0; i < count; i++) {
            int chargerId = 101 + i;
            Charger charger = new Charger(chargerId, 900000 + chargerId, 150.0);

            // Defensive: ensure the charger really has the expected ID (constructor order / internal fields may differ)
            forceIntFieldIfPresent(charger, "chargerId", chargerId);
            forceIntFieldIfPresent(charger, "id", chargerId);

            stationManager.addChargerToLocation(loc.getLocationId(), charger);
        }
    }

    @Given("the charger with ID {int} has pricing AC {double} EUR per kWh and {double} EUR per min")
    public void charger_has_pricing(Integer chargerId, Double acPrice, Double minPrice) {
        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertNotNull(charger, "Charger not found: " + chargerId);

        Location loc = stationManager.getLocationById(charger.getLocationId());
        Assertions.assertNotNull(loc, "Location not found for charger: " + chargerId);

        stationManager.updateLocationPricing(loc.getLocationId(), acPrice, 0.65, minPrice, 0.20);
    }

    // -----------------------------
    // Start / Active session
    // -----------------------------

    @When("the charging customer {string} starts an AC charging session on charger {int} at {string}")
    public void customer_starts_ac_session(String customerName, Integer chargerId, String locationName) {
        lastException = null;

        Client client = requireCustomer(customerName);
        Location loc = requireLocation(locationName);

        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertNotNull(charger, "Charger not found: " + chargerId);

        chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                chargerId,
                ChargerType.AC,
                LocalDateTime.now()
        );
    }

    @Given("the charging customer {string} has an active session on charger {int} at {string}")
    public void customer_has_active_session(String customerName, Integer chargerId, String locationName) {
        customer_starts_ac_session(customerName, chargerId, locationName);
    }

    @When("the charging customer {string} attempts to start a charging session on charger {int} at {string}")
    public void customer_attempts_to_start_session(String customerName, Integer chargerId, String locationName) {
        try {
            customer_starts_ac_session(customerName, chargerId, locationName);
        } catch (Exception e) {
            lastException = e;
        }
    }

    // -----------------------------
    // Finish session
    // -----------------------------

    @When("the session finishes after {int} minutes with {double} kWh")
    public void session_finishes(Integer minutes, Double kwh) {
        chargingService.finishSession(LocalDateTime.now().plusMinutes(minutes), kwh);
    }

    // -----------------------------
    // Assertions
    // -----------------------------

    @Then("the charging session should be active")
    public void session_should_be_active() {
        Assertions.assertTrue(chargingService.hasActiveSession(), "Expected an active session, but none is active.");
    }

    @Then("charger {int} should be OCCUPIED")
    public void charger_should_be_occupied(Integer chargerId) {
        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertNotNull(charger, "Charger not found: " + chargerId);
        Assertions.assertEquals(ChargerStatus.OCCUPIED, charger.getStatus());
    }

    @Then("charger {int} should be AVAILABLE")
    public void charger_should_be_available(Integer chargerId) {
        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertNotNull(charger, "Charger not found: " + chargerId);
        Assertions.assertEquals(ChargerStatus.IN_OPERATION_FREE, charger.getStatus());
    }

    @Then("the total session cost should be {double} EUR")
    public void total_cost_should_be(Double expected) {
        List<InvoiceEntry> entries = billingManager.getAllEntries();
        Assertions.assertFalse(entries.isEmpty(), "No invoice entries found!");

        double actualCost = entries.get(entries.size() - 1).getPrice();
        Assertions.assertEquals(expected, actualCost, 0.01, "Cost calculation incorrect");
    }

    @Then("the charging customer {string} balance should be {double} EUR")
    public void customer_balance_should_be(String customerName, Double expected) {
        Client client = requireCustomer(customerName);
        Assertions.assertEquals(expected, client.getAccount().getBalance(), 0.01);
    }

    @Then("the session should fail with error {string}")
    public void session_should_fail_with(String message) {
        Assertions.assertNotNull(lastException, "Expected an exception but none was thrown.");
        Assertions.assertTrue(lastException.getMessage().contains(message),
                "Expected error message to contain '" + message + "' but got '" + lastException.getMessage() + "'");
    }

    @Then("the session start should fail")
    public void session_start_should_fail() {
        Assertions.assertNotNull(lastException, "Expected session start to fail, but it succeeded.");
    }

    // -----------------------------
    // Helpers
    // -----------------------------

    private Client ensureCustomerExists(String name) {
        if (clientsByName.containsKey(name)) return clientsByName.get(name);

        int id = nextClientId++;
        String email = name.toLowerCase().replace(" ", ".") + "@test.com";
        Client client = clientManager.registerClient(id, name, email);

        clientsByName.put(name, client);
        return client;
    }

    private Client requireCustomer(String name) {
        Client client = clientsByName.get(name);
        Assertions.assertNotNull(client, "Unknown customer: " + name);
        return client;
    }

    private Location ensureLocationExists(String name) {
        if (locationsByName.containsKey(name)) return locationsByName.get(name);

        int id = nextLocationId++;
        Location loc = stationManager.createLocation(id, name, "Address " + name);
        locationsByName.put(name, loc);
        return loc;
    }

    private Location requireLocation(String name) {
        Location loc = locationsByName.get(name);
        Assertions.assertNotNull(loc, "Unknown location: " + name);
        return loc;
    }

    private static void forceIntFieldIfPresent(Object target, String fieldName, int value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            if (f.getType() == int.class || f.getType() == Integer.class) {
                f.set(target, value);
            }
        } catch (NoSuchFieldException ignored) {
            // field not present in this implementation
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field '" + fieldName + "' via reflection", e);
        }
    }
}
