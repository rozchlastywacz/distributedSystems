package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReadThreadTCP extends Thread {
    private BufferedReader reader;
    private Client client;
    private boolean running;
    private Socket socket;

    public ReadThreadTCP(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
        running = true;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            System.out.println("ReadThread constructor reader failure");
        }
    }

    public void run() {
        String clientPrompt = ("<" + client.getNickname() + ">: ");
        while (running) {
            try {
                String response = reader.readLine();
                String message = "\r" + response;
                if (message.length() < clientPrompt.length()) {
                    message = message
                            + new String(new char[clientPrompt.length() - message.length()]).replace('\0', ' ');
                }
                System.out.println(message);
                if (client.getNickname() != null) {
                    System.out.print(clientPrompt);
                }
            } catch (IOException e) {
                if (socket.isClosed()) {
                    System.out.println("ReadThreadTCP socket closed");
                } else {
                    System.out.println("ReadThreadTCP reading message failure");
                }
                running = false;
            }

        }
    }

    public void kill() {
        running = false;
    }
}
