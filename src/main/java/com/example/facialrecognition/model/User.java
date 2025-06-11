package com.example.facialrecognition.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty status;
    private byte[] biometricData;

    public User(int id, String name, String status, byte[] biometricData) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.status = new SimpleStringProperty(status);
        this.biometricData = biometricData;
    }

    // Getters et setters pour les propriétés JavaFX
    public IntegerProperty idProperty() {
        return id;
    }

    public int getId() {
        return id.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }
    public void setId(Integer id) {
        this.id.set(id);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    // Getter et setter pour les données biométriques
    public byte[] getBiometricData() {
        return biometricData;
    }

    public void setBiometricData(byte[] biometricData) {
        this.biometricData = biometricData;
    }

    // Méthode utilitaire pour vérifier si l'utilisateur a des données biométriques
    public boolean hasBiometricData() {
        return biometricData != null && biometricData.length > 0;
    }

    @Override
    public String toString() {
        return name.get();
    }
}