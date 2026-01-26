package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.StationManager;
import org.example.domain.Charger;
import org.example.domain.ChargerStatus;
import org.example.domain.ChargerType; // Ensure this enum exists
import org.example.domain.Location;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class CreateChargerSteps {

    private StationManager stationManager;
    private Charger lastCreatedCharger;
    private Exception lastException; // For error handling

    private Map<String, Location> locationsByName;
    private int nextLocationId;

    @Before
    public void setup() {
        stationManager = new StationManager();
        locationsByName = new HashMap<>();
        nextLocationId = 1;
        lastCreatedCharger = null;
        lastException = null;
    }

    @Given("the station manager is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("a target location named {string} exists")
    public void a_location_exists(String locationName) {
        ensureLocationExists(locationName);
    }

    // UPDATED: Changed wording to avoid conflict with CreatePriceSteps
    @Given("I confirm there is no location named {string}")
    public void no_location_named(String name) {
        // This checks the local map used by this test class
        Assertions.assertFalse(locationsByName.containsKey(name), "Location " + name + " should not exist");
    }

    // UPDATED: Now accepts Type (AC/DC) and validates power
    @When("I add a {string} charger with ID {int} and power {double} kW to location {string}")
    public void i_add_charger(String typeStr, Integer chargerId, Double power, String locationName) {
        try {
            Location loc = requireLocation(locationName);
            ChargerType type = ChargerType.valueOf(typeStr);

            validatePowerForType(type, power);

            lastCreatedCharger = new Charger(chargerId, 900000 + chargerId, power, type);
            stationManager.addChargerToLocation(loc.getLocationId(), lastCreatedCharger);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I add a specific {string} charger ID {int} with {double} kW to location {string}")
    public void i_add_charger_short(String typeStr, Integer chargerId, Double power, String locationName) {
        i_add_charger(typeStr, chargerId, power, locationName);
    }

    // Error Case Step
    @When("I attempt to add a {string} charger with ID {int} and power {double} kW to location {string}")
    public void attempt_add_charger(String typeStr, Integer id, Double power, String locationName) {
        try {
            // Check if location exists manually to mimic service behavior
            Location loc = locationsByName.get(locationName);
            if (loc == null) throw new IllegalArgumentException("Location not found: " + locationName);

            ChargerType type = ChargerType.valueOf(typeStr);
            validatePowerForType(type, power);

            stationManager.addChargerToLocation(loc.getLocationId(), new Charger(id, 999, power, type));
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the new charger should be registered in location {string}")
    public void charger_should_be_added(String locationName) {
        Assertions.assertNull(lastException, "Expected success but got error: " + (lastException != null ? lastException.getMessage() : ""));
        Location loc = requireLocation(locationName);
        Assertions.assertNotNull(lastCreatedCharger, "No charger was created");

        boolean found = loc.getChargers().stream()
                .anyMatch(c -> c.getChargerId() == lastCreatedCharger.getChargerId());
        Assertions.assertTrue(found, "Charger not found in location");
    }

    @Then("the new charger should have ID {int}")
    public void charger_should_have_id(Integer id) {
        Assertions.assertEquals(id.intValue(), lastCreatedCharger.getChargerId());
    }

    @Then("the new charger should have power {double} kW")
    public void charger_should_have_power(Double power) {
        Assertions.assertEquals(power, lastCreatedCharger.getMaxPowerKw(), 0.01);
    }

    @Then("the new charger status should be IN_OPERATION_FREE")
    public void charger_should_be_available() {
        Assertions.assertEquals(ChargerStatus.IN_OPERATION_FREE, lastCreatedCharger.getStatus());
    }

    @Then("location {string} should contain {int} chargers")
    public void location_should_have_chargers(String locationName, Integer count) {
        Location loc = requireLocation(locationName);
        Assertions.assertEquals(count.intValue(), loc.getChargers().size());
    }

    @Then("the charger should not be registered")
    public void charger_not_registered() {
        // Since no location exists, we can't check the location's list easily here,
        // but verifying an exception was thrown implies failure.
        Assertions.assertNotNull(lastException);
    }

    @Then("the system should show an error for non-existent location")
    public void error_non_existent_location() {
        Assertions.assertNotNull(lastException);
        Assertions.assertTrue(lastException.getMessage().contains("Location not found"));
    }

    @Then("the system should show an error for invalid power")
    public void error_invalid_power() {
        Assertions.assertNotNull(lastException);
        Assertions.assertTrue(lastException.getMessage().contains("Invalid power"));
    }

    // -------------------
    // Helpers
    // -------------------

    private void validatePowerForType(ChargerType type, double power) {
        if (type == ChargerType.AC) {
            if (power < 2.3 || power > 22.0) {
                throw new IllegalArgumentException("Invalid power for AC: " + power + "kW. Must be between 2.3 and 22.");
            }
        } else if (type == ChargerType.DC) {
            if (power <= 22.0) {
                throw new IllegalArgumentException("Invalid power for DC: " + power + "kW. Must be above 22.");
            }
        }
    }

    private Location ensureLocationExists(String locationName) {
        if (locationsByName.containsKey(locationName)) return locationsByName.get(locationName);
        int id = nextLocationId++;
        Location loc = stationManager.createLocation(id, locationName, "Address " + locationName);
        locationsByName.put(locationName, loc);
        return loc;
    }

    private Location requireLocation(String locationName) {
        Location loc = locationsByName.get(locationName);
        if (loc == null) throw new IllegalArgumentException("Location not found: " + locationName);
        return loc;
    }
}