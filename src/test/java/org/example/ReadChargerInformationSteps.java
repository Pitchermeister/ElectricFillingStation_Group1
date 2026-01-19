package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.StationManager;
import org.example.domain.Charger;
import org.example.domain.PriceConfiguration;
import org.junit.jupiter.api.Assertions;

public class ReadChargerInformationSteps {

    private StationManager stationManager;
    private Charger lastCreatedCharger;

    @Before
    public void setup() {
        stationManager = new StationManager();
    }

    // UPDATED: Unique wording
    @Given("the charger info service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    // UPDATED: Unique wording
    @Given("an info-service location with ID {int} exists")
    public void a_location_exists(Integer id) {
        stationManager.createLocation(id, "Location " + id, "Address " + id);
    }

    // UPDATED: Unique wording
    @Given("an info-service charger with ID {int} and {double} kW exists at location {int}")
    public void charger_exists_at_location(Integer chargerId, Double power, Integer locId) {
        Charger charger = new Charger(chargerId, 900000, power);
        stationManager.addChargerToLocation(locId, charger);
    }

    // UPDATED: Unique wording
    @Given("an info-service charger with ID {int} exists at location {int}")
    public void charger_exists(Integer chargerId, Integer locId) {
        Charger charger = new Charger(chargerId, 900000, 150.0);
        stationManager.addChargerToLocation(locId, charger);
    }

    // UPDATED: Unique wording + removed symbols
    @Given("info-service location {int} has pricing AC {double} EUR per kWh")
    public void location_has_pricing(Integer locId, Double ac) {
        stationManager.updateLocationPricing(locId, ac, 0.65, 0.20, 0.20);
    }

    @When("I request information for charger ID {int}")
    public void i_request_charger_info(Integer chargerId) {
        lastCreatedCharger = stationManager.findChargerById(chargerId);
    }

    @When("I request charger information for ID {int}")
    public void i_request_charger_information(Integer chargerId) {
        lastCreatedCharger = stationManager.findChargerById(chargerId);
    }

    @Then("I should see charger ID {int}")
    public void i_should_see_charger_id(Integer id) {
        Assertions.assertNotNull(lastCreatedCharger);
        Assertions.assertEquals(id.intValue(), lastCreatedCharger.getChargerId());
    }

    @Then("I should see power {double} kW")
    public void i_should_see_power(Double power) {
        Assertions.assertEquals(power, lastCreatedCharger.getMaxPowerKw(), 0.01);
    }

    @Then("I should see the charger status")
    public void i_should_see_status() {
        Assertions.assertNotNull(lastCreatedCharger.getStatus());
    }

    // UPDATED: Removed symbols
    @Then("I should see AC price {double} EUR per kWh")
    public void i_should_see_ac_price(Double price) {
        PriceConfiguration pricing = lastCreatedCharger.getPriceConfiguration();
        Assertions.assertNotNull(pricing);
        Assertions.assertEquals(price, pricing.getAcPricePerKWh(), 0.01);
    }
}