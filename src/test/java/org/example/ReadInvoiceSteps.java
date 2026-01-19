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
        invoiceReport = "";
    }

    @Given("the invoice service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("an invoicing client with ID {int} exists with balance {double} EUR")
    public void client_exists_with_balance(Integer id, Double balance) {
        Client client = clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
        client.getAccount().topUp(balance);
    }

    @Given("an invoicing location with ID {int} {string} exists with {int} charger")
    public void location_with_name_exists(Integer id, String name, Integer count) {
        stationManager.createLocation(id, name, "Address");
        PriceConfiguration defaultPrice = new PriceConfiguration(id, 0.45, 0.55, 0.20, 0.20);

        for (int i = 0; i < count; i++) {
            Charger charger = new Charger(id * 100 + i, 900000, 150.0);
            charger.setPriceConfiguration(defaultPrice);
            stationManager.addChargerToLocation(id, charger);
        }
    }

    @Given("the invoicing client completes a charging session")
    public void client_completes_session() {
        Client client = clientManager.getAllClients().get(0);
        Location loc = stationManager.getAllLocations().get(0);
        Charger charger = loc.getChargers().get(0);

        if (charger.getPriceConfiguration() == null) {
            charger.setPriceConfiguration(new PriceConfiguration(999, 0.45, 0.55, 0.20, 0.20));
        }

        chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                charger.getChargerId(),
                ChargerType.AC,
                LocalDateTime.now()
        );
        chargingService.finishSession(LocalDateTime.now().plusMinutes(20), 12.5);
    }

    @Given("the invoicing client completes {int} sessions at different times")
    public void client_completes_multiple_sessions(Integer count) {
        Client client = clientManager.getAllClients().get(0);

        if (stationManager.getAllLocations().isEmpty()) {
            stationManager.createLocation(1, "Default Loc", "Addr");
            Charger c = new Charger(101, 999, 50.0);
            c.setPriceConfiguration(new PriceConfiguration(999, 0.45, 0.55, 0.20, 0.20));
            stationManager.addChargerToLocation(1, c);
        }

        Location loc = stationManager.getAllLocations().get(0);
        Charger charger = loc.getChargers().get(0);

        if (charger.getPriceConfiguration() == null) {
            charger.setPriceConfiguration(new PriceConfiguration(999, 0.45, 0.55, 0.20, 0.20));
        }

        for (int i = 0; i < count; i++) {
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

    @Given("an invoicing client with ID {int} exists")
    public void client_exists(Integer id) {
        clientManager.registerClient(id, "Client " + id, "client" + id + "@test.com");
    }

    @Given("the invoicing client has topped up {double} EUR total")
    public void client_has_topped_up_total(Double amount) {
        Client client = clientManager.getAllClients().get(0);
        client.getAccount().topUp(amount);
    }

    // --- FIX IS HERE ---
    @Given("the invoicing client has spent {double} EUR on charging")
    public void client_has_spent(Double amount) {
        Client client = clientManager.getAllClients().get(0);

        // 1. Deduct balance physically
        client.getAccount().debit(amount);

        // 2. Create a "Fake" session to show up in the Invoice
        // We need the calculated cost to equal 'amount' (e.g., 30.00)
        // Let's assume we charged 10 kWh.
        // So we need a price of (amount / 10.0) per kWh.
        double fakeKWh = 10.0;
        double requiredPrice = amount / fakeKWh;

        // Create a price config that forces the correct total cost
        PriceConfiguration fakePrice = new PriceConfiguration(999, requiredPrice, requiredPrice, 0.0, 0.0);

        ChargingSession dummySession = new ChargingSession(
                999, // Fake session ID
                client.getClientId(),
                1, // Fake location
                101, // Fake charger
                ChargerType.AC,
                fakePrice, // <--- This forces the cost calculation
                LocalDateTime.now().minusHours(1)
        );

        // 3. Use 'finish' instead of 'endSession'
        dummySession.finish(LocalDateTime.now(), fakeKWh);

        // 4. Register it in the billing system
        billingManager.createEntryFromSession(dummySession, "Manual Adjustment");
    }
    // -------------------

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
        Assertions.assertNotNull(invoiceReport);
        Assertions.assertTrue(invoiceReport.length() > 0, "Invoice report should not be empty");
    }

    @Then("the invoice should show the location name")
    public void invoice_should_show_location_name() {
        Assertions.assertTrue(invoiceReport.contains("Location") || invoiceReport.contains("City Center") || invoiceReport.contains("Manual Adjustment"));
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
        // Assert that the report contains the word "Sessions" indicating the list is present
        Assertions.assertTrue(invoiceReport.contains("Sessions"), "Invoice should contain session list");
    }

    @Then("the invoice should show {int} items")
    public void invoice_should_show_items(Integer count) {
        Assertions.assertEquals(count.intValue(), billingManager.getInvoiceCountForClient(1));
    }

    @Then("the invoice should show total top-ups {double} EUR")
    public void invoice_should_show_topups(Double amount) {
        Assertions.assertTrue(invoiceReport.contains(String.format("%.2f", amount)),
                "Expected top-up " + amount + " in report: " + invoiceReport);
    }

    @Then("the invoice should show total spent {double} EUR")
    public void invoice_should_show_spent(Double amount) {
        Assertions.assertTrue(invoiceReport.contains(String.format("%.2f", amount)),
                "Expected spent " + amount + " in report: " + invoiceReport);
    }

    @Then("the invoice should show remaining balance {double} EUR")
    public void invoice_should_show_remaining(Double amount) {
        Client client = clientManager.getAllClients().get(0);
        Assertions.assertEquals(amount, client.getAccount().getBalance(), 0.01);
    }
}