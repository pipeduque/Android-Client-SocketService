package com.example.client.models;

public class User {

    // Definiciones

    // Contantes
    private final String address; // Direccion ip del usuario

    /**
     * Nuevo usuario para el servidor
     *
     * @param address Direccion ip del nuevo usuario
     */
    public User(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
