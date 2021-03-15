package chat;

import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WriteThreadTCP extends Thread {
    private PrintWriter writer;
    private Client client;
    private Socket socket;
    private boolean running;
    private WriteThreadUDP writeUDP;

    public WriteThreadTCP(Client client, Socket socket, WriteThreadUDP writeUDP) {
        this.client = client;
        this.socket = socket;
        running = true;
        this.writeUDP = writeUDP;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println("WriteThread PrintWriter failure");
        }
    }

    public void run() {
        Console cmdReader = System.console();
        try {
            writer.println(client.getNickname());
        } catch (Exception e) {
            System.out.println("WriteThread sending nick failure");
        }
        String message;
        String clientPrompt = ("<" + client.getNickname() + ">: ");
        while (running) {
            message = cmdReader.readLine(clientPrompt);

            if (message.contains("U")) {
                writeUDP.send(message);
            } else {
                writer.println(message);
            }
            if (message.equals("quit")) {
                running = false;
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("WriteThread closing socket failure");
        }
    }
}
