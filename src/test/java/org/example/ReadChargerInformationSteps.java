package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.StationManager;
import org.example.domain.Charger;
import org.example.domain.ChargerStatus;
import org.example.domain.ChargerType;
import org.example.domain.Location;
import org.example.domain.PriceConfiguration;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class ReadChargerInformationSteps {

    private StationManager stationManager;
    private Charger selectedCharger;
    private Charger lastCreatedCharger; // Track for "currently occupied" step

    private final Map<String, Integer> locationNameToId = new HashMap<>();
    private int nextLocationId;

    @Before
    public void setup() {
        stationManager = new StationManager();
        selectedCharger = null;
        lastCreatedCharger = null;

        locationNameToId.clear();
        nextLocationId = 1;
    }

    @Given("the charger info service is initialized")
    public void the_charger_info_service_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("an info-service location named {string} exists")
    public void an_info_service_location_named_exists(String locationName) {
        int locId = locationNameToId.computeIfAbsent(locationName, n -> nextLocationId++);
        stationManager.createLocation(locId, locationName, "Address " + locationName);
    }

    @Given("an info-service charger with ID {int} and {double} kW exists at location {string}")
    public void an_info_service_charger_with_id_and_power_exists_at_location(Integer chargerId, Double power, String locationName) {
        int locId = requireLocationId(locationName);

        // Determine Mode based on Power (Standard logic: >22kW is DC)
        ChargerType type = (power > 22.0) ? ChargerType.DC : ChargerType.AC;

        Charger charger = new Charger(chargerId, 900000 + chargerId, power, type);
        stationManager.addChargerToLocation(locId, charger);
        lastCreatedCharger = charger;
    }

    @Given("an info-service charger with ID {int} exists at location {string}")
    public void an_info_service_charger_with_id_exists_at_location(Integer chargerId, String locationName) {
        int locId = requireLocationId(locationName);

        // Default to AC 22kW so the "View charger with pricing" scenario sees AC Mode
        double power = 22.0;
        ChargerType type = ChargerType.AC;

        Charger charger = new Charger(chargerId, 900000 + chargerId, power, type);
        stationManager.addChargerToLocation(locId, charger);
        lastCreatedCharger = charger;
    }

    @Given("info-service location {string} has pricing AC {double} EUR per kWh")
    public void info_service_location_has_pricing_ac(String locationName, Double acPrice) {
        int locId = requireLocationId(locationName);
        stationManager.updateLocationPricing(locId, acPrice, 0.65, 0.20, 0.20);
    }

    @When("I request information for charger ID {int}")
    public void i_request_information_for_charger_id(Integer chargerId) {
        selectedCharger = stationManager.findChargerById(chargerId);
    }

    @When("I request charger information for ID {int}")
    public void i_request_charger_information_for_id(Integer chargerId) {
        selectedCharger = stationManager.findChargerById(chargerId);
    }

    @Then("I should see charger ID {int}")
    public void i_should_see_charger_id(Integer expectedId) {
        Assertions.assertNotNull(selectedCharger, "No charger selected/found");
        Assertions.assertEquals(expectedId.intValue(), selectedCharger.getChargerId());
    }

    @Then("I should see power {double} kW")
    public void i_should_see_power(Double expectedPower) {
        Assertions.assertNotNull(selectedCharger, "No charger selected/found");
        Assertions.assertEquals(expectedPower, selectedCharger.getMaxPowerKw(), 0.01);
    }

    @Then("I should see charging mode {string}")
    public void i_should_see_charging_mode(String expectedMode) {
        Assertions.assertNotNull(selectedCharger, "No charger selected/found");
        Assertions.assertEquals(expectedMode, selectedCharger.getType().toString());
    }

    @Then("I should see the charger status")
    public void i_should_see_the_charger_status() {
        Assertions.assertNotNull(selectedCharger, "No charger selected/found");
        Assertions.assertNotNull(selectedCharger.getStatus(), "Charger status must not be null");
    }

    @Then("I should see AC price {double} EUR per kWh")
    public void i_should_see_ac_price(Double expectedPrice) {
        Assertions.assertNotNull(selectedCharger, "No charger selected/found");

        // FIX: Use StationManager to get the current pricing configuration,
        // as the Location object inside the Charger might be stale or not fully linked.
        PriceConfiguration pricing = stationManager.getPricingForLocation(selectedCharger.getLocationId());

        Assertions.assertNotNull(pricing, "No pricing set for locationId=" + selectedCharger.getLocationId());
        Assertions.assertEquals(expectedPrice, pricing.getAcPricePerKWh(), 0.01);
    }

    // --- MISSING STEPS IMPLEMENTED ---

    @Then("an error should be returned for charger not found")
    public void an_error_should_be_returned_for_charger_not_found() {
        // If findChargerById returns null, selectedCharger is null.
        Assertions.assertNull(selectedCharger, "Expected no charger to be found, but one was selected");
    }

    @Given("the charger is currently occupied by a charging session")
    public void the_charger_is_currently_occupied() {
        Assertions.assertNotNull(lastCreatedCharger, "No charger created to set occupied");
        lastCreatedCharger.setStatus(ChargerStatus.OCCUPIED);
    }

    @Then("I should see the charger status as OCCUPIED")
    public void i_should_see_status_as_occupied() {
        Assertions.assertNotNull(selectedCharger);
        Assertions.assertEquals(ChargerStatus.OCCUPIED, selectedCharger.getStatus());
    }

    @Given("location {string} has no pricing configured")
    public void location_has_no_pricing(String locationName) {
        int locId = requireLocationId(locationName);
        Location loc = stationManager.getLocationById(locId);
        loc.setPriceConfiguration(null); // Force clear
    }

    @Then("the pricing information should indicate {string}")
    public void pricing_should_indicate(String message) {
        Assertions.assertNotNull(selectedCharger);
        PriceConfiguration pricing = stationManager.getPricingForLocation(selectedCharger.getLocationId());

        if ("Not configured".equals(message)) {
            Assertions.assertNull(pricing, "Expected pricing to be null/not configured");
        }
    }

    // -----------------------
    // helpers
    // -----------------------
    private int requireLocationId(String locationName) {
        Integer id = locationNameToId.get(locationName);
        Assertions.assertNotNull(id, "Location not known in scenario: " + locationName);
        return id;
    }
}