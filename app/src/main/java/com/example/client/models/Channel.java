package com.example.client.models;

import java.util.ArrayList;
import java.util.List;

public class Channel {

    // Definiciones

    // Contantes
    private final String name; // Nombre del canal
    private final List<User> users; // Usuarios pertenecientes a un canal
    private final List<Message> messages; // Lista de mensajes del canal

    /**
     * Crea un nuevo canal en el servidor
     *
     * @param name Nombre del nuevo canal
     */
    public Channel(String name) {
        this.name = name;
        this.messages = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Message> getListMessage() {
        return messages;
    }

    public List<User> getListUsers() {
        return users;
    }

    public void setUsers(String address) {
        this.users.add(new User(address));
    }

    public void setMessage(String date, String userAddress, String content, String file) {
        this.messages.add(new Message(date, new User(userAddress), content, file));
    }

    public void clearMessages() {
        messages.clear();
    }

    public void clearUsers() {
        users.clear();
    }
}
