package org.example.entity;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private float amount;
    private String transactionId;
    private LocalDateTime transactionDate;
    private Apprenant apprenant;
    private String status;

    // Constructeur
    public Transaction(int id, float amount, String transactionId, LocalDateTime transactionDate, Apprenant apprenant, String status) {
        this.id = id;
        this.amount = amount;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.apprenant = apprenant;
        this.status = status;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Apprenant getApprenant() {
        return apprenant;
    }

    public void setApprenant(Apprenant apprenant) {
        this.apprenant = apprenant;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

