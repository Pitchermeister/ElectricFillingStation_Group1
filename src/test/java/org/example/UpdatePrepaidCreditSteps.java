package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.ClientManager;
import org.example.domain.Client;
import org.junit.jupiter.api.Assertions;

public class UpdatePrepaidCreditSteps {

    private ClientManager clientManager;

    @Before
    public void setup() {
        clientManager = new ClientManager();
    }

    @Given("the system is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(clientManager);
    }

    @Given("a client with ID {int} exists")
    public void client_exists(Integer id) {
        clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
    }

    @Given("a client with ID {int} exists with balance €{double}")
    public void client_exists_with_balance(Integer id, Double balance) {
        Client client = clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
        client.getAccount().topUp(balance);
    }

    @When("the client tops up €{double}")
    public void client_tops_up(Double amount) {
        Client client = clientManager.getAllClients().get(clientManager.getAllClients().size() - 1);
        client.getAccount().topUp(amount);
    }

    @Then("the client balance should be €{double}")
    public void client_balance_should_be(Double expected) {
        Client client = clientManager.getAllClients().get(clientManager.getAllClients().size() - 1);
        Assertions.assertEquals(expected, client.getAccount().getBalance(), 0.01);
    }

    @Then("the client should be able to charge")
    public void client_should_be_able_to_charge() {
        Client client = clientManager.getAllClients().get(0);
        Assertions.assertTrue(client.getAccount().getBalance() >= 1.0);
    }
}
