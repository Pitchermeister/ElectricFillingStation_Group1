package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.StationManager;
import org.example.domain.Charger;
import org.example.domain.Location;
import org.example.domain.PriceConfiguration;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class ReadChargerInformationSteps {

    private StationManager stationManager;
    private Charger selectedCharger;

    // map location names -> internal ids (hidden from feature)
    private final Map<String, Integer> locationNameToId = new HashMap<>();
    private int nextLocationId;

    @Before
    public void setup() {
        stationManager = new StationManager();
        selectedCharger = null;

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
        // createLocation may throw if duplicate in your implementation; but computeIfAbsent avoids re-creating
        stationManager.createLocation(locId, locationName, "Address " + locationName);
    }

    @Given("an info-service charger with ID {int} and {double} kW exists at location {string}")
    public void an_info_service_charger_with_id_and_power_exists_at_location(Integer chargerId, Double power, String locationName) {
        int locId = requireLocationId(locationName);

        Charger charger = new Charger(chargerId, 900000 + chargerId, power);
        stationManager.addChargerToLocation(locId, charger);
    }

    @Given("an info-service charger with ID {int} exists at location {string}")
    public void an_info_service_charger_with_id_exists_at_location(Integer chargerId, String locationName) {
        int locId = requireLocationId(locationName);

        Charger charger = new Charger(chargerId, 900000 + chargerId, 150.0);
        stationManager.addChargerToLocation(locId, charger);
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

    @Then("I should see the charger status")
    public void i_should_see_the_charger_status() {
        Assertions.assertNotNull(selectedCharger, "No charger selected/found");
        Assertions.assertNotNull(selectedCharger.getStatus(), "Charger status must not be null");
    }

    @Then("I should see AC price {double} EUR per kWh")
    public void i_should_see_ac_price(Double expectedPrice) {
        Assertions.assertNotNull(selectedCharger, "No charger selected/found");

        int locId = selectedCharger.getLocationId();
        Location loc = stationManager.getLocationById(locId);
        Assertions.assertNotNull(loc, "Location not found for charger locationId=" + locId);

        PriceConfiguration pricing = loc.getPriceConfiguration();
        Assertions.assertNotNull(pricing, "No pricing set for locationId=" + locId);

        Assertions.assertEquals(expectedPrice, pricing.getAcPricePerKWh(), 0.01);
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
