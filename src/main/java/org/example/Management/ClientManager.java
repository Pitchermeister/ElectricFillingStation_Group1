package org.example.Management;

import org.example.domain.Client;

import java.util.ArrayList;
import java.util.List;

public class ClientManager {
    private List<Client> clientDatabase = new ArrayList<>();

    // CREATE
    public Client registerClient(int id, String name, String email) {
        Client newClient = new Client(id, name, email);
        clientDatabase.add(newClient);
        System.out.println("Client registered: " + name);
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
            System.out.println("Client updated: " + id);
        } else {
            System.out.println("Client not found: " + id);
        }
    }

    // DELETE
    public void deleteClient(int id) {
        Client client = getClientById(id);
        if (client != null) {
            clientDatabase.remove(client);
            System.out.println("Client deleted: " + id);
        } else {
            System.out.println("Client not found: " + id);
        }
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
