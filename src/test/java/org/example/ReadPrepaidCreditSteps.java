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

/**
 * ReadPrepaidCreditSteps
 * BDD-friendly: works with customer names instead of technical IDs.
 */
public class ReadPrepaidCreditSteps {

    private ClientManager clientManager;
    private double lastBalance;

    // internal name → client mapping
    private Map<String, Client> clientsByName;
    private int nextClientId;

    @Before
    public void setup() {
        clientManager = new ClientManager();
        clientsByName = new HashMap<>();
        nextClientId = 1;
    }

    @Given("the read-credit service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(clientManager);
    }

    // --- Customer creation ---

    @Given("a read-credit customer {string} exists")
    public void customer_exists(String name) {
        ensureCustomerExists(name);
    }

    @Given("a read-credit customer {string} exists with balance {double} EUR")
    public void customer_exists_with_balance(String name, Double balance) {
        Client client = ensureCustomerExists(name);
        client.getAccount().topUp(balance);
    }

    // --- Account actions ---

    @Given("{string} tops up {double} EUR")
    public void customer_tops_up(String name, Double amount) {
        Client client = requireCustomer(name);
        client.getAccount().topUp(amount);
    }

    @Given("{string} spends {double} EUR on charging")
    public void customer_spends(String name, Double amount) {
        Client client = requireCustomer(name);
        client.getAccount().debit(amount);
    }

    // --- Read balance ---

    @When("{string} checks their balance")
    public void customer_checks_balance(String name) {
        Client client = requireCustomer(name);
        lastBalance = client.getAccount().getBalance();
    }

    // --- Assertion ---

    @Then("the balance should be {double} EUR")
    public void balance_should_be(Double expected) {
        Assertions.assertEquals(expected, lastBalance, 0.01);
    }

    // --- Helpers ---

    private Client ensureCustomerExists(String name) {
        if (clientsByName.containsKey(name)) {
            return clientsByName.get(name);
        }
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
