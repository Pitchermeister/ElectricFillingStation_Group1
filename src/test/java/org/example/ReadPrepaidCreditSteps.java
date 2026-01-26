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

public class ReadPrepaidCreditSteps {

    private ClientManager clientManager;

    // Store results of check actions. Key = Customer Name, Value = Balance
    private Map<String, Double> checkedBalances;
    private String lastCheckedCustomer; // To support the generic "Then the balance should be..."
    private Exception lastException;

    private Map<String, Client> clientsByName;
    private int nextClientId;

    @Before
    public void setup() {
        clientManager = new ClientManager();
        clientsByName = new HashMap<>();
        checkedBalances = new HashMap<>();
        lastCheckedCustomer = null;
        lastException = null;
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
        double bal = client.getAccount().getBalance();
        checkedBalances.put(name, bal);
        lastCheckedCustomer = name;
    }

    @When("I attempt to check the balance for {string}")
    public void i_attempt_check_balance(String name) {
        try {
            if (!clientsByName.containsKey(name)) {
                throw new IllegalArgumentException("Customer not found: " + name);
            }
            customer_checks_balance(name);
        } catch (Exception e) {
            lastException = e;
        }
    }

    // --- Assertion ---

    @Then("the balance should be {double} EUR")
    public void balance_should_be(Double expected) {
        Assertions.assertNotNull(lastCheckedCustomer, "No balance check performed yet");
        Double actual = checkedBalances.get(lastCheckedCustomer);
        Assertions.assertEquals(expected, actual, 0.01);
    }

    @Then("{string} balance should be {double} EUR")
    public void specific_balance_should_be(String name, Double expected) {
        Assertions.assertTrue(checkedBalances.containsKey(name), "Balance was not checked for " + name);
        Double actual = checkedBalances.get(name);
        Assertions.assertEquals(expected, actual, 0.01, "Incorrect balance for " + name);
    }

    // UPDATED: Changed string to match new feature file wording
    @Then("an error should be returned for unknown prepaid customer")
    public void error_returned_unknown_customer() {
        Assertions.assertNotNull(lastException, "Expected an exception but none was thrown");
        Assertions.assertTrue(lastException.getMessage().contains("Customer not found"));
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