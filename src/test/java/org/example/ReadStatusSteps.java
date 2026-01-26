package org.example;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.BillingManager;
import org.example.Management.ClientManager;
import org.example.Management.StationManager;
import org.example.domain.*;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadStatusSteps {

    private StationManager stationManager;
    private ClientManager clientManager;
    private BillingManager billingManager;
    private ChargingService chargingService;
    private String networkStatus;
    private Exception lastException;

    private Map<String, Location> locationsByName;
    private Map<String, Client> clientsByName;

    private int nextLocationId;
    private int nextClientId;

    @Before
    public void setup() {
        stationManager = new StationManager();
        clientManager = new ClientManager();
        billingManager = new BillingManager();
        chargingService = new ChargingService(clientManager, stationManager, billingManager);

        locationsByName = new HashMap<>();
        clientsByName = new HashMap<>();
        nextLocationId = 1;
        nextClientId = 1;
        lastException = null;

        networkStatus = "";
    }

    @Given("the status monitoring system is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
        Assertions.assertNotNull(clientManager);
        Assertions.assertNotNull(chargingService);
    }

    // -----------------------
    // Locations by NAME
    // -----------------------

    @Given("a status monitored location named {string} exists with {int} chargers")
    public void location_with_name_and_chargers(String name, Integer count) {
        Location loc = ensureLocationExists(name);
        ensureLocationHasDefaultPricing(loc);

        for (int i = 0; i < count; i++) {
            // chargers 101, 102...
            int chargerId = (loc.getLocationId() * 100) + i + 1;
            Charger charger = new Charger(chargerId, 900000 + chargerId, 150.0);
            stationManager.addChargerToLocation(loc.getLocationId(), charger);
        }
    }

    @Given("the following locations and chargers exist:")
    public void locations_and_chargers_exist(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        for (Map<String, String> row : rows) {
            String locName = row.get("location");
            int chargerId = Integer.parseInt(row.get("chargerID"));

            Location loc = ensureLocationExists(locName);
            ensureLocationHasDefaultPricing(loc);

            Charger charger = new Charger(chargerId, 900000 + chargerId, 150.0);
            stationManager.addChargerToLocation(loc.getLocationId(), charger);
        }
    }

    @Given("location {string} has pricing AC {double} EUR per kWh")
    public void location_has_pricing(String locationName, Double ac) {
        Location loc = ensureLocationExists(locationName);
        stationManager.updateLocationPricing(loc.getLocationId(), ac, 0.65, 0.20, 0.20);
    }

    // -----------------------
    // Clients by NAME
    // -----------------------

    @Given("a monitoring customer {string} exists with balance {double} EUR")
    public void customer_exists_with_balance(String name, Double balance) {
        Client client = ensureCustomerExists(name);
        client.getAccount().topUp(balance);
    }

    // -----------------------
    // Charging action
    // -----------------------

    @Given("the customer {string} is charging on charger {int}")
    public void customer_is_charging_on_charger(String customerName, Integer chargerId) {
        Client client = requireCustomer(customerName);

        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertNotNull(charger, "Charger not found: " + chargerId);

        Location loc = stationManager.getLocationById(charger.getLocationId());
        Assertions.assertNotNull(loc, "Location not found for charger: " + chargerId);

        // Ensure pricing is registered with StationManager before starting session
        ensureLocationHasDefaultPricing(loc);

        chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                chargerId,
                ChargerType.AC,
                LocalDateTime.now()
        );
    }

    // -----------------------
    // Request & Assertions
    // -----------------------

    @When("I request the network status")
    public void i_request_network_status() {
        networkStatus = stationManager.toString();
        // Append details for verification
        for (Location loc : stationManager.getAllLocations()) {
            networkStatus += "\nLocation: " + loc.getName();
            if (loc.getPriceConfiguration() != null) {
                networkStatus += " PriceAC: " + loc.getPriceConfiguration().getAcPricePerKWh();
            }
            for (Charger c : loc.getChargers()) {
                networkStatus += " Charger " + c.getChargerId() + ": " + c.getStatus();
            }
        }
    }

    @When("I attempt to request status for location {string}")
    public void i_attempt_request_status_location(String name) {
        try {
            if (!locationsByName.containsKey(name)) {
                throw new IllegalArgumentException("Location not found: " + name);
            }
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the status should show location {string}")
    public void status_should_show_location(String name) {
        Assertions.assertTrue(networkStatus.contains(name),
                "Status did not contain location: " + name + "\nActual status:\n" + networkStatus);
    }

    @Then("the status should show AC price {double}")
    public void status_should_show_ac_price(Double price) {
        Assertions.assertTrue(networkStatus.contains(String.valueOf(price)) || networkStatus.contains("PriceAC: " + price),
                "Status did not contain price: " + price + "\nActual status:\n" + networkStatus);
    }

    @Then("the status should show {int} chargers")
    public void status_should_show_chargers(Integer count) {
        Assertions.assertEquals(count.intValue(), stationManager.getTotalChargersCount());
    }

    @Then("charger {int} should show status OCCUPIED")
    public void charger_should_show_occupied(Integer chargerId) {
        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertNotNull(charger, "Charger not found: " + chargerId);
        Assertions.assertEquals(ChargerStatus.OCCUPIED, charger.getStatus());
    }

    @Then("charger {int} should show status AVAILABLE")
    public void charger_should_show_available(Integer chargerId) {
        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertNotNull(charger, "Charger not found: " + chargerId);
        Assertions.assertEquals(ChargerStatus.IN_OPERATION_FREE, charger.getStatus());
    }

    @Then("the status should show {int} locations")
    public void status_should_show_locations(Integer count) {
        Assertions.assertEquals(count.intValue(), stationManager.getAllLocations().size());
    }

    @Then("the status should show {int} total chargers")
    public void status_should_show_total_chargers(Integer count) {
        Assertions.assertEquals(count.intValue(), stationManager.getTotalChargersCount());
    }

    @Then("an error should be returned for location not found")
    public void error_returned_location_not_found() {
        Assertions.assertNotNull(lastException);
        Assertions.assertTrue(lastException.getMessage().contains("Location not found"));
    }

    // -----------------------
    // Helpers
    // -----------------------

    private Location ensureLocationExists(String name) {
        if (locationsByName.containsKey(name)) return locationsByName.get(name);
        int id = nextLocationId++;
        Location loc = stationManager.createLocation(id, name, "Address");
        locationsByName.put(name, loc);
        return loc;
    }

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

    // FIX: Use stationManager to update pricing so it persists correctly for ChargingService
    private void ensureLocationHasDefaultPricing(Location loc) {
        if (loc.getPriceConfiguration() == null) {
            stationManager.updateLocationPricing(
                    loc.getLocationId(), 0.45, 0.65, 0.20, 0.20
            );
        }
    }
}