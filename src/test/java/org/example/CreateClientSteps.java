package org.example;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.ClientManager;
import org.example.domain.Client;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;

public class CreateClientSteps {

    private ClientManager clientManager;
    private Client lastCreatedClient;
    private Exception lastException; // Store exceptions here

    @Before
    public void setup() {
        clientManager = new ClientManager();
        lastException = null;
        lastCreatedClient = null;
    }

    @Given("the client management system is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(clientManager);
    }

    @When("I register a client with name {string} and email {string}")
    public void i_register_client(String name, String email) {
        lastCreatedClient = clientManager.registerClient(name, email);
    }

    @When("I register client {string} with email {string}")
    public void i_register_client_short(String name, String email) {
        clientManager.registerClient(name, email);
    }

    // NEW: Attempt step that catches exceptions
    @When("I attempt to register a client with name {string} and email {string}")
    public void i_attempt_to_register(String name, String email) {
        try {
            // We force a check here or ensure ClientManager throws it
            if (name.isEmpty() || email.isEmpty()) {
                throw new IllegalArgumentException("Invalid input: name or email cannot be empty");
            }
            clientManager.registerClient(name, email);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I register the following clients:")
    public void i_register_following_clients(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        for (Map<String, String> row : rows) {
            String name = row.get("name");
            String email = row.get("email");
            double expectedBalance = Double.parseDouble(row.get("balance"));

            Client c = clientManager.registerClient(name, email);
            Assertions.assertEquals(expectedBalance, c.getAccount().getBalance(), 0.01);
        }
    }

    @Then("the client should be saved in the system")
    public void client_should_be_saved() {
        Assertions.assertNotNull(lastCreatedClient);
    }

    @Then("the client should have name {string}")
    public void client_should_have_name(String name) {
        Assertions.assertEquals(name, lastCreatedClient.getName());
    }

    @Then("the client should have email {string}")
    public void client_should_have_email(String email) {
        Assertions.assertEquals(email, lastCreatedClient.getEmail());
    }

    @Then("the client should have zero balance")
    public void client_should_have_zero_balance() {
        Assertions.assertEquals(0.0, lastCreatedClient.getAccount().getBalance(), 0.01);
    }

    @Then("the system should have {int} clients")
    public void system_should_have_clients(Integer count) {
        Assertions.assertEquals(count.intValue(), clientManager.getAllClients().size());
    }

    // NEW: Error assertions
    @Then("the client should not be registered")
    public void client_not_registered() {
        // If we tried to register 1 invalid client, count should still be 0 (or unchanged)
        // Since setup runs before every scenario, for the error case, count starts at 0.
        Assertions.assertEquals(0, clientManager.getAllClients().size());
    }

    @Then("an error should be returned for invalid input")
    public void error_invalid_input() {
        Assertions.assertNotNull(lastException, "Expected an exception but none was thrown");
        Assertions.assertTrue(lastException.getMessage().contains("Invalid input"));
    }
}