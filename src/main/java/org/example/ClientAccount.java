package org.example;

public class ClientAccount {
    private double balance;

    public ClientAccount() {
        this.balance = 0.0;
    }

    public void topUp(double amount) {
        this.balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}
