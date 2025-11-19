package app;

import client.network.GameClient;

// the main function for client that will connect the client to server + launch the ui (not done yet)
public class ClientMain {
    public static void main(String[] args) {
        // create new Instance of GameClient class
        // for what purpose ?
        GameClient client = new GameClient(
                "localhost",
                5000,
                System.out::println // prints server messages in terminal
        );
        // if the client does not connect to server mean connect returns false -> print errrrrrrr
        if (!client.connect()) {
            System.out.println("Failed to connect to server.");
            return;
        }
        // if connected
        System.out.print("Connected! Type messages:");

        // Read input from terminal & send to server
        // scan the input from terminal then send it to server
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                client.send(line);
            }
        }
    }
}
