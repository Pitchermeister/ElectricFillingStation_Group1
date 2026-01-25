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

import java.time.LocalDateTime;
import java.util.Locale;

public class ReadStatusSteps {

    private StationManager stationManager;
    private ClientManager clientManager;
    private BillingManager billingManager;
    private ChargingService chargingService;
    private String networkStatus;

    @Before
    public void setup() {
        stationManager = new StationManager();
        clientManager = new ClientManager();
        billingManager = new BillingManager();
        chargingService = new ChargingService(clientManager, stationManager, billingManager);
    }

    @Given("the status monitoring system is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("a status monitored location with ID {int} {string} exists with {int} chargers")
    public void location_with_name_and_chargers(Integer id, String name, Integer count) {
        Location loc = stationManager.createLocation(id, name, "Address");

        // ✅ pricing on location
        loc.setPriceConfiguration(new PriceConfiguration(id, 0.45, 0.65, 0.20, 0.20));

        for (int i = 0; i < count; i++) {
            Charger charger = new Charger(id * 100 + i, 900000, 150.0);
            stationManager.addChargerToLocation(id, charger);
        }
    }

    @Given("a status monitored location with ID {int} exists with {int} chargers")
    public void location_exists_with_chargers(Integer id, Integer count) {
        Location loc = stationManager.createLocation(id, "Location " + id, "Address");

        // ✅ pricing on location
        loc.setPriceConfiguration(new PriceConfiguration(id, 0.45, 0.65, 0.20, 0.20));

        for (int i = 0; i < count; i++) {
            Charger charger = new Charger(id * 100 + i, 900000, 150.0);
            stationManager.addChargerToLocation(id, charger);
        }
    }


    @Given("status location {int} has pricing AC {double} EUR per kWh")
    public void location_has_pricing(Integer locId, Double ac) {
        stationManager.updateLocationPricing(locId, ac, 0.65, 0.20, 0.20);
    }

    @Given("a monitoring client with ID {int} exists with balance {double} EUR")
    public void client_exists_with_balance(Integer id, Double balance) {
        Client client = clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
        client.getAccount().topUp(balance);
    }

    @Given("the client is charging on charger {int}")
    public void client_is_charging_on_charger(Integer chargerId) {
        Client client = clientManager.getAllClients().get(0);

        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertNotNull(charger, "Charger not found: " + chargerId);

        // Find the location of that charger (über charger.getLocationId())
        Location loc = stationManager.getLocationById(charger.getLocationId());
        Assertions.assertNotNull(loc, "Location not found for charger: " + chargerId);

        // ✅ ensure location pricing exists
        if (loc.getPriceConfiguration() == null) {
            loc.setPriceConfiguration(new PriceConfiguration(loc.getLocationId(), 0.45, 0.65, 0.20, 0.20));
        }

        chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                chargerId,
                ChargerType.AC,
                LocalDateTime.now()
        );
    }


    @When("I request the network status")
    public void i_request_network_status() {
        networkStatus = stationManager.toString();
    }

    @Then("the status should show location {string}")
    public void status_should_show_location(String name) {
        Assertions.assertTrue(networkStatus.contains(name));
    }

    @Then("the status should show AC price {double}")
    public void status_should_show_ac_price(Double price) {
        // FIX: Enforce US formatting (dots) to match StationManager output "0.45"
        // preventing "0,45" failure on European computers.
        String expectedPrice = String.format(Locale.US, "%.2f", price); // "0.45"

        Assertions.assertTrue(networkStatus.contains(expectedPrice),
                "Status did not contain price: " + expectedPrice + "\nActual status: " + networkStatus);
    }

    @Then("the status should show {int} chargers")
    public void status_should_show_chargers(Integer count) {
        Assertions.assertEquals(count.intValue(), stationManager.getTotalChargersCount());
    }

    @Then("the status should show charger availability")
    public void status_should_show_availability() {
        Assertions.assertTrue(networkStatus.contains("available") || networkStatus.contains("AVAILABLE"));
    }

    @Then("charger {int} should show status OCCUPIED")
    public void charger_should_show_occupied(Integer chargerId) {
        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertEquals(ChargerStatus.OCCUPIED, charger.getStatus());
    }

    @Then("charger {int} should show status AVAILABLE")
    public void charger_should_show_available(Integer chargerId) {
        Charger charger = stationManager.findChargerById(chargerId);
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
}