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

import java.util.HashMap;
import java.util.Map;

public class CreateChargerSteps {

    private StationManager stationManager;
    private Charger lastCreatedCharger;

    // Map: location name -> Location (intern uses IDs, but feature doesn't)
    private Map<String, Location> locationsByName;
    private int nextLocationId;

    @Before
    public void setup() {
        stationManager = new StationManager();
        locationsByName = new HashMap<>();
        nextLocationId = 1;
        lastCreatedCharger = null;
    }

    @Given("the station manager is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("a target location named {string} exists")
    public void a_location_exists(String locationName) {
        ensureLocationExists(locationName);
    }

    @When("I add a charger with ID {int} and power {double} kW to location {string}")
    public void i_add_charger(Integer chargerId, Double power, String locationName) {
        Location loc = requireLocation(locationName);

        lastCreatedCharger = new Charger(chargerId, 900000 + chargerId, power);
        stationManager.addChargerToLocation(loc.getLocationId(), lastCreatedCharger);
    }

    @When("I add a specific charger ID {int} with {double} kW to location {string}")
    public void i_add_charger_short(Integer chargerId, Double power, String locationName) {
        Location loc = requireLocation(locationName);

        Charger charger = new Charger(chargerId, 900000 + chargerId, power);
        stationManager.addChargerToLocation(loc.getLocationId(), charger);
    }

    @Then("the new charger should be registered in location {string}")
    public void charger_should_be_added(String locationName) {
        Location loc = requireLocation(locationName);

        Assertions.assertNotNull(lastCreatedCharger, "No charger was created");
        boolean found = loc.getChargers().stream()
                .anyMatch(c -> c.getChargerId() == lastCreatedCharger.getChargerId());

        Assertions.assertTrue(found,
                "Expected charger " + lastCreatedCharger.getChargerId() + " to be in location '" + locationName + "'");
    }

    @Then("the new charger should have ID {int}")
    public void charger_should_have_id(Integer id) {
        Assertions.assertNotNull(lastCreatedCharger, "No charger was created");
        Assertions.assertEquals(id.intValue(), lastCreatedCharger.getChargerId());
    }

    @Then("the new charger should have power {double} kW")
    public void charger_should_have_power(Double power) {
        Assertions.assertNotNull(lastCreatedCharger, "No charger was created");
        Assertions.assertEquals(power, lastCreatedCharger.getMaxPowerKw(), 0.01);
    }

    @Then("the new charger status should be IN_OPERATION_FREE")
    public void charger_should_be_available() {
        Assertions.assertNotNull(lastCreatedCharger, "No charger was created");
        Assertions.assertEquals(ChargerStatus.IN_OPERATION_FREE, lastCreatedCharger.getStatus());
    }

    @Then("location {string} should contain {int} chargers")
    public void location_should_have_chargers(String locationName, Integer count) {
        Location loc = requireLocation(locationName);
        Assertions.assertEquals(count.intValue(), loc.getChargers().size(),
                "Unexpected charger count in location '" + locationName + "'");
    }

    // -------------------
    // Helpers
    // -------------------

    private Location ensureLocationExists(String locationName) {
        if (locationsByName.containsKey(locationName)) return locationsByName.get(locationName);

        int id = nextLocationId++;
        Location loc = stationManager.createLocation(id, locationName, "Address " + locationName);
        locationsByName.put(locationName, loc);
        return loc;
    }

    private Location requireLocation(String locationName) {
        Location loc = locationsByName.get(locationName);
        Assertions.assertNotNull(loc, "Unknown location: " + locationName);
        return loc;
    }
}
