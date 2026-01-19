package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.StationManager;
import org.example.domain.Charger;
import org.example.domain.ChargerStatus;
import org.example.domain.Location;
import org.junit.jupiter.api.Assertions;

public class CreateChargerSteps {

    private StationManager stationManager;
    private Charger lastCreatedCharger;

    @Before
    public void setup() {
        stationManager = new StationManager();
    }

    // UPDATED: "the station manager is initialized"
    @Given("the station manager is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    // UPDATED: "a target location..."
    @Given("a target location with ID {int} exists")
    public void a_location_exists(Integer id) {
        // Ensure we create it cleanly
        stationManager.createLocation(id, "Location " + id, "Address " + id);
    }

    // UPDATED: "the new charger..."
    @Then("the new charger should be registered in the location")
    public void charger_should_be_added() {
        // Safe check using ID 1 directly as per scenario
        Location loc = stationManager.getLocationById(1);
        Assertions.assertNotNull(loc, "Location 1 not found");
        Assertions.assertFalse(loc.getChargers().isEmpty(), "Charger list is empty");
    }

    // UPDATED: "the new charger..."
    @Then("the new charger should have ID {int}")
    public void charger_should_have_id(Integer id) {
        Assertions.assertEquals(id.intValue(), lastCreatedCharger.getChargerId());
    }

    // UPDATED: "the new charger..."
    @Then("the new charger should have power {double} kW")
    public void charger_should_have_power(Double power) {
        Assertions.assertEquals(power, lastCreatedCharger.getMaxPowerKw(), 0.01);
    }

    // UPDATED: "the new charger status..."
    @Then("the new charger status should be IN_OPERATION_FREE")
    public void charger_should_be_available() {
        Assertions.assertEquals(ChargerStatus.IN_OPERATION_FREE, lastCreatedCharger.getStatus());
    }

    // UPDATED: "contain {int} chargers"
    @Then("location {int} should contain {int} chargers")
    public void location_should_have_chargers(Integer locId, Integer count) {
        Location loc = stationManager.getLocationById(locId);
        Assertions.assertEquals(count.intValue(), loc.getChargers().size());
    }

    // --- WHEN STEPS (Mapping the actions) ---

    @When("I add a charger with ID {int} and power {double} kW to location {int}")
    public void i_add_charger(Integer chargerId, Double power, Integer locationId) {
        lastCreatedCharger = new Charger(chargerId, 900000 + chargerId, power);
        stationManager.addChargerToLocation(locationId, lastCreatedCharger);
    }

    @When("I add a specific charger ID {int} with {double} kW to location {int}")
    public void i_add_charger_short(Integer chargerId, Double power, Integer locationId) {
        Charger charger = new Charger(chargerId, 900000, power);
        stationManager.addChargerToLocation(locationId, charger);
    }
}