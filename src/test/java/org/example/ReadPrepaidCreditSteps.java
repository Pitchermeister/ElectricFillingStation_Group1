package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.ClientManager;
import org.example.domain.Client;
import org.junit.jupiter.api.Assertions;

public class ReadPrepaidCreditSteps {

    private ClientManager clientManager;
    private double lastBalance;

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

    @Given("the client tops up €{double}")
    public void the_client_tops_up(Double amount) {
        Client client = clientManager.getAllClients().get(0);
        client.getAccount().topUp(amount);
    }

    @Given("the client spends €{double} on charging")
    public void client_spends(Double amount) {
        Client client = clientManager.getAllClients().get(0);
        client.getAccount().debit(amount);
    }

    @When("I check the client balance")
    public void i_check_balance() {
        Client client = clientManager.getAllClients().get(0);
        lastBalance = client.getAccount().getBalance();
    }

    @Then("the balance should be €{double}")
    public void balance_should_be(Double expected) {
        Assertions.assertEquals(expected, lastBalance, 0.01);
    }
}
