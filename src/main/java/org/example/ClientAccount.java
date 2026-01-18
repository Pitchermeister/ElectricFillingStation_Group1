package org.example;

public class ClientAccount {
    private double balance;

    public ClientAccount() {
        this.balance = 0.0;
    }

    public void topUp(double amount) {
        this.balance += amount;
    }

    public void debit(double amount ) {
        //if (amount > balance) throw new IllegalStateException("Insufficient balance");
        this.balance -= amount;
    }

    public double getBalance() {
        return balance;
    }
}
