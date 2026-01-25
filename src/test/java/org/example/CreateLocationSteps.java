package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.StationManager;
import org.example.domain.Location;
import org.junit.jupiter.api.Assertions;

public class CreateLocationSteps {

    private StationManager stationManager;
    private Location lastCreatedLocation;

    @Before
    public void setup() {
        stationManager = new StationManager();
    }

    // UPDATED: Made unique to avoid conflict with CreateClientSteps
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
}