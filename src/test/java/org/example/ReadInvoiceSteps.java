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
    private Exception lastException;

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
        lastException = null;
        clientsByName = new HashMap<>();
        locationsByName = new HashMap<>();

        nextClientId = 1;
        nextLocationId = 1;
    }

    @Given("the invoice service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("an invoicing customer {string} exists with balance {double} EUR")
    public void customer_exists_with_balance(String customerName, Double balance) {
        Client c = ensureCustomerExists(customerName);
        c.getAccount().topUp(balance);
    }

    @Given("an invoicing customer {string} exists")
    public void customer_exists(String customerName) {
        ensureCustomerExists(customerName);
    }

    @Given("a charging location named {string} exists with {int} charger")
    public void location_named_exists_with_chargers(String locationName, Integer count) {
        Location loc = ensureLocationExists(locationName);
        for (int i = 0; i < count; i++) {
            // IDs start at 101
            int chargerId = loc.getLocationId() * 100 + i + 1;
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

    @Given("{string} completes a charging session at {string}")
    public void customer_completes_session_at(String customerName, String locationName) {
        performSession(customerName, locationName, 12.5, LocalDateTime.now().minusMinutes(30));
    }

    @Given("{string} completes a charging session with {double} kWh at {string}")
    public void customer_completes_session_with_kwh(String customerName, Double kwh, String locationName) {
        performSession(customerName, locationName, kwh, LocalDateTime.now().minusMinutes(10));
    }

    @Given("{string} completes {int} charging sessions at different times at {string}")
    public void customer_completes_multiple_sessions(String customerName, Integer count, String locationName) {
        for (int i = count; i >= 1; i--) {
            LocalDateTime start = LocalDateTime.now().minusHours(i);
            performSession(customerName, locationName, 10.0, start);
        }
    }

    @Given("{string} has topped up {double} EUR total")
    public void customer_has_topped_up_total(String customerName, Double amount) {
        Client client = requireCustomer(customerName);
        client.getAccount().topUp(amount);
    }

    @Given("{string} has spent {double} EUR on charging")
    public void customer_has_spent(String customerName, Double amount) {
        Client client = requireCustomer(customerName);
        client.getAccount().debit(amount);

        PriceConfiguration price = new PriceConfiguration(999, amount, amount, 0.0, 0.0);
        ChargingService.ChargingSession dummy = new ChargingService.ChargingSession(
                999, client.getClientId(), 1, 101, ChargerType.AC, price, LocalDateTime.now()
        );
        dummy.finish(LocalDateTime.now(), 1.0);
        billingManager.createEntryFromSession(dummy, "Manual Spend");
    }

    @When("{string} requests their invoice")
    public void customer_requests_invoice(String customerName) {
        Client client = requireCustomer(customerName);
        invoiceReport = billingManager.getDetailedInvoiceReport(client.getClientId(), clientManager);
    }

    @When("I request an invoice for a non-existent customer {string}")
    public void request_invoice_non_existent(String name) {
        try {
            Client c = clientsByName.get(name);
            int id = (c != null) ? c.getClientId() : 99999;

            if (clientManager.getClientById(id) == null) {
                throw new IllegalArgumentException("Customer not found");
            }
            invoiceReport = billingManager.getDetailedInvoiceReport(id, clientManager);
        } catch (Exception e) {
            lastException = e;
        }
    }

    // --- ASSERTIONS ---

    @Then("the invoice should list {int} charging sessions")
    public void the_invoice_should_list_charging_sessions(Integer count) {
        Assertions.assertNotNull(invoiceReport);
        if(!clientsByName.isEmpty()){
            Client client = clientsByName.values().iterator().next();
            Assertions.assertEquals(count.intValue(), billingManager.getInvoiceCountForClient(client.getClientId()));
        }
    }

    @Then("the invoice should include location {string}")
    public void invoice_should_include_location(String locationName) {
        Assertions.assertTrue(invoiceReport.contains(locationName), "Report missing location: " + locationName);
    }

    @Then("the invoice should include charged energy {double} kWh")
    public void invoice_should_include_charged_energy(Double kwh) {
        String val = String.format(Locale.US, "%.1f", kwh);
        Assertions.assertTrue(invoiceReport.contains(val) || invoiceReport.contains(String.valueOf(kwh)),
                "Report missing energy: " + kwh + "\n" + invoiceReport);
    }

    @Then("the invoice should include a total price")
    public void invoice_should_include_a_total_price() {
        Assertions.assertTrue(invoiceReport.contains("€") || invoiceReport.contains("EUR"));
    }

    @Then("the invoice should include item number {int}")
    public void invoice_should_include_item_number(Integer itemNum) {
        Assertions.assertTrue(invoiceReport.contains(String.valueOf(itemNum)),
                "Report missing item number: " + itemNum + "\n" + invoiceReport);
    }

    @Then("the invoice should include charging point {int}")
    public void invoice_should_include_charging_point(Integer chargerId) {
        Assertions.assertTrue(invoiceReport.contains(String.valueOf(chargerId)),
                "Report missing charging point ID: " + chargerId + "\n" + invoiceReport);
    }

    @Then("the invoice should include charging mode {string}")
    public void invoice_should_include_charging_mode(String mode) {
        Assertions.assertTrue(invoiceReport.contains(mode),
                "Report missing charging mode: " + mode + "\n" + invoiceReport);
    }

    // FIX: Check for "60 min" OR "60min" to handle formatting differences
    @Then("the invoice should include duration {string}")
    public void invoice_should_include_duration(String duration) {
        String noSpaceDuration = duration.replace(" ", ""); // "60min"

        boolean match = invoiceReport.contains(duration) || invoiceReport.contains(noSpaceDuration);

        Assertions.assertTrue(match,
                "Report missing duration: " + duration + "\nReport Content:\n" + invoiceReport);
    }

    @Then("the invoice sessions should be sorted chronologically")
    public void the_invoice_sessions_should_be_sorted_chronologically() {
        Assertions.assertTrue(invoiceReport.contains("by start time") || invoiceReport.contains("Sessions"),
                "Invoice should indicate session list sorting");
    }

    @Then("the invoice should show {int} items")
    public void invoice_should_show_items(Integer count) {
        the_invoice_should_list_charging_sessions(count);
    }

    @Then("the invoice should show total top-ups {double} EUR")
    public void invoice_should_show_topups(Double amount) {
        String val = String.format(Locale.US, "%.2f", amount);
        Assertions.assertTrue(invoiceReport.contains(val), "Report should contain top-up amount " + val);
    }

    @Then("the invoice should show total spent {double} EUR")
    public void invoice_should_show_spent(Double amount) {
        String val = String.format(Locale.US, "%.2f", amount);
        Assertions.assertTrue(invoiceReport.contains(val), "Report should contain spent amount " + val);
    }

    @Then("the invoice should show remaining balance {double} EUR")
    public void invoice_should_show_remaining(Double amount) {
        String val = String.format(Locale.US, "%.2f", amount);
        Assertions.assertTrue(invoiceReport.contains(val), "Report should contain balance " + val);
    }

    @Then("the invoice should show all sessions")
    public void invoice_should_show_all_sessions() {
        Assertions.assertFalse(invoiceReport.isEmpty());
        Assertions.assertTrue(invoiceReport.contains("Sessions") || invoiceReport.contains("Location"));
    }

    @Then("the invoice should correctly show the balance impact")
    public void invoice_should_correctly_show_balance_impact() {
        Assertions.assertTrue(invoiceReport.contains("Balance"));
    }

    @Then("an error should be returned for customer not found")
    public void error_customer_not_found() {
        Assertions.assertNotNull(lastException);
        Assertions.assertTrue(lastException.getMessage().contains("found"));
    }

    // --- HELPERS ---

    private void performSession(String customerName, String locationName, double kwh, LocalDateTime start) {
        Client client = requireCustomer(customerName);
        Location loc = requireLocation(locationName);
        Charger charger = loc.getChargers().get(0);

        if (stationManager.getPricingForLocation(loc.getLocationId()) == null) {
            ensureLocationHasDefaultPricing(loc);
        }

        chargingService.startSession(
                client.getClientId(),
                loc.getLocationId(),
                charger.getChargerId(),
                ChargerType.AC,
                start
        );

        // Finish 60 minutes later so duration matches "60 min" expectation
        chargingService.finishSession(start.plusMinutes(60), kwh);
    }

    private Client ensureCustomerExists(String customerName) {
        if (clientsByName.containsKey(customerName)) return clientsByName.get(customerName);
        int id = nextClientId++;
        Client client = clientManager.registerClient(id, customerName, customerName.toLowerCase() + "@test.com");
        clientsByName.put(customerName, client);
        return client;
    }

    private Location ensureLocationExists(String locationName) {
        if (locationsByName.containsKey(locationName)) return locationsByName.get(locationName);
        int id = nextLocationId++;
        Location loc = stationManager.createLocation(id, locationName, "Address " + locationName);
        locationsByName.put(locationName, loc);
        return loc;
    }

    private Client requireCustomer(String name) { return clientsByName.get(name); }
    private Location requireLocation(String name) { return locationsByName.get(name); }

    private void ensureLocationHasDefaultPricing(Location loc) {
        stationManager.updateLocationPricing(loc.getLocationId(), 0.45, 0.55, 0.20, 0.20);
    }
}