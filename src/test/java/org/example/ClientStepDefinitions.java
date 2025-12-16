package org.example;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;

public class ClientStepDefinitions {

    // 1. Instantiate your Management Class (The Logic)
    private ClientManager clientManager = new ClientManager();

    // 2. specific variables to hold data between steps
    private String tempName;
    private String tempEmail;
    private Client registeredClient;

    // --------------------------------------------------------
    // MAPPING THE GHERKIN STEPS
    // --------------------------------------------------------

    // Matches: Given I have a client name "John Doe" and email "john@example.com"
    @Given("I have a client name {string} and email {string}")
    public void i_have_a_client_name_and_email(String name, String email) {
        // Just store the data for now, don't save to DB yet
        this.tempName = name;
        this.tempEmail = email;
    }

    // Matches: When I register the new client
    @When("I register the new client")
    public void i_register_the_new_client() {
        // Now call the Manager to actually do the work
        // We use a dummy ID (e.g., 1) for this test
        this.registeredClient = clientManager.registerClient(1, tempName, tempEmail);
    }

    // Matches: Then the client should be saved in the system
    @Then("the client should be saved in the system")
    public void the_client_should_be_saved_in_the_system() {
        // Verify that the object is not null
        Assertions.assertNotNull(registeredClient, "Client was not returned by the manager");

        // Double check by trying to find them in the 'database'
        Client found = clientManager.getClientById(1);
        Assertions.assertNotNull(found, "Client was not found in the list after registration");
    }

    // Matches: And the system should return a valid Client ID
    @Then("the system should return a valid Client ID")
    public void the_system_should_return_a_valid_client_id() {
        // Check that the ID is what we expect
        Assertions.assertEquals(1, registeredClient.getClientId());

        // Optional: Check name matches too
        Assertions.assertEquals(tempName, registeredClient.getName());
    }
}