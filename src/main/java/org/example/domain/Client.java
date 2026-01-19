package org.example.domain;

public class Client {
    private int clientId;
    private String name;
    private String email;
    private ClientAccount account;

    public Client(int id, String name, String email) {
        this.clientId = id;
        this.name = name;
        this.email = email;
        this.account = new ClientAccount();
    }

    // Getters
    public int getClientId() { return clientId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public ClientAccount getAccount() { return account; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
}

