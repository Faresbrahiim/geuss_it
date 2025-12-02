package client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class GameClient {

    // localhost 
    private final String host;
    // 5000
    private final int port;
    private Socket socket;
    // to read
    private BufferedReader in;
    // to write
    private PrintWriter out;
    // multiple task at once
    private Thread listenThread;
    // Callback to handle received messages
    private Consumer<String> messageCallback;

    private String username;

    public GameClient(String host, int port, Consumer<String> callback) {
        this.host = host;
        this.port = port;
        this.messageCallback = callback;
    }
    // connect 
    public boolean connect() {
        try {
            // connect to server
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            startListening();
            System.out.println("Connected to server!");
            return true;
        } catch (IOException e) {
            System.err.println(" Failed to connect: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void startListening() {
        // The code inside () -> { ... } is called a lambda 
        // we use thread because we have many player
        // put this in the main thread (UI thread), your program will freeze until a message comes.
        listenThread = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (messageCallback != null) {
                        System.out.println(msg + "*******************************************");
                        messageCallback.accept(msg); // // call the callback will be in gameController
                    }
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server.");
            }
        });
        // If the main program finishes, this thread will automatically stop.
        listenThread.setDaemon(true);
        // start thread
        listenThread.start();
    }

    // send msg to server
    public void send(String message) {
        if (out != null) {
            out.println(message);
            System.out.println(" Sent: " + message);
        }
    }
    // player remove
    public void disconnect() {
        try {
            if (socket != null) socket.close();
            if (listenThread != null) listenThread.interrupt();// Stops the listening thread
        } catch (IOException ignored) {}
    }
    // get username
    public String getUsername() {
        return username;
    }
    // set the username
    public void setUsername(String username) {
        this.username = username;
        System.out.println(" Client username set to: '" + username + "'");
    }

    public void setMessageCallback(Consumer<String> callback) {
        this.messageCallback = callback;
    }
}