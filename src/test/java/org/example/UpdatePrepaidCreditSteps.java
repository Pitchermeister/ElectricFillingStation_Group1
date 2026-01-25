package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.ClientManager;
import org.example.domain.Client;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class UpdatePrepaidCreditSteps {

    private ClientManager clientManager;

    // name -> client mapping (no IDs in feature)
    private Map<String, Client> clientsByName;
    private int nextClientId;

    @Before
    public void setup() {
        clientManager = new ClientManager();
        clientsByName = new HashMap<>();
        nextClientId = 1;
    }

    @Given("the update-credit service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(clientManager);
    }

    @Given("an update-credit customer {string} exists")
    public void customer_exists(String name) {
        ensureCustomerExists(name);
    }

    @Given("an update-credit customer {string} exists with balance {double} EUR")
    public void customer_exists_with_balance(String name, Double balance) {
        Client client = ensureCustomerExists(name);
        client.getAccount().topUp(balance);
    }

    // ✅ UNIQUE wording => avoids duplicate with ReadPrepaidCreditSteps
    @When("the update-credit customer {string} tops up {double} EUR")
    public void customer_tops_up(String name, Double amount) {
        Client client = requireCustomer(name);
        client.getAccount().topUp(amount);
    }

    // ✅ UNIQUE wording => avoids duplicate with ReadPrepaidCreditSteps
    @Then("the update-credit customer {string} balance should be {double} EUR")
    public void customer_balance_should_be(String name, Double expected) {
        Client client = requireCustomer(name);
        Assertions.assertEquals(expected, client.getAccount().getBalance(), 0.01);
    }

    // ✅ UNIQUE wording => avoids duplicate with ReadPrepaidCreditSteps
    @Then("the update-credit customer {string} should be able to charge")
    public void customer_should_be_able_to_charge(String name) {
        Client client = requireCustomer(name);
        Assertions.assertTrue(
                client.getAccount().getBalance() >= 1.0,
                "Expected at least EUR 1.00 to start charging, but was " + client.getAccount().getBalance()
        );
    }

    // -------------------
    // Helpers
    // -------------------

    private Client ensureCustomerExists(String name) {
        if (clientsByName.containsKey(name)) return clientsByName.get(name);

        int id = nextClientId++;
        String email = name.toLowerCase().replace(" ", ".") + "@test.com";
        Client client = clientManager.registerClient(id, name, email);

        clientsByName.put(name, client);
        return client;
    }

    private Client requireCustomer(String name) {
        Client client = clientsByName.get(name);
        Assertions.assertNotNull(client, "Unknown customer: " + name);
        return client;
    }
}
