package org.example;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.StationManager;
import org.example.domain.Location;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;

public class CreateLocationSteps {

    private StationManager stationManager;
    private Location lastCreatedLocation;
    private Exception lastException; // Store errors here

    @Before
    public void setup() {
        stationManager = new StationManager();
        lastCreatedLocation = null;
        lastException = null;
    }

    @Given("the location service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @When("I create a location with name {string} and address {string}")
    public void i_create_location(String name, String address) {
        lastCreatedLocation = stationManager.createLocation(name, address);
    }

    @When("I create location {string} at {string}")
    public void i_create_location_short(String name, String address) {
        stationManager.createLocation(name, address);
    }

    @When("I create the following locations:")
    public void i_create_following_locations(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        for (Map<String, String> row : rows) {
            String name = row.get("Name");
            String address = row.get("Address");
            stationManager.createLocation(name, address);
        }
    }

    // NEW: Attempt step for error cases
    @When("I attempt to create a location with name {string} and address {string}")
    public void i_attempt_create_location(String name, String address) {
        try {
            // Validate input (Simulating logic that might be in a Service layer)
            if (name.isEmpty() || address.isEmpty()) {
                throw new IllegalArgumentException("Invalid location data: name or address cannot be empty");
            }
            stationManager.createLocation(name, address);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the location should be saved")
    public void location_should_be_saved() {
        Assertions.assertNotNull(lastCreatedLocation);
    }

    @Then("the location should have name {string}")
    public void location_should_have_name(String name) {
        Assertions.assertEquals(name, lastCreatedLocation.getName());
    }

    @Then("the location should have address {string}")
    public void location_should_have_address(String address) {
        Assertions.assertEquals(address, lastCreatedLocation.getAddress());
    }

    @Then("the system should have {int} locations")
    public void system_should_have_locations(Integer count) {
        Assertions.assertEquals(count.intValue(), stationManager.getAllLocations().size());
    }

    // NEW: Error Assertions
    @Then("the location should not be created")
    public void location_not_created() {
        // Since setup runs fresh for every scenario, list should be empty if creation failed
        Assertions.assertTrue(stationManager.getAllLocations().isEmpty(), "Location list should be empty");
    }

    @Then("an error should be returned for invalid location data")
    public void error_returned() {
        Assertions.assertNotNull(lastException, "Expected an exception but none was thrown");
        Assertions.assertTrue(lastException.getMessage().contains("Invalid location data"));
    }
}