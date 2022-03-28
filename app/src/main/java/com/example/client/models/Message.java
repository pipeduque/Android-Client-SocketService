package com.example.client.models;

public class Message {

    // Definiciones

    // Contantes
    private final String date; // Fecha del mensaje
    private final User user; // Usuario emisor
    private final String content; // Contenido del mensaje
    private final String base64file; // String del archivo en base64

    /**
     * Crea un nuevo mensaje para un canal
     *
     * @param date       Fecha del nuevo mensaje
     * @param user       Usuario emisor del mensaje
     * @param content    Contenido del mensaje
     * @param base64file String del archivo en base64
     */
    public Message(String date, User user, String content, String base64file) {

        this.date = date;
        this.user = user;
        this.content = content;
        this.base64file = base64file;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public User getUser() {
        return user;
    }

    public String getBase64file() {
        return base64file;
    }
}
