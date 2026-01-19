package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.ClientManager;
import org.example.domain.Client;
import org.junit.jupiter.api.Assertions;

public class CreateClientSteps {

    private ClientManager clientManager;
    private Client lastCreatedClient;

    @Before
    public void setup() {
        clientManager = new ClientManager();
    }

    @Given("the system is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(clientManager);
    }

    @When("I register a client with ID {int}, name {string} and email {string}")
    public void i_register_client(Integer id, String name, String email) {
        lastCreatedClient = clientManager.registerClient(id, name, email);
    }

    @When("I register client ID {int} {string} with email {string}")
    public void i_register_client_short(Integer id, String name, String email) {
        clientManager.registerClient(id, name, email);
    }

    @Then("the client should be saved in the system")
    public void client_should_be_saved() {
        Assertions.assertNotNull(lastCreatedClient);
    }

    @Then("the client should have ID {int}")
    public void client_should_have_id(Integer id) {
        Assertions.assertEquals(id.intValue(), lastCreatedClient.getClientId());
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
}
