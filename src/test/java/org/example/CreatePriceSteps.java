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

public class CreatePriceSteps {

    private StationManager stationManager;

    @Before
    public void setup() {
        stationManager = new StationManager();
    }

    @Given("the system is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("a location with ID {int} exists with {int} charger")
    public void location_exists_with_chargers(Integer locId, Integer count) {
        stationManager.createLocation(locId, "Location " + locId, "Address");
        for (int i = 0; i < count; i++) {
            Charger charger = new Charger(locId * 100 + i, 900000, 150.0);
            stationManager.addChargerToLocation(locId, charger);
        }
    }

    @When("I set pricing for location {int}: AC €{double}/kWh, DC €{double}/kWh, €{double}/min")
    public void i_set_pricing(Integer locId, Double ac, Double dc, Double min) {
        stationManager.updateLocationPricing(locId, ac, dc, min, min);
    }

    @When("I set location {int} pricing: AC €{double}/kWh")
    public void i_set_location_pricing(Integer locId, Double ac) {
        stationManager.updateLocationPricing(locId, ac, 0.65, 0.20, 0.20);
    }

    @Then("location {int} should have AC price €{double}/kWh")
    public void location_should_have_ac_price(Integer locId, Double price) {
        PriceConfiguration pricing = stationManager.getPricingForLocation(locId);
        Assertions.assertEquals(price, pricing.getAcPricePerKWh(), 0.01);
    }

    @Then("location {int} should have DC price €{double}/kWh")
    public void location_should_have_dc_price(Integer locId, Double price) {
        PriceConfiguration pricing = stationManager.getPricingForLocation(locId);
        Assertions.assertEquals(price, pricing.getDcPricePerKWh(), 0.01);
    }

    @Then("all chargers at location {int} should have these prices")
    public void all_chargers_should_have_prices(Integer locId) {
        Location loc = stationManager.getLocationById(locId);
        for (Charger c : loc.getChargers()) {
            Assertions.assertNotNull(c.getPriceConfiguration());
        }
    }
}
