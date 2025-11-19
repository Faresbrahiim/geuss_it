package client.network;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

// what is the purpose of game client class  ?
// 1 ? connect to server + reads msgs + sends mssgs + notify ui
public class GameClient {
    // host -> will be localhost in our case
    private final String host;
    // the port number can be any number in avaibble range
    // range user because the port and host assigned once
    private final int port;
    // TCP socket user to connect the server
    private Socket socket;
    // read incoming msgs
    private BufferedReader in;
    //  send msg via socket
    // (exchanging msgs  by bites) + via network
    private PrintWriter out;

    // to not block the process -> handle multiple client
    private Thread listenThread;

    // callback to send messages to Controller
    // consumer  -> method the accept string and returns nothing
    private Consumer<String> messageCallback;
    // constructor for the object -> host + port + callback will affected in the calling or creating instance of this class
    public GameClient(String host, int port, Consumer<String> callback) {
        this.host = host;
        this.port = port;
        this.messageCallback = callback;
    }
    // this connect the client with the server
    public boolean connect() {
        try {
            // socket object for connection ....
            socket = new Socket(host, port);
            // getIputstream -> byte by byte + input stream reader + not byte but chars it convert it in other word
            //  buffer reader -> read many chars at once -> thanks to readline -> it reads till it went to \n new line
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // out -> sends the  msg to the server
            // inputstream for receiving data
            // output stream for sending data.....
            out = new PrintWriter(socket.getOutputStream(), true);

            startListening();
            // returns true (connected sussssiiiiflulyy)
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    private void startListening() {
        // a thread is a mini program inside a program
        // so we can run multiple threads once
        // in this case we use thread -> to listen to incoming msgs but without blocking main idea
        // that's what we call parallelisme execution and not conncurency execution
        // this just create the instance and not run it
        listenThread = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    // forward to controller ui....
                    messageCallback.accept(msg);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server.");
            }
        });
        // when main thread end -> like main , just end this thread also + no need for cntrl c
        // start ,,, it start the thread ....
        listenThread.setDaemon(true);
        listenThread.start();
    }

    public void send(String message) {
        if (out != null) {
            // sends msg to server imediatly
            out.println(message);
        }
    }
    // close socker + stop the thread
    public void disconnect() {
        try {
            if (socket != null) socket.close();
            if (listenThread != null) listenThread.interrupt();
        } catch (IOException ignored) {}
    }
}
