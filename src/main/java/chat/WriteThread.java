package chat;

import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class WriteThread extends Thread {
    private PrintWriter writer;
    private SRClient client;
    private Socket socket;
    private boolean running;

    public WriteThread(SRClient client, Socket socket) {
        this.client = client;
        this.socket = socket;
        running = true;
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
        while (running) {

            message = cmdReader.readLine("<" + client.getNickname() + ">: ");
            writer.println(message);
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
