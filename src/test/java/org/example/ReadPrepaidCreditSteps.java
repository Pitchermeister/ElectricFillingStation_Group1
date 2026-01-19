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

    // UPDATED: Unique wording
    @Given("the read-credit service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(clientManager);
    }

    // UPDATED: Unique wording
    @Given("a read-credit client with ID {int} exists")
    public void client_exists(Integer id) {
        clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
    }

    // UPDATED: Unique wording + removed symbol
    @Given("a read-credit client with ID {int} exists with balance {double} EUR")
    public void client_exists_with_balance(Integer id, Double balance) {
        Client client = clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
        client.getAccount().topUp(balance);
    }

    // UPDATED: Unique wording + removed symbol
    @Given("the read-credit client tops up {double} EUR")
    public void the_client_tops_up(Double amount) {
        Client client = clientManager.getAllClients().get(0);
        client.getAccount().topUp(amount);
    }

    // UPDATED: Unique wording + removed symbol
    @Given("the read-credit client spends {double} EUR on charging")
    public void client_spends(Double amount) {
        Client client = clientManager.getAllClients().get(0);
        client.getAccount().debit(amount);
    }

    @When("I check the client balance")
    public void i_check_balance() {
        Client client = clientManager.getAllClients().get(0);
        lastBalance = client.getAccount().getBalance();
    }

    // UPDATED: Removed symbol
    @Then("the balance should be {double} EUR")
    public void balance_should_be(Double expected) {
        Assertions.assertEquals(expected, lastBalance, 0.01);
    }
}