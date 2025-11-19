package app;

import client.network.GameClient;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        GameClient client = new GameClient(
                "localhost",
                5000,
                System.out::println // prints server messages in terminal
        );

        if (!client.connect()) {
            System.out.println("Failed to connect to server.");
            return;
        }

        System.out.println("Connected! Type messages or guesses (GUESS:<word>):");

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                client.send(line);
            }
        }
    }
}
