package com.example.client.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Repositorio ficticio de leads
 */
public class AppRepository {

    // Definiciones

    // Contantes
    private final HashMap<String, Channel> mapChannel = new HashMap<>(); // Mapa de los canales, key: nombreCanal value: Canal

    private static final AppRepository repository = new AppRepository(); //  //Repositorio de datos para la aplicacion

    /**
     * Obtiene la instancia del repositorio para ser usada por el servidor
     *
     * @return instancia del repositorio
     */
    public static AppRepository getInstance() {
        return repository;
    }

    /**
     * Guarda los nuevos canales en la aplicacion
     *
     * @param channels lista de canales proporcionada por el servidor
     */
    public void saveChannels(String[] channels) {

        for (String s : channels) { // Recorremos canal por canal de la lista de canales proporcionada por el servidor
            String[] channel = s.split(","); // Cortamos los datos del canal por comas

            if (!mapChannel.containsKey(channel[1])) { // Manejamos que el nombre del canal no exista en la aplicacion
                mapChannel.put(channel[1], new Channel(channel[1])); // Guardamos el nuevo canal
            }
        }
    }

    /**
     * Asigna los mensajes a el canal correspondiente
     *
     * @param idChannel Canal donde se asignaran los mensajes
     * @param messages  Mensajes proporcionados por el servidor
     */
    public void setMessagesChannel(String idChannel, String[] messages) {

        Objects.requireNonNull(mapChannel.get(idChannel)).clearMessages(); //Limpiamos los mensajes existentes para el canal

        for (String s : messages) { // Recorremos mensaje por mensaje de la lista de mensajes proporcionada por el servidor
            String[] message = s.split(","); // Cortamos los datos del mensaje por comas
            Objects.requireNonNull(mapChannel.get(idChannel)).setMessage(message[0], message[1], message[2], message[3]); // Asignamos el nuevo mensaje
        }
    }

    /**
     * Asigna los usuarios a el canal correspondiente
     *
     * @param idChannel Canal donde se asignaran los mensajes
     * @param users     Usuarios proporcionados por el servidor
     */
    public void setUsersChannel(String idChannel, String[] users) {

        Objects.requireNonNull(mapChannel.get(idChannel)).clearUsers(); //Limpiamos los usuarios existentes para el canal

        for (String s : users) { // Recorremos usuario por usuario de la lista de usuarios proporcionada por el servidor
            Objects.requireNonNull(mapChannel.get(idChannel)).setUsers(s); // Asignamos el nuevo usuario al canal
        }
    }

    public List<Channel> getChannels() {
        return new ArrayList<>(this.mapChannel.values());
    }

    public Channel getChannel(String name) {
        return mapChannel.get(name);
    }
}