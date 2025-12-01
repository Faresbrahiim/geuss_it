package client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class GameClient {

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenThread;
    private Consumer<String> messageCallback;

    private String username;

    public GameClient(String host, int port, Consumer<String> callback) {
        this.host = host;
        this.port = port;
        this.messageCallback = callback;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            startListening();
            System.out.println("✅ Connected to server!");
            return true;
        } catch (IOException e) {
            System.err.println("❌ Failed to connect: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void startListening() {
        listenThread = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (messageCallback != null) {
                        messageCallback.accept(msg);
                    }
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server.");
            }
        });

        listenThread.setDaemon(true);
        listenThread.start();
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("📤 Sent: " + message);
        }
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
            if (listenThread != null) listenThread.interrupt();
        } catch (IOException ignored) {}
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        System.out.println("✅ Client username set to: '" + username + "'");
    }

    public void setMessageCallback(Consumer<String> callback) {
        this.messageCallback = callback;
    }
}