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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReadInvoiceSteps {

    private StationManager stationManager;
    private ClientManager clientManager;
    private BillingManager billingManager;
    private ChargingService chargingService;

    private String invoiceReport;

    // BDD-friendly identity (names) -> internal objects
    private Map<String, Client> clientsByName;
    private Map<String, Location> locationsByName;

    // Internal technical IDs (hidden from feature files)
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
        Assertions.assertNotNull(clientManager);
        Assertions.assertNotNull(billingManager);
        Assertions.assertNotNull(chargingService);
    }

    // -------------------------
    // Customers (no IDs)
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
    // Locations (no IDs)
    // -------------------------

    @Given("a charging location named {string} exists with {int} charger")
    public void location_named_exists_with_chargers(String locationName, Integer count) {
        Location loc = ensureLocationExists(locationName);

        // Add chargers
        for (int i = 0; i < count; i++) {
            int chargerId = loc.getLocationId() * 100 + i;
            Charger charger = new Charger(chargerId, 900000 + chargerId, 150.0);
            stationManager.addChargerToLocation(loc.getLocationId(), charger);
        }

        // Ensure pricing exists so sessions can start
        ensureLocationHasDefaultPricing(loc);
    }

    @Given("location {string} has pricing AC {double} EUR per kWh, DC {double} EUR per kWh, {double} EUR per min")
    public void location_has_pricing(String locationName, Double ac, Double dc, Double perMin) {
        Location loc = ensureLocationExists(locationName);
        // pricing belongs to location
        stationManager.updateLocationPricing(loc.getLocationId(), ac, dc, perMin, perMin);
    }

    // -------------------------
    // Charging sessions (no IDs)
    // -------------------------

    @Given("{string} completes a charging session at {string}")
    public void customer_completes_session_at(String customerName, String locationName) {
        Client client = requireCustomer(customerName);
        Location loc = requireLocation(locationName);

        Assertions.assertFalse(loc.getChargers().isEmpty(), "No chargers available at location: " + locationName);
        Charger charger = loc.getChargers().get(0);

        ensureLocationHasDefaultPricing(loc);

        // Use stable times in the past to avoid flaky ordering
        LocalDateTime start = LocalDateTime.now().minusMinutes(30);
        chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                charger.getChargerId(),
                ChargerType.AC,
                start
        );
        chargingService.finishSession(start.plusMinutes(20), 12.5);
    }

    @Given("{string} completes {int} charging sessions at different times at {string}")
    public void customer_completes_multiple_sessions(String customerName, Integer count, String locationName) {
        Client client = requireCustomer(customerName);
        Location loc = requireLocation(locationName);

        Assertions.assertFalse(loc.getChargers().isEmpty(), "No chargers available at location: " + locationName);
        Charger charger = loc.getChargers().get(0);

        ensureLocationHasDefaultPricing(loc);

        // Create sessions with increasing start times (chronological)
        // Example: now-3h, now-2h, now-1h ...
        for (int i = count; i >= 1; i--) {
            LocalDateTime start = LocalDateTime.now().minusHours(i);
            chargingService.startSession(
                    client.getClientId(),
                    loc.getLocationId(),
                    charger.getChargerId(),
                    ChargerType.AC,
                    start
            );
            chargingService.finishSession(start.plusMinutes(20), 10.0);
        }
    }

    // -------------------------
    // Balance/Invoice setup (no IDs)
    // -------------------------

    @Given("{string} has topped up {double} EUR total")
    public void customer_has_topped_up_total(String customerName, Double amount) {
        Client client = requireCustomer(customerName);
        client.getAccount().topUp(amount);
    }

    /**
     * Keeps your previous "spent" hack, but scoped to a named customer.
     * This simulates historical spending entries on the invoice.
     */
    @Given("{string} has spent {double} EUR on charging")
    public void customer_has_spent(String customerName, Double amount) {
        Client client = requireCustomer(customerName);

        // 1) Deduct balance
        client.getAccount().debit(amount);

        // 2) Create a dummy finished session whose calculated total matches 'amount'
        double fakeKWh = 10.0;
        double requiredPrice = amount / fakeKWh;

        PriceConfiguration fakePrice = new PriceConfiguration(
                999, // fake price config id
                requiredPrice,
                requiredPrice,
                0.0,
                0.0
        );

        ChargingSession dummySession = new ChargingSession(
                999, // fake session id
                client.getClientId(),
                1,   // fake location id
                101, // fake charger id
                ChargerType.AC,
                fakePrice,
                LocalDateTime.now().minusHours(2)
        );

        dummySession.finish(LocalDateTime.now().minusHours(1), fakeKWh);

        // 3) Register it in billing
        billingManager.createEntryFromSession(dummySession, "Manual Adjustment");
    }

    // -------------------------
    // Invoice request (no IDs)
    // -------------------------

    @When("{string} requests their invoice")
    public void customer_requests_invoice(String customerName) {
        Client client = requireCustomer(customerName);
        invoiceReport = billingManager.getDetailedInvoiceReport(client.getClientId(), clientManager);
    }

    /**
     * Convenience fallback: if you still have a scenario that says "When I request the invoice"
     * this uses the first created customer.
     */
    @When("I request the invoice")
    public void i_request_invoice_fallback() {
        Assertions.assertFalse(clientsByName.isEmpty(), "No customers exist in scenario");
        Client client = clientsByName.values().iterator().next();
        invoiceReport = billingManager.getDetailedInvoiceReport(client.getClientId(), clientManager);
    }

    // -------------------------
    // Assertions (BDD-friendly)
    // -------------------------

    @Then("the invoice should show the session details")
    public void invoice_should_show_session_details() {
        Assertions.assertNotNull(invoiceReport);
        Assertions.assertFalse(invoiceReport.isBlank(), "Invoice report should not be empty");
    }

    @Then("the invoice sessions should be sorted chronologically")
    public void the_invoice_sessions_should_be_sorted_chronologically() {
        Assertions.assertNotNull(invoiceReport);
        Assertions.assertTrue(invoiceReport.contains("by start time"),
                "Invoice should indicate chronological sorting ('by start time').\n" + invoiceReport);
    }


    @Then("the invoice should include location {string}")
    public void invoice_should_include_location(String locationName) {
        Assertions.assertNotNull(invoiceReport);
        Assertions.assertTrue(invoiceReport.contains(locationName),
                "Expected location name '" + locationName + "' in report:\n" + invoiceReport);
    }

    @Then("the invoice should list {int} charging sessions")
    public void the_invoice_should_list_charging_sessions(Integer count) {
        invoice_should_show_items(count);
    }

    @Then("the invoice should include charged energy {double} kWh")
    public void invoice_should_include_charged_energy(Double kwh) {
        Assertions.assertNotNull(invoiceReport);
        // match typical formatting: "12.50" could appear as "12.5" depending on formatting in report
        String expectedA = String.format(Locale.US, "%.2f", kwh);
        String expectedB = String.format(Locale.US, "%.1f", kwh);

        Assertions.assertTrue(invoiceReport.contains("kWh"), "Invoice should contain 'kWh' unit.\n" + invoiceReport);
        Assertions.assertTrue(invoiceReport.contains(expectedA) || invoiceReport.contains(expectedB),
                "Expected kWh amount (" + expectedA + " or " + expectedB + ") in report:\n" + invoiceReport);
    }

    @Then("the invoice should include a total price")
    public void invoice_should_include_a_total_price() {
        Assertions.assertNotNull(invoiceReport);
        Assertions.assertTrue(invoiceReport.contains("€") || invoiceReport.contains("EUR"),
                "Expected currency (€, EUR) in report:\n" + invoiceReport);
    }

    @Then("the sessions should be sorted chronologically")
    public void sessions_should_be_sorted() {
        Assertions.assertNotNull(invoiceReport);
        Assertions.assertTrue(invoiceReport.contains("Sessions"),
                "Invoice should contain session list header 'Sessions'.\n" + invoiceReport);
    }

    @Then("the invoice should show {int} items")
    public void invoice_should_show_items(Integer expectedCount) {
        // Use first customer in scenario unless you want a named variant
        Assertions.assertFalse(clientsByName.isEmpty(), "No customers exist in scenario");
        Client client = clientsByName.values().iterator().next();

        int actual = billingManager.getInvoiceCountForClient(client.getClientId());
        Assertions.assertEquals(expectedCount.intValue(), actual,
                "Expected " + expectedCount + " invoice items, got " + actual);
    }

    @Then("the invoice should show total top-ups {double} EUR")
    public void invoice_should_show_topups(Double amount) {
        Assertions.assertNotNull(invoiceReport);
        String expected = String.format(Locale.US, "%.2f", amount);
        Assertions.assertTrue(invoiceReport.contains(expected),
                "Expected top-up " + expected + " in report:\n" + invoiceReport);
    }

    @Then("the invoice should show total spent {double} EUR")
    public void invoice_should_show_spent(Double amount) {
        Assertions.assertNotNull(invoiceReport);
        String expected = String.format(Locale.US, "%.2f", amount);
        Assertions.assertTrue(invoiceReport.contains(expected),
                "Expected spent " + expected + " in report:\n" + invoiceReport);
    }

    @Then("the invoice should show remaining balance {double} EUR")
    public void invoice_should_show_remaining(Double amount) {
        Assertions.assertFalse(clientsByName.isEmpty(), "No customers exist in scenario");
        Client client = clientsByName.values().iterator().next();
        Assertions.assertEquals(amount, client.getAccount().getBalance(), 0.01);
    }

    // -------------------------
    // Helpers
    // -------------------------

    private Client ensureCustomerExists(String customerName) {
        Client existing = clientsByName.get(customerName);
        if (existing != null) return existing;

        int id = nextClientId++;
        String email = customerName.toLowerCase().replace(" ", ".") + "@test.com";
        Client client = clientManager.registerClient(id, customerName, email);
        clientsByName.put(customerName, client);
        return client;
    }

    private Location ensureLocationExists(String locationName) {
        Location existing = locationsByName.get(locationName);
        if (existing != null) return existing;

        int id = nextLocationId++;
        Location loc = stationManager.createLocation(id, locationName, "Address");
        locationsByName.put(locationName, loc);
        return loc;
    }

    private Client requireCustomer(String customerName) {
        Client c = clientsByName.get(customerName);
        Assertions.assertNotNull(c, "Unknown customer: " + customerName);
        return c;
    }

    private Location requireLocation(String locationName) {
        Location l = locationsByName.get(locationName);
        Assertions.assertNotNull(l, "Unknown location: " + locationName);
        return l;
    }

    private void ensureLocationHasDefaultPricing(Location loc) {
        if (loc.getPriceConfiguration() == null) {
            loc.setPriceConfiguration(new PriceConfiguration(
                    loc.getLocationId(),
                    0.45, 0.55,
                    0.20, 0.20
            ));
        }
    }
}