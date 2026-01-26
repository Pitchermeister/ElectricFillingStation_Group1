package org.example;

import io.cucumber.java.en.*;

import org.example.Management.ChargingManager;
import org.example.Management.ClientManager;
import org.example.domain.Client;
import org.example.domain.ChargingService.ChargingSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeleteClientSteps {

    private ClientManager clientManager;
    private ChargingManager chargingManager;
    private Exception lastException;

    private Client client;
    private int clientId;
    // List to track multiple clients for the batch delete scenario
    private List<Client> batchClients;

    @Given("the delete client management system is initialized")
    public void the_delete_client_management_system_is_initialized() {
        clientManager = new ClientManager();
        chargingManager = new ChargingManager();
        client = null;
        clientId = -1;
        lastException = null;
        batchClients = new ArrayList<>();
    }

    @Given("a client {string} with email {string} is registered")
    public void a_client_with_email_is_registered(String name, String email) {
        client = clientManager.registerClient(name, email);
        clientId = client.getClientId();
        assertNotNull(clientManager.getClientById(clientId));
    }

    @Given("the client has a balance of {double}")
    public void the_client_has_a_balance_of(double amount) {
        Client c = clientManager.getClientById(clientId);
        assertNotNull(c);
        c.getAccount().topUp(amount);
        assertEquals(amount, c.getAccount().getBalance(), 0.0001);
    }

    @Given("the client has an active charging session")
    public void the_client_has_an_active_charging_session() {
        ChargingSession session = new ChargingSession(
                1L, clientId, 1, 1, null, null, LocalDateTime.now().minusMinutes(5)
        );
        chargingManager.addSession(session);
    }

    // NEW: Create multiple active sessions
    @Given("the client has {int} active charging sessions")
    public void the_client_has_multiple_active_sessions(int count) {
        for (int i = 0; i < count; i++) {
            ChargingSession session = new ChargingSession(
                    100L + i, clientId, 1, 10 + i, null, null, LocalDateTime.now().minusMinutes(5)
            );
            chargingManager.addSession(session);
        }
    }

    @Given("the client has a finished charging session")
    public void the_client_has_a_finished_charging_session() {
        ChargingSession session = new ChargingSession(
                2L, clientId, 1, 1, null, null, LocalDateTime.now().minusMinutes(10)
        );
        session.finish(LocalDateTime.now().minusMinutes(1), 0.0);
        chargingManager.addSession(session);
    }

    // NEW: Helper for batch creation
    @Given("a client {string} with email {string} is registered with zero balance")
    public void register_client_zero_balance(String name, String email) {
        Client c = clientManager.registerClient(name, email);
        batchClients.add(c);
    }

    @When("I try to delete the client")
    public void i_try_to_delete_the_client() {
        try {
            clientManager.deleteClient(clientId, chargingManager);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I attempt to delete a client with ID {int}")
    public void i_attempt_delete_client_id(int id) {
        try {
            clientManager.deleteClient(id, chargingManager);
        } catch (Exception e) {
            lastException = e;
        }
    }

    // NEW: Delete all tracked clients
    @When("I delete all three clients")
    public void i_delete_all_clients() {
        for (Client c : batchClients) {
            clientManager.deleteClient(c.getClientId(), chargingManager);
        }
    }

    @Then("the client should still exist in the system")
    public void the_client_should_still_exist_in_the_system() {
        assertNotNull(clientManager.getClientById(clientId), "Client should NOT have been deleted");
    }

    @Then("the client should be deleted from the system")
    public void the_client_should_be_deleted_from_the_system() {
        assertNull(clientManager.getClientById(clientId), "Client should have been deleted");
    }

    @Then("an error should be returned for client not found")
    public void error_client_not_found() {
        assertNotNull(lastException);
        assertTrue(lastException.getMessage().contains("Client not found"));
    }

    @Then("no clients should exist in the system")
    public void no_clients_exist() {
        assertEquals(0, clientManager.getAllClients().size(), "Expected system to be empty");
    }
}