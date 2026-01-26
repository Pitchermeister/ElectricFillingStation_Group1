package org.example.Management;

import org.example.domain.Client;
import org.example.domain.ChargingService.ChargingSession;

import java.util.ArrayList;
import java.util.List;

public class ClientManager {
    private List<Client> clientDatabase = new ArrayList<>();
    private int id = 1;

    // CREATE
    public Client registerClient(String name, String email) {
        Client newClient = new Client(this.id, name, email);
        clientDatabase.add(newClient);
        this.id += 1;
        return newClient;
    }

    public Client registerClient(int id, String name, String email) {
        Client newClient = new Client(id, name, email);
        clientDatabase.add(newClient);
        return newClient;
    }

    // READ
    public Client getClientById(int id) {
        return clientDatabase.stream()
                .filter(c -> c.getClientId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Client> getAllClients() {
        return new ArrayList<>(clientDatabase);
    }

    // UPDATE
    public void updateClient(int id, String name, String email) {
        Client client = getClientById(id);
        if (client != null) {
            client.setName(name);
            client.setEmail(email);
        }
    }

    // DELETE (UPDATED: Throws exceptions for tests)
    public void deleteClient(int id, ChargingManager chargingManager) {
        Client client = getClientById(id);
        if (client == null) {
            throw new IllegalArgumentException("Client not found: " + id);
        }

        // 1) Cannot delete if balance > 0
        if (client.getAccount() != null && client.getAccount().getBalance() > 0) {
            throw new IllegalStateException("Client cannot be deleted (balance > 0): " + id);
        }

        // 2) Cannot delete if currently charging
        if (chargingManager == null) {
            throw new IllegalStateException("Client cannot be deleted (chargingManager not set)");
        }

        boolean isCharging = chargingManager.getSessionsByClientId(id).stream()
                .anyMatch(session -> !session.isFinished());

        if (isCharging) {
            throw new IllegalStateException("Client cannot be deleted (currently charging): " + id);
        }

        clientDatabase.remove(client);
        System.out.println("Client deleted: " + id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ClientManager ===\n");
        sb.append("Total Clients: ").append(clientDatabase.size()).append("\n");
        for (Client client : clientDatabase) {
            sb.append("- ID: ").append(client.getClientId())
                    .append(" | Name: ").append(client.getName())
                    .append(" | Email: ").append(client.getEmail())
                    .append(" | Balance: EUR ").append(client.getAccount().getBalance())
                    .append("\n");
        }
        return sb.toString();
    }
}