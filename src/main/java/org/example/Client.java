package org.example;

public class Client {
    private int clientId;        // Renamed from customerId
    private String name;
    private String email;
    private ClientAccount account; // Renamed from CustomerAccount

    public Client(int id, String name, String email) {
        this.clientId = id;
        this.name = name;
        this.email = email;
        this.account = new ClientAccount(); // Starts with empty account
    }

    public int getClientId() { return clientId; }
    public String getName() { return name; }
    public ClientAccount getAccount() { return account; }
}
