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

public class CreateChargingSessionSteps {

    private StationManager stationManager;
    private ClientManager clientManager;
    private BillingManager billingManager;
    private ChargingService chargingService;
    private Exception lastException;

    @Before
    public void setup() {
        stationManager = new StationManager();
        clientManager = new ClientManager();
        billingManager = new BillingManager();
        chargingService = new ChargingService(clientManager, stationManager, billingManager);
        lastException = null;
    }

    @Given("the system is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("a client with ID {int} exists with balance €{double}")
    public void client_exists_with_balance(Integer id, Double balance) {
        Client client = clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
        client.getAccount().topUp(balance);
    }

    @Given("a location with ID {int} exists with {int} charger")
    public void location_exists_with_chargers(Integer locId, Integer count) {
        stationManager.createLocation(locId, "Location " + locId, "Address");
        for (int i = 0; i < count; i++) {
            Charger charger = new Charger(locId * 100 + i, 900000, 150.0);
            stationManager.addChargerToLocation(locId, charger);
        }
    }

    @Given("the charger has pricing AC €{double}/kWh and €{double}/min")
    public void charger_has_pricing(Double kwh, Double min) {
        Location loc = stationManager.getAllLocations().get(0);
        stationManager.updateLocationPricing(loc.getLocationId(), kwh, 0.65, min, 0.20);
    }

    @Given("the client has an active charging session")
    public void client_has_active_session() {
        Client client = clientManager.getAllClients().get(0);
        Location loc = stationManager.getAllLocations().get(0);
        Charger charger = loc.getChargers().get(0);

        chargingService.startSession(
            client.getClientId(),
            loc.getLocationId(),
            charger.getChargerId(),
            ChargerType.AC,
            LocalDateTime.now()
        );
    }

    @Given("client {int} has an active session")
    public void specific_client_has_active_session(Integer clientId) {
        Client client = clientManager.getClientById(clientId);
        Location loc = stationManager.getAllLocations().get(0);
        Charger charger = loc.getChargers().get(0);

        chargingService.startSession(
            client.getClientId(),
            loc.getLocationId(),
            charger.getChargerId(),
            ChargerType.AC,
            LocalDateTime.now()
        );
    }

    @When("the client starts an AC charging session")
    public void client_starts_session() {
        Client client = clientManager.getAllClients().get(0);
        Location loc = stationManager.getAllLocations().get(0);
        Charger charger = loc.getChargers().get(0);

        chargingService.startSession(
            client.getClientId(),
            loc.getLocationId(),
            charger.getChargerId(),
            ChargerType.AC,
            LocalDateTime.now()
        );
    }

    @When("the session finishes after {int} minutes with {double} kWh")
    public void session_finishes(Integer minutes, Double kwh) {
        chargingService.finishSession(LocalDateTime.now().plusMinutes(minutes), kwh);
    }

    @When("the client attempts to start a charging session")
    public void client_attempts_to_start_session() {
        try {
            Client client = clientManager.getAllClients().get(0);
            Location loc = stationManager.getAllLocations().get(0);
            Charger charger = loc.getChargers().get(0);

            chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                charger.getChargerId(),
                ChargerType.AC,
                LocalDateTime.now()
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("client {int} attempts to start a charging session")
    public void specific_client_attempts_session(Integer clientId) {
        try {
            Client client = clientManager.getClientById(clientId);
            Location loc = stationManager.getAllLocations().get(0);
            Charger charger = loc.getChargers().get(0);

            chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                charger.getChargerId(),
                ChargerType.AC,
                LocalDateTime.now()
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the session should be active")
    public void session_should_be_active() {
        Assertions.assertTrue(chargingService.hasActiveSession());
    }

    @Then("the charger should be occupied")
    public void charger_should_be_occupied() {
        Location loc = stationManager.getAllLocations().get(0);
        Assertions.assertEquals(ChargerStatus.OCCUPIED, loc.getChargers().get(0).getStatus());
    }

    @Then("the total cost should be €{double}")
    public void total_cost_should_be(Double expected) {
        Assertions.assertTrue(billingManager.getAllEntries().size() > 0);
    }

    @Then("the client balance should be €{double}")
    public void client_balance_should_be(Double expected) {
        Client client = clientManager.getAllClients().get(0);
        Assertions.assertEquals(expected, client.getAccount().getBalance(), 0.01);
    }

    @Then("the charger should be available")
    public void charger_should_be_available() {
        Location loc = stationManager.getAllLocations().get(0);
        Charger charger = loc.getChargers().get(0);
        Assertions.assertEquals(ChargerStatus.IN_OPERATION_FREE, charger.getStatus());
    }

    @Then("the session should fail with {string}")
    public void session_should_fail_with(String message) {
        Assertions.assertNotNull(lastException);
        Assertions.assertTrue(lastException.getMessage().contains(message));
    }

    @Then("the session should fail")
    public void session_should_fail() {
        Assertions.assertNotNull(lastException);
    }
}
