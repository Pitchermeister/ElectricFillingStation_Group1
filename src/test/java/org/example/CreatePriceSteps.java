package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.StationManager;
import org.example.domain.Location;
import org.example.domain.PriceConfiguration;
import org.example.domain.Charger;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class CreatePriceSteps {

    private StationManager stationManager;

    // ✅ internal mapping: business name -> created location
    private Map<String, Location> locationsByName;
    private int nextLocationId;

    @Before
    public void setup() {
        stationManager = new StationManager();
        locationsByName = new HashMap<>();
        nextLocationId = 1;
    }

    @Given("the pricing service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("a pricing location named {string} exists with {int} charger")
    public void location_exists_with_chargers(String name, Integer count) {
        int locId = nextLocationId++; // ✅ technical detail hidden from feature
        Location loc = stationManager.createLocation(locId, name, "Address");
        locationsByName.put(name, loc);

        for (int i = 0; i < count; i++) {
            Charger charger = new Charger(locId * 100 + i, 900000, 150.0);
            stationManager.addChargerToLocation(locId, charger);
        }
    }

    @When("I set pricing for location {string}: AC {double} EUR per kWh, DC {double} EUR per kWh, {double} EUR per min")
    public void i_set_pricing(String name, Double ac, Double dc, Double min) {
        Location loc = requireLocation(name);
        stationManager.updateLocationPricing(loc.getLocationId(), ac, dc, min, min);
    }

    @When("I set location {string} pricing: AC {double} EUR per kWh")
    public void i_set_location_pricing(String name, Double ac) {
        Location loc = requireLocation(name);
        stationManager.updateLocationPricing(loc.getLocationId(), ac, 0.65, 0.20, 0.20);
    }

    @Then("location {string} should have AC price {double} EUR per kWh")
    public void location_should_have_ac_price(String name, Double price) {
        Location loc = requireLocation(name);
        PriceConfiguration pricing = loc.getPriceConfiguration();
        Assertions.assertNotNull(pricing, "Pricing not set for location: " + name);
        Assertions.assertEquals(price, pricing.getAcPricePerKWh(), 0.01);
    }

    @Then("location {string} should have DC price {double} EUR per kWh")
    public void location_should_have_dc_price(String name, Double price) {
        Location loc = requireLocation(name);
        PriceConfiguration pricing = loc.getPriceConfiguration();
        Assertions.assertNotNull(pricing, "Pricing not set for location: " + name);
        Assertions.assertEquals(price, pricing.getDcPricePerKWh(), 0.01);
    }

    @Then("location {string} pricing should apply to all chargers")
    public void pricing_should_apply_to_all_chargers(String name) {
        Location loc = requireLocation(name);

        // In the new model pricing lives on the location (single source of truth)
        Assertions.assertNotNull(loc.getPriceConfiguration(), "Pricing not set for location: " + name);

        // optional: ensure chargers exist if scenario says so
        Assertions.assertFalse(loc.getChargers().isEmpty(), "No chargers at location: " + name);
    }

    private Location requireLocation(String name) {
        Location loc = locationsByName.get(name);
        Assertions.assertNotNull(loc, "Unknown location name in scenario: " + name);
        return loc;
    }
}
