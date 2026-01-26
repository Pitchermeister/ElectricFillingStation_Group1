package org.example;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.Management.BillingManager;
import org.example.Management.ClientManager;
import org.example.Management.StationManager;
import org.example.domain.*;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateChargingSessionSteps {

    private StationManager stationManager;
    private ClientManager clientManager;
    private BillingManager billingManager;
    private ChargingService chargingService;

    private Exception lastException;

    private Map<String, Client> clientsByName;
    private Map<String, Location> locationsByName;
    private Map<String, Double> initialBalances;

    private int nextClientId;
    private int nextLocationId;

    @Before
    public void setup() {
        stationManager = new StationManager();
        clientManager = new ClientManager();
        billingManager = new BillingManager();
        chargingService = new ChargingService(clientManager, stationManager, billingManager);

        lastException = null;
        clientsByName = new HashMap<>();
        locationsByName = new HashMap<>();
        initialBalances = new HashMap<>();
        nextClientId = 1;
        nextLocationId = 1;
    }

    @Given("the charging service is initialized")
    public void the_system_is_initialized() {
        Assertions.assertNotNull(stationManager);
    }

    @Given("a charging customer {string} exists with balance {double} EUR")
    public void charging_customer_exists_with_balance(String name, Double balance) {
        Client client = ensureCustomerExists(name);
        client.getAccount().topUp(balance);
        initialBalances.put(name, client.getAccount().getBalance());
    }

    @Given("a charging location named {string} exists with {int} chargers")
    public void charging_location_named_exists_with_chargers(String locationName, Integer count) {
        Location loc = ensureLocationExists(locationName);
        for (int i = 0; i < count; i++) {
            int chargerId = 101 + i;

            // UPDATED: Create different chargers based on ID parity
            // Odd IDs (101, 103) -> AC, 22kW
            // Even IDs (102, 104) -> DC, 150kW
            boolean isDc = (chargerId % 2 == 0);
            ChargerType type = isDc ? ChargerType.DC : ChargerType.AC;
            double power = isDc ? 150.0 : 22.0;

            Charger charger = new Charger(chargerId, 900000 + chargerId, power, type);

            forceIntFieldIfPresent(charger, "chargerId", chargerId);
            forceIntFieldIfPresent(charger, "id", chargerId);
            stationManager.addChargerToLocation(loc.getLocationId(), charger);
        }
        // Set Default Pricing (AC & DC)
        stationManager.updateLocationPricing(loc.getLocationId(), 0.45, 0.65, 0.20, 0.20);
    }

    // UPDATED: Now accepts "AC" or "DC" dynamically
    // FIX: Changed to Regex syntax to support "a" or "an"
    @When("^the charging customer \"([^\"]*)\" starts (?:a|an) \"([^\"]*)\" charging session on charger (\\d+) at \"([^\"]*)\"$")
    public void customer_starts_session(String customerName, String typeStr, Integer chargerId, String locationName) {
        lastException = null;
        Client client = requireCustomer(customerName);
        Location loc = requireLocation(locationName);

        // Convert string "AC"/"DC" to Enum
        ChargerType mode = ChargerType.valueOf(typeStr);

        try {
            chargingService.startSession(
                    client.getClientId(),
                    loc.getLocationId(),
                    chargerId,
                    mode,
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Given("the charging customer {string} has an active session on charger {int} at {string}")
    public void customer_has_active_session(String customerName, Integer chargerId, String locationName) {
        // Defaults to AC for backward compatibility or simplistic tests
        customer_starts_session(customerName, "AC", chargerId, locationName);
    }

    @When("the charging customer {string} attempts to start a charging session on charger {int} at {string}")
    public void customer_attempts_to_start_session(String customerName, Integer chargerId, String locationName) {
        try {
            customer_starts_session(customerName, "AC", chargerId, locationName);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("the session finishes after {int} minutes with {double} kWh")
    @When("the session finishes after {int} minute with {double} kWh")
    public void session_finishes(Integer minutes, Double kwh) {
        chargingService.finishSession(LocalDateTime.now().plusMinutes(minutes), kwh);
    }

    @Then("the charging session should be active")
    public void session_should_be_active() {
        Assertions.assertTrue(chargingService.hasActiveSession());
    }

    @Then("charger {int} should be OCCUPIED")
    public void charger_should_be_occupied(Integer chargerId) {
        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertEquals(ChargerStatus.OCCUPIED, charger.getStatus());
    }

    @Then("charger {int} should be AVAILABLE")
    public void charger_should_be_available(Integer chargerId) {
        Charger charger = stationManager.findChargerById(chargerId);
        Assertions.assertEquals(ChargerStatus.IN_OPERATION_FREE, charger.getStatus());
    }

    @Then("the total session cost should be {double} EUR")
    public void total_cost_should_be(Double expected) {
        List<InvoiceEntry> entries = billingManager.getAllEntries();
        Assertions.assertFalse(entries.isEmpty());
        double actualCost = entries.get(entries.size() - 1).getPrice();
        Assertions.assertEquals(expected, actualCost, 0.01);
    }

    @Then("the charging customer {string} balance should be {double} EUR")
    public void customer_balance_should_be(String customerName, Double expected) {
        Client client = requireCustomer(customerName);
        Assertions.assertEquals(expected, client.getAccount().getBalance(), 0.01);
    }

    @Then("the session should fail with error {string}")
    public void session_should_fail_with(String message) {
        Assertions.assertNotNull(lastException);
        Assertions.assertTrue(lastException.getMessage().contains(message));
    }

    @Then("the session start should fail")
    public void session_start_should_fail() {
        Assertions.assertNotNull(lastException);
    }

    @Then("the total session cost should be greater than {int}")
    public void cost_should_be_greater_than(Integer val) {
        List<InvoiceEntry> entries = billingManager.getAllEntries();
        Assertions.assertFalse(entries.isEmpty());
        double actualCost = entries.get(entries.size() - 1).getPrice();
        Assertions.assertTrue(actualCost > val);
    }

    @Then("the charging customer {string} balance should decrease")
    public void balance_should_decrease(String name) {
        Client client = requireCustomer(name);
        double current = client.getAccount().getBalance();
        double initial = initialBalances.getOrDefault(name, current);
        Assertions.assertTrue(current < initial);
    }

    @Then("the total session cost should be calculated correctly")
    public void cost_calculated_correctly() {
        List<InvoiceEntry> entries = billingManager.getAllEntries();
        Assertions.assertFalse(entries.isEmpty());
        Assertions.assertTrue(entries.get(entries.size() - 1).getPrice() > 0);
    }

    // --- Scenario 2 Table Check (Invoice) ---
    @Then("the invoice for {string} should contain the following entry:")
    public void invoice_should_contain_entry(String customerName, DataTable dataTable) {
        Client client = requireCustomer(customerName);
        List<InvoiceEntry> entries = billingManager.getEntriesForClient(client.getClientId());

        Assertions.assertFalse(entries.isEmpty(), "No invoice entries found for client " + customerName);
        InvoiceEntry lastEntry = entries.get(entries.size() - 1);

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> data = rows.get(0);

        if (data.containsKey("Location")) Assertions.assertTrue(lastEntry.getLocationName().contains(data.get("Location")));
        if (data.containsKey("AC_or_DC")) Assertions.assertEquals(data.get("AC_or_DC"), lastEntry.getMode().toString());

        String energyRaw = data.get("Energy");
        if (energyRaw != null) {
            double expectedKWh = Double.parseDouble(energyRaw.replaceAll("[^0-9.]", ""));
            Assertions.assertEquals(expectedKWh, lastEntry.getChargedKWh(), 0.1);
        }

        String costRaw = data.get("Cost");
        if (costRaw != null) {
            double expectedCost = Double.parseDouble(costRaw.replaceAll("[^0-9.]", ""));
            Assertions.assertEquals(expectedCost, lastEntry.getPrice(), 0.01);
        }

        if (data.containsKey("Start Time") && data.get("Start Time").equals("RECENT")) {
            Assertions.assertNotNull(lastEntry.getStartTime());
        }
    }

    // --- Scenario 3 Table Check (Owner Report) ---
    @Then("the system should have the following charging entries:")
    public void system_should_have_entries(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        List<InvoiceEntry> allEntries = billingManager.getAllEntries();

        Assertions.assertTrue(allEntries.size() >= rows.size(), "Not enough entries in system");

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> expected = rows.get(i);
            InvoiceEntry actual = allEntries.get(i);

            Client client = clientsByName.get(expected.get("client"));
            Assertions.assertEquals(client.getClientId(), actual.getClientId());

            Assertions.assertTrue(actual.getLocationName().contains(expected.get("location")));
            Assertions.assertEquals(Integer.parseInt(expected.get("charger")), actual.getChargerId());
            Assertions.assertEquals(expected.get("mode"), actual.getMode().toString());

            String timeStr = expected.get("time").replaceAll("[^0-9]", "");
            Assertions.assertEquals(Long.parseLong(timeStr), actual.getDurationMinutes());

            Assertions.assertEquals(Double.parseDouble(expected.get("kWh")), actual.getChargedKWh(), 0.1);
            Assertions.assertEquals(Double.parseDouble(expected.get("cost")), actual.getPrice(), 0.01);
        }
    }

    // Helpers
    private Client ensureCustomerExists(String name) {
        if (clientsByName.containsKey(name)) return clientsByName.get(name);
        int id = nextClientId++;
        Client client = clientManager.registerClient(id, name, name.toLowerCase() + "@test.com");
        clientsByName.put(name, client);
        return client;
    }

    private Client requireCustomer(String name) {
        return clientsByName.get(name);
    }

    private Location ensureLocationExists(String name) {
        if (locationsByName.containsKey(name)) return locationsByName.get(name);
        int id = nextLocationId++;
        Location loc = stationManager.createLocation(id, name, "Address " + name);
        locationsByName.put(name, loc);
        return loc;
    }

    private Location requireLocation(String name) {
        return locationsByName.get(name);
    }

    private static void forceIntFieldIfPresent(Object target, String fieldName, int value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            if (f.getType() == int.class || f.getType() == Integer.class) f.set(target, value);
        } catch (Exception ignored) {}
    }
}