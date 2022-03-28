package com.example.client.io;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import static com.example.client.utility.Utility.SERVER_IP;
import static com.example.client.utility.Utility.SERVER_PORT;

import android.os.Binder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketService extends Service {

    // Definiciones
    // Variables
    private String request;
    private SocketThread socketThread;        // Hilo para conectarse al servidor en segundo plano
    private WriterThread writerThread;        // Hilo de escritura de solicitudes en segundo plano
    private ReaderThread readerThread;        // Hilo de lectura de respuesta en segundo plano
    private BufferedReader input;             // Canal de entrada para la respuesta del servidor
    private PrintWriter output;               // Canal de salida para enviar la solicitud al servidor
    private Socket socket;                    // Socket para crear la conexion cliente - servidor
    private static boolean isRunning = false; // Bandera que informa si el servicio esta en ejecucion

    @Override
    public void onCreate() {
        isRunning = true; // Al ejecutar el servicio ponemos en true la bandera
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        socketThread = new SocketThread(); // Iniciamos nuestro hilo para mantener la conexion con socket
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { // Cerramos todos los hilos y demas
            socket.close();
            input.close();
            output.close();
            writerThread.thread.interrupt();
            readerThread.thread.interrupt();
            socketThread.thread.interrupt();
            readerThread.thread.interrupt();

        } catch (Exception e) {
            e.printStackTrace();
        }
        socket = null;
    }

    /**
     * Nos devuelve la bandera que nos informa si el servicio esta en ejecucion
     *
     * @return bandera informativa
     */
    public static boolean isRunning() {
        return isRunning;
    }

    private final IBinder myBinder = new LocalBinder();

    /**
     * Método de acceso
     * LocalBinder extiende de Binder con el fin de retornar la instancia del servicio a través de getService().
     */
    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    /**
     * Envia una solicitud al servicio
     *
     * @param request solicitud
     */
    public void sendMessage(String request) {

        if (output != null && !output.checkError()) { //Manejamos que este inciado el escritor de solicitudes

            this.request = request; // Asignamos la solicitud

            if (writerThread != null) { // Manejamos si se encuentra el hilo escritor en uso para reiniciarlo

                // Interrumpimos el hilo lector y escritor
                readerThread.thread.interrupt();
                writerThread.thread.interrupt();
                // Volemos a inciar el hilo lector y escritor para enviar la nueva solicitud
                readerThread = new ReaderThread();
                writerThread = new WriterThread(request);
            } else {

                // Si es la primera vez en ejecucion iniciamos el hilo
                writerThread = new WriterThread(request);
                readerThread = new ReaderThread();

            }
        }
    }

    /**
     * Enviador la respuesta del servicio a las actividades
     *
     * @param data respuesta del servicio
     */
    private void sendResponseToActivity(String[] data) {

        // Dependiendo de la solicitud enviaremos una respuesta a quien corresponda mediante un intent
        Intent intent = null;
        if (request.contains("LIST_CHN")) {
            intent = new Intent("response_chn");
            intent.putExtra("data_chn", data);

        } else if (request.contains("LIST_MSG")) {
            intent = new Intent("response_msg");
            intent.putExtra("data_msg", data);

        } else if (request.contains("LIST_USR")) {
            intent = new Intent("response_usr");
            intent.putExtra("data_usr", data);
        }

        if (intent != null) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent); // Enviamos la respuesta
        }
    }

    /**
     * Hilo para para crear la conexion cliente - servidor a traves del Socket
     */
    private class SocketThread implements Runnable {

        private final Thread thread;

        SocketThread() {
            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {

            socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 3000); // Socket para crear la conexion cliente - servidor
                output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true); // Instanciamos el canal de salida
                input = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Instanciamos el canal de entrada
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Hilo para para leer las respuestas del servidor a traves del input en segundo plano
     */
    class ReaderThread implements Runnable {

        private final Thread thread;

        ReaderThread() {
            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {

            try {
                StringBuilder finalString = new StringBuilder();
                String[] data;

                while (true) { // leer datos del servidor

                    String line = input.readLine();
                    finalString.append(line);
                    String response = finalString.toString();

                    if (response.contains("FALSE")) {

                        data = new String[]{"FALSE"};
                    } else {
                        data = response.split(";"); // Respuesta del servidor
                    }
                    sendResponseToActivity(data); // Enviamos la respuesta del servidor a la actividad mediante el servicio
                    if (line != null) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Hilo para para escribir solicitudes al servidor a traves del output
     */
    class WriterThread implements Runnable {
        private final String message;
        private final Thread thread;

        WriterThread(String message) {

            this.message = message;
            thread = new Thread(this);
            thread.start();

        }

        @Override
        public void run() {
            output.println(message);
            output.flush();
        }
    }
}




