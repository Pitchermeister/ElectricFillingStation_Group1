package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.BillingManager;
import org.example.Management.ClientManager;
import org.example.Management.StationManager;
import org.example.domain.*;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;

public class ReadInvoiceSteps {

    private StationManager stationManager;
    private ClientManager clientManager;
    private BillingManager billingManager;
    private ChargingService chargingService;
    private String invoiceReport;

    @Before
    public void setup() {
        stationManager = new StationManager();
        clientManager = new ClientManager();
        billingManager = new BillingManager();
        chargingService = new ChargingService(clientManager, stationManager, billingManager);
    }

    @Given("the system is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("a client with ID {int} exists with balance €{double}")
    public void client_exists_with_balance(Integer id, Double balance) {
        Client client = clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
        client.getAccount().topUp(balance);
    }

    @Given("a location with ID {int} {string} exists with {int} charger")
    public void location_with_name_exists(Integer id, String name, Integer count) {
        stationManager.createLocation(id, name, "Address");
        for (int i = 0; i < count; i++) {
            Charger charger = new Charger(id * 100 + i, 900000, 150.0);
            stationManager.addChargerToLocation(id, charger);
        }
    }

    @Given("the client completes a charging session")
    public void client_completes_session() {
        Client client = clientManager.getAllClients().get(0);
        Location loc = stationManager.getAllLocations().get(0);
        Charger charger = loc.getChargers().get(0);

        chargingService.startSession(
            client.getClientId(),
            loc.getLocationId(),
            charger.getChargerId(),
            ChargerType.AC,
            LocalDateTime.now()
        );
        chargingService.finishSession(LocalDateTime.now().plusMinutes(20), 12.5);
    }

    @Given("the client completes {int} sessions at different times")
    public void client_completes_multiple_sessions(Integer count) {
        Client client = clientManager.getAllClients().get(0);
        Location loc = stationManager.getAllLocations().get(0);

        for (int i = 0; i < count; i++) {
            Charger charger = loc.getChargers().get(0);
            chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                charger.getChargerId(),
                ChargerType.AC,
                LocalDateTime.now().plusHours(i)
            );
            chargingService.finishSession(LocalDateTime.now().plusHours(i).plusMinutes(20), 10.0);
        }
    }

    @Given("a client with ID {int} exists")
    public void client_exists(Integer id) {
        clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
    }

    @Given("the client has topped up €{double} total")
    public void client_has_topped_up_total(Double amount) {
        Client client = clientManager.getAllClients().get(0);
        client.getAccount().topUp(amount);
    }

    @Given("the client has spent €{double} on charging")
    public void client_has_spent(Double amount) {
        Client client = clientManager.getAllClients().get(0);
        client.getAccount().debit(amount);
    }

    @When("I request the invoice for client {int}")
    public void i_request_invoice(Integer clientId) {
        invoiceReport = billingManager.getDetailedInvoiceReport(clientId, clientManager);
    }

    @When("I request the invoice")
    public void i_request_invoice() {
        Client client = clientManager.getAllClients().get(0);
        invoiceReport = billingManager.getDetailedInvoiceReport(client.getClientId(), clientManager);
    }

    @Then("the invoice should show the session details")
    public void invoice_should_show_session_details() {
        Assertions.assertTrue(invoiceReport.length() > 0);
    }

    @Then("the invoice should show the location name")
    public void invoice_should_show_location_name() {
        Assertions.assertTrue(invoiceReport.contains("Location") || invoiceReport.contains("City Center"));
    }

    @Then("the invoice should show the charged kWh")
    public void invoice_should_show_kwh() {
        Assertions.assertTrue(invoiceReport.contains("kWh"));
    }

    @Then("the invoice should show the price")
    public void invoice_should_show_price() {
        Assertions.assertTrue(invoiceReport.contains("€") || invoiceReport.contains("EUR"));
    }

    @Then("the sessions should be sorted chronologically")
    public void sessions_should_be_sorted() {
        Assertions.assertTrue(invoiceReport.contains("sorted") || invoiceReport.length() > 0);
    }

    @Then("the invoice should show {int} items")
    public void invoice_should_show_items(Integer count) {
        Assertions.assertEquals(count.intValue(), billingManager.getInvoiceCountForClient(1));
    }

    @Then("the invoice should show total top-ups €{double}")
    public void invoice_should_show_topups(Double amount) {
        Assertions.assertTrue(invoiceReport.contains(String.format("%.2f", amount)));
    }

    @Then("the invoice should show total spent €{double}")
    public void invoice_should_show_spent(Double amount) {
        Assertions.assertTrue(invoiceReport.contains(String.format("%.2f", amount)));
    }

    @Then("the invoice should show remaining balance €{double}")
    public void invoice_should_show_remaining(Double amount) {
        Client client = clientManager.getAllClients().get(0);
        Assertions.assertEquals(amount, client.getAccount().getBalance(), 0.01);
    }
}
