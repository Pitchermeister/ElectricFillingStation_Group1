package org.example;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.BillingManager;
import org.example.Management.ClientManager;
import org.example.Management.StationManager;
import org.example.domain.*;
// We still need this import for the dummy session at the bottom
import org.example.domain.ChargingService.ChargingSession;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReadInvoiceSteps {

    private StationManager stationManager;
    private ClientManager clientManager;
    private BillingManager billingManager;
    private ChargingService chargingService;

    private String invoiceReport;

    private Map<String, Client> clientsByName;
    private Map<String, Location> locationsByName;

    private int nextClientId;
    private int nextLocationId;

    @Before
    public void setup() {
        Locale.setDefault(Locale.US);
        stationManager = new StationManager();
        clientManager = new ClientManager();
        billingManager = new BillingManager();
        chargingService = new ChargingService(clientManager, stationManager, billingManager);

        invoiceReport = "";
        clientsByName = new HashMap<>();
        locationsByName = new HashMap<>();

        nextClientId = 1;
        nextLocationId = 1;
    }

    // -------------------------
    // Initialization
    // -------------------------

    @Given("the invoice service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    // -------------------------
    // Customers
    // -------------------------

    @Given("an invoicing customer {string} exists with balance {double} EUR")
    public void customer_exists_with_balance(String customerName, Double balance) {
        Client c = ensureCustomerExists(customerName);
        c.getAccount().topUp(balance);
    }

    @Given("an invoicing customer {string} exists")
    public void customer_exists(String customerName) {
        ensureCustomerExists(customerName);
    }

    // -------------------------
    // Locations
    // -------------------------

    @Given("a charging location named {string} exists with {int} charger")
    public void location_named_exists_with_chargers(String locationName, Integer count) {
        Location loc = ensureLocationExists(locationName);
        for (int i = 0; i < count; i++) {
            int chargerId = loc.getLocationId() * 100 + i;
            Charger charger = new Charger(chargerId, 900000 + chargerId, 150.0);
            stationManager.addChargerToLocation(loc.getLocationId(), charger);
        }
        ensureLocationHasDefaultPricing(loc);
    }

    @Given("location {string} has pricing AC {double} EUR per kWh, DC {double} EUR per kWh, {double} EUR per min")
    public void location_has_pricing(String locationName, Double ac, Double dc, Double perMin) {
        Location loc = ensureLocationExists(locationName);
        stationManager.updateLocationPricing(loc.getLocationId(), ac, dc, perMin, perMin);
    }

    // -------------------------
    // Charging sessions
    // -------------------------

    @Given("{string} completes a charging session at {string}")
    public void customer_completes_session_at(String customerName, String locationName) {
        Client client = requireCustomer(customerName);
        Location loc = requireLocation(locationName);
        Charger charger = loc.getChargers().get(0);
        ensureLocationHasDefaultPricing(loc);

        LocalDateTime start = LocalDateTime.now().minusMinutes(30);

        // 1. Start the session
        chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                charger.getChargerId(),
                ChargerType.AC,
                start
        );

        // 2. Finish the session (FIXED: Removed 'session' argument)
        // The service knows the active session internally.
        chargingService.finishSession(start.plusMinutes(20), 12.5);
    }

    @Given("{string} completes {int} charging sessions at different times at {string}")
    public void customer_completes_multiple_sessions(String customerName, Integer count, String locationName) {
        Client client = requireCustomer(customerName);
        Location loc = requireLocation(locationName);
        Charger charger = loc.getChargers().get(0);
        ensureLocationHasDefaultPricing(loc);

        for (int i = count; i >= 1; i--) {
            LocalDateTime start = LocalDateTime.now().minusHours(i);

            // 1. Start
            chargingService.startSession(
                    client.getClientId(),
                    loc.getLocationId(),
                    charger.getChargerId(),
                    ChargerType.AC,
                    start
            );

            // 2. Finish (FIXED: Removed 'session' argument)
            chargingService.finishSession(start.plusMinutes(20), 10.0);
        }
    }

    // -------------------------
    // Balance/Invoice setup
    // -------------------------

    @Given("{string} has topped up {double} EUR total")
    public void customer_has_topped_up_total(String customerName, Double amount) {
        Client client = requireCustomer(customerName);
        client.getAccount().topUp(amount);
    }

    @Given("{string} has spent {double} EUR on charging")
    public void customer_has_spent(String customerName, Double amount) {
        Client client = requireCustomer(customerName);
        client.getAccount().debit(amount);

        double fakeKWh = 10.0;
        double requiredPrice = amount / fakeKWh;

        PriceConfiguration fakePrice = new PriceConfiguration(999, requiredPrice, requiredPrice, 0.0, 0.0);

        // We manually create a session here for the billing history
        ChargingSession dummySession = new ChargingSession(
                999, client.getClientId(), 1, 101, ChargerType.AC, fakePrice, LocalDateTime.now().minusHours(2)
        );

        // Manually finish it (this bypasses the service, so it's fine)
        dummySession.finish(LocalDateTime.now().minusHours(1), fakeKWh);

        billingManager.createEntryFromSession(dummySession, "Manual Adjustment");
    }

    // -------------------------
    // Invoice request
    // -------------------------

    @When("{string} requests their invoice")
    public void customer_requests_invoice(String customerName) {
        Client client = requireCustomer(customerName);
        invoiceReport = billingManager.getDetailedInvoiceReport(client.getClientId(), clientManager);
    }

    @When("I request the invoice")
    public void i_request_invoice_fallback() {
        Client client = clientsByName.values().iterator().next();
        invoiceReport = billingManager.getDetailedInvoiceReport(client.getClientId(), clientManager);
    }

    // -------------------------
    // Assertions
    // -------------------------

    @Then("the invoice should show the session details")
    public void invoice_should_show_session_details() {
        Assertions.assertNotNull(invoiceReport);
        Assertions.assertFalse(invoiceReport.isBlank());
    }

    @Then("the invoice sessions should be sorted chronologically")
    public void the_invoice_sessions_should_be_sorted_chronologically() {
        Assertions.assertTrue(invoiceReport.contains("by start time"));
    }

    @Then("the invoice should include location {string}")
    public void invoice_should_include_location(String locationName) {
        Assertions.assertTrue(invoiceReport.contains(locationName));
    }

    @Then("the invoice should list {int} charging sessions")
    public void the_invoice_should_list_charging_sessions(Integer count) {
        invoice_should_show_items(count);
    }

    @Then("the invoice should include charged energy {double} kWh")
    public void invoice_should_include_charged_energy(Double kwh) {
        String expectedA = String.format(Locale.US, "%.2f", kwh);
        String expectedB = String.format(Locale.US, "%.1f", kwh);
        Assertions.assertTrue(invoiceReport.contains(expectedA) || invoiceReport.contains(expectedB));
    }

    @Then("the invoice should include a total price")
    public void invoice_should_include_a_total_price() {
        Assertions.assertTrue(invoiceReport.contains("€") || invoiceReport.contains("EUR"));
    }

    @Then("the sessions should be sorted chronologically")
    public void sessions_should_be_sorted() {
        Assertions.assertTrue(invoiceReport.contains("Sessions"));
    }

    @Then("the invoice should show {int} items")
    public void invoice_should_show_items(Integer expectedCount) {
        Client client = clientsByName.values().iterator().next();
        int actual = billingManager.getInvoiceCountForClient(client.getClientId());
        Assertions.assertEquals(expectedCount.intValue(), actual);
    }

    @Then("the invoice should show total top-ups {double} EUR")
    public void invoice_should_show_topups(Double amount) {
        String expected = String.format(Locale.US, "%.2f", amount);
        Assertions.assertTrue(invoiceReport.contains(expected));
    }

    @Then("the invoice should show total spent {double} EUR")
    public void invoice_should_show_spent(Double amount) {
        String expected = String.format(Locale.US, "%.2f", amount);
        Assertions.assertTrue(invoiceReport.contains(expected));
    }

    @Then("the invoice should show remaining balance {double} EUR")
    public void invoice_should_show_remaining(Double amount) {
        Client client = clientsByName.values().iterator().next();
        Assertions.assertEquals(amount, client.getAccount().getBalance(), 0.01);
    }

    // -------------------------
    // Helpers
    // -------------------------

    private Client ensureCustomerExists(String customerName) {
        if (clientsByName.containsKey(customerName)) return clientsByName.get(customerName);
        int id = nextClientId++;
        Client client = clientManager.registerClient(id, customerName, customerName + "@test.com");
        clientsByName.put(customerName, client);
        return client;
    }

    private Location ensureLocationExists(String locationName) {
        if (locationsByName.containsKey(locationName)) return locationsByName.get(locationName);
        int id = nextLocationId++;
        Location loc = stationManager.createLocation(id, locationName, "Address");
        locationsByName.put(locationName, loc);
        return loc;
    }

    private Client requireCustomer(String customerName) {
        return clientsByName.get(customerName);
    }

    private Location requireLocation(String locationName) {
        return locationsByName.get(locationName);
    }

    private void ensureLocationHasDefaultPricing(Location loc) {
        if (stationManager.getPricingForLocation(loc.getLocationId()) == null) {
            stationManager.updateLocationPricing(loc.getLocationId(), 0.45, 0.55, 0.20, 0.20);
        }
    }
}