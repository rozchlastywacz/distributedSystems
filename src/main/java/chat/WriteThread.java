package chat;

import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class WriteThread extends Thread {
    private PrintWriter writer;
    private Client client;
    private Socket socket;
    private boolean running;
    private WriteUDP writeUDP;
    private WriteMulti writeMulti;

    public WriteThread(Client client, Socket socket, WriteUDP writeUDP, WriteMulti writeMulti) {
        this.client = client;
        this.socket = socket;
        running = true;
        this.writeUDP = writeUDP;
        this.writeMulti = writeMulti;
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
            } else if (message.contains("M")) {
                writeMulti.send(message);
            } else {
                writer.println(message);
            }
            if (message.equals("quit")) {
                running = false;
            }
        }
        try {
            socket.close();
            writeUDP.close();
            writeMulti.close();
        } catch (IOException e) {
            System.out.println("WriteThread closing socket failure");
        }
    }
}
