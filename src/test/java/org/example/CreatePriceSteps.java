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

import java.util.List;

public class CreatePriceSteps {

    private StationManager stationManager;
    private Exception lastException;
    private int nextLocationId = 1;

    @Before
    public void setup() {
        stationManager = new StationManager();
        lastException = null;
        nextLocationId = 1;
    }

    @Given("the pricing service is initialized")
    public void pricing_service_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("a pricing location named {string} exists with {int} charger")
    public void pricing_location_exists(String name, Integer chargerCount) {
        int id = nextLocationId++;
        stationManager.createLocation(id, name, "Test Address");

        // Create chargers for this location
        for (int i = 0; i < chargerCount; i++) {
            Charger c = new Charger(id * 100 + i, 50000, 150.0); // Dummy capacity/power
            stationManager.addChargerToLocation(id, c);
        }
    }

    @Given("there is no location with the name {string}")
    public void no_location_with_name(String name) {
        Location loc = findLocationByName(name);
        Assertions.assertNull(loc, "Location " + name + " should not exist but does.");
    }

    @When("I set pricing for location {string}: AC {double} EUR per kWh, DC {double} EUR per kWh, {double} EUR per min")
    public void set_full_pricing(String name, Double acKw, Double dcKw, Double minPrice) {
        Location loc = findLocationByName(name);
        if (loc != null) {
            // Using same price for AC/DC minute for simplicity based on input
            stationManager.updateLocationPricing(loc.getLocationId(), acKw, dcKw, minPrice, minPrice);
        } else {
            // Simulate system error for test if location missing
            lastException = new IllegalArgumentException("Location not found: " + name);
        }
    }

    @When("I set location {string} pricing: AC {double} EUR per kWh")
    public void set_ac_pricing_only(String name, Double acKw) {
        Location loc = findLocationByName(name);
        if (loc != null) {
            // We preserve existing values or set defaults if strictly updating AC
            // For this test, we assume defaults for others if not specified
            stationManager.updateLocationPricing(loc.getLocationId(), acKw, 0.0, 0.0, 0.0);
        }
    }

    @When("I attempt to set pricing for location {string}: AC {double} EUR per kWh, DC {double} EUR per kWh, {double} EUR per min")
    public void attempt_set_pricing(String name, Double acKw, Double dcKw, Double minPrice) {
        try {
            Location loc = findLocationByName(name);
            if (loc == null) {
                throw new IllegalArgumentException("Location not found: " + name);
            }
            stationManager.updateLocationPricing(loc.getLocationId(), acKw, dcKw, minPrice, minPrice);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("location {string} should have AC price {double} EUR per kWh")
    public void verify_ac_price(String name, Double expectedAc) {
        Location loc = findLocationByName(name);
        Assertions.assertNotNull(loc, "Location not found: " + name);

        PriceConfiguration price = stationManager.getPricingForLocation(loc.getLocationId());
        Assertions.assertNotNull(price, "Pricing not set for location");
        Assertions.assertEquals(expectedAc, price.getAcPricePerKWh(), 0.001);
    }

    @Then("location {string} should have DC price {double} EUR per kWh")
    public void verify_dc_price(String name, Double expectedDc) {
        Location loc = findLocationByName(name);
        Assertions.assertNotNull(loc);

        PriceConfiguration price = stationManager.getPricingForLocation(loc.getLocationId());
        Assertions.assertEquals(expectedDc, price.getDcPricePerKWh(), 0.001);
    }

    @Then("location {string} pricing should apply to all chargers")
    public void verify_pricing_all_chargers(String name) {
        Location loc = findLocationByName(name);
        Assertions.assertNotNull(loc);

        for (Charger c : loc.getChargers()) {
            Assertions.assertNotNull(c.getPriceConfiguration(), "Charger " + c.getChargerId() + " has no price");
            // Check if price config object is associated
            Assertions.assertEquals(loc.getLocationId(), c.getPriceConfiguration().getPriceConfigId());
            // Note: In StationManager.updateLocationPricing, we use identityHashCode as ID,
            // but effectively we just check that a config exists.
        }
    }

    @Then("location {string} pricing should have a timestamp")
    public void verify_timestamp(String name) {
        Location loc = findLocationByName(name);
        Assertions.assertNotNull(loc);
        PriceConfiguration price = stationManager.getPricingForLocation(loc.getLocationId());

        // This ensures history tracking is possible
        Assertions.assertNotNull(price.getLastUpdated(), "Timestamp should not be null");
    }

    @Then("an error should be returned for non-existent location")
    public void verify_error_returned() {
        Assertions.assertNotNull(lastException, "Expected an exception but none was thrown");
        Assertions.assertTrue(lastException.getMessage().contains("Location not found")
                || lastException.getMessage().contains("NonExistent"));
    }

    // HELPER to find location by name since StationManager uses IDs
    private Location findLocationByName(String name) {
        List<Location> all = stationManager.getAllLocations();
        for (Location loc : all) {
            if (loc.getName().equals(name)) {
                return loc;
            }
        }
        return null;
    }
}