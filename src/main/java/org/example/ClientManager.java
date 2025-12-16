package org.example;

import java.util.ArrayList;
import java.util.List;

public class ClientManager {
    // This list acts as your "Database"
    private List<Client> clientDatabase = new ArrayList<>();

    // Logic: Register a new client
    // This matches your MVP Story: "Create Client"
    public Client registerClient(int id, String name, String email) {
        Client newClient = new Client(id, name, email);
        clientDatabase.add(newClient);
        System.out.println("Client registered: " + name);
        return newClient;
    }

    // Logic: Helper to find a client by ID (useful for login/validation)
    public Client getClientById(int id) {
        return clientDatabase.stream()
                .filter(c -> c.getClientId() == id)
                .findFirst()
                .orElse(null);
    }
}
