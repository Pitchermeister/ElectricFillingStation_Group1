package org.example;

import java.util.ArrayList;
import java.util.List;

public class BillingService {

    private List<InvoiceEntry> entries;
    private int nextItemNumber;

    public BillingService() {
        this.entries = new ArrayList<>();
        this.nextItemNumber = 1;
    }

    public void addEntryFromSession(ChargingSession session, String locationName, double price) {
        int itemNo = nextItemNumber;
        nextItemNumber++;

        InvoiceEntry entry = new InvoiceEntry(
                itemNo,
                session.getClientId(),
                locationName,
                session.getChargerId(),
                session.getMode(),
                session.getStartTime(),
                session.getDurationMinutesRoundedUp(),
                session.getChargedKWh(),
                price
        );

        entries.add(entry);
    }

    public void printInvoiceForClient(int clientId, ClientManager clientManager) {

        Client client = clientManager.getClientById(clientId);
        if (client == null) {
            System.out.println("Client not found: " + clientId);
            return;
        }

        // Rechnungsposten dieses Clients ausgeben (einfach mit for-Schleife)
        System.out.println("----- Invoice for " + client.getName() + " (ClientId " + clientId + ") -----");

        boolean hasAny = false;
        for (int i = 0; i < entries.size(); i++) {
            InvoiceEntry e = entries.get(i);
            if (e.getClientId() == clientId) {
                System.out.println(e);
                hasAny = true;
            }
        }

        if (!hasAny) {
            System.out.println("(no invoice entries yet)");
        }

        System.out.println("Remaining balance: " + client.getAccount().getBalance());
        System.out.println("---------------------------------------------");
    }
}
