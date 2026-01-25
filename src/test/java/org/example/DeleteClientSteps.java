package org.example.steps;

import io.cucumber.java.en.*;

import org.example.Management.ChargingManager;
import org.example.Management.ClientManager;
import org.example.domain.ChargingSession;
import org.example.domain.Client;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DeleteClientSteps {

    private ClientManager clientManager;
    private ChargingManager chargingManager;

    private Client client;
    private int clientId;

    @Given("the delete client management system is initialized")
    public void the_delete_client_management_system_is_initialized() {
        clientManager = new ClientManager();
        chargingManager = new ChargingManager();
        client = null;
        clientId = -1;
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
        assertNotNull(c.getAccount());

        c.getAccount().topUp(amount);
        assertEquals(amount, c.getAccount().getBalance(), 0.0001);
    }

    @Given("the client has an active charging session")
    public void the_client_has_an_active_charging_session() {
        ChargingSession session = new ChargingSession(
                1L,
                clientId,
                1, // locationId
                1, // chargerId
                null, // chargerType (für diesen Test egal)
                null, // priceConfiguration (für diesen Test egal)
                LocalDateTime.now().minusMinutes(5)
        );
        // nicht finish() -> active
        chargingManager.addSession(session);

        assertTrue(
                chargingManager.getSessionsByClientId(clientId).stream().anyMatch(s -> !s.isFinished()),
                "Expected at least one active session for client"
        );
    }

    @Given("the client has a finished charging session")
    public void the_client_has_a_finished_charging_session() {
        ChargingSession session = new ChargingSession(
                2L,
                clientId,
                1,
                1,
                null,
                null,
                LocalDateTime.now().minusMinutes(10)
        );
        session.finish(LocalDateTime.now().minusMinutes(1), 0.0);
        chargingManager.addSession(session);

        assertTrue(
                chargingManager.getSessionsByClientId(clientId).stream().allMatch(ChargingSession::isFinished),
                "Expected only finished sessions for client"
        );
    }

    @When("I try to delete the client")
    public void i_try_to_delete_the_client() {
        clientManager.deleteClient(clientId, chargingManager);
    }

    @Then("the client should still exist in the system")
    public void the_client_should_still_exist_in_the_system() {
        assertNotNull(clientManager.getClientById(clientId), "Client should NOT have been deleted");
    }

    @Then("the client should be deleted from the system")
    public void the_client_should_be_deleted_from_the_system() {
        assertNull(clientManager.getClientById(clientId), "Client should have been deleted");
    }
}
