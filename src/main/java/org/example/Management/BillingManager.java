package org.example.Management;

// UPDATED IMPORT: Pointing to the inner class
import org.example.domain.ChargingService.ChargingSession;
import org.example.domain.Client;
import org.example.domain.InvoiceEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BillingManager {
    private final List<InvoiceEntry> entries = new ArrayList<>();
    private int nextItemNumber = 1;

    // CREATE
    public InvoiceEntry createEntryFromSession(ChargingSession session, String locationName) {
        InvoiceEntry entry = session.toInvoiceEntry(nextItemNumber++, locationName);
        entries.add(entry);
        return entry;
    }

    // READ
    public InvoiceEntry getEntryByItemNumber(int itemNumber) {
        for (InvoiceEntry e : entries) {
            if (e.getItemNumber() == itemNumber) return e;
        }
        return null;
    }

    public List<InvoiceEntry> getEntriesForClient(int clientId) {
        List<InvoiceEntry> result = new ArrayList<>();
        for (InvoiceEntry e : entries) {
            if (e.getClientId() == clientId) result.add(e);
        }
        return result;
    }

    public List<InvoiceEntry> getAllEntries() {
        return new ArrayList<>(entries);
    }

    // STATISTICS
    public double getTotalSpentForClient(int clientId) {
        double total = 0;
        for (InvoiceEntry e : getEntriesForClient(clientId)) total += e.getPrice();
        return total;
    }

    public int getInvoiceCountForClient(int clientId) {
        return getEntriesForClient(clientId).size();
    }

    // INVOICE REPORT
    public String getDetailedInvoiceReport(int clientId, ClientManager clientManager) {
        Client client = clientManager.getClientById(clientId);
        if (client == null) return "Client not found: " + clientId;

        StringBuilder sb = new StringBuilder("=== INVOICE REPORT ===\n\n");
        sb.append("Client: ").append(client.getName()).append(" (").append(client.getEmail()).append(")\n\n");

        double spent = getTotalSpentForClient(clientId);
        double balance = client.getAccount().getBalance();

        sb.append("Balance: Top-ups EUR ").append(String.format("%.2f", spent + balance))
                .append(" | Spent EUR ").append(String.format("%.2f", spent))
                .append(" | Remaining EUR ").append(String.format("%.2f", balance)).append("\n\n");

        List<InvoiceEntry> sorted = getEntriesForClient(clientId);
        if (sorted.isEmpty()) return sb.append("No sessions.\n").toString();

        sorted.sort(Comparator.comparing(InvoiceEntry::getStartTime));

        sb.append("Sessions (by start time):\n");
        double totalKWh = 0;
        for (InvoiceEntry e : sorted) {
            sb.append("  #").append(e.getItemNumber()).append(" | ").append(e.getLocationName())
                    .append(" | Ch").append(e.getChargerId()).append(" | ").append(e.getMode())
                    .append(" | ").append(e.getDurationMinutes()).append("min | ")
                    .append(String.format("%.1f", e.getChargedKWh())).append("kWh | EUR ")
                    .append(String.format("%.2f", e.getPrice())).append("\n");
            totalKWh += e.getChargedKWh();
        }

        sb.append("\nTotal: ").append(sorted.size()).append(" sessions | Avg EUR ")
                .append(String.format("%.2f", spent / sorted.size())).append(" | Avg ")
                .append(String.format("%.1f", totalKWh / sorted.size())).append("kWh\n");

        return sb.toString();
    }

    @Override
    public String toString() {
        return "=== BILLING (" + entries.size() + " invoices) ===";
    }
}