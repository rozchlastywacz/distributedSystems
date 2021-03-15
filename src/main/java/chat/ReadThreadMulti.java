package chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class ReadThreadMulti extends Thread {
    private final int BUFFER_SIZE = 1024;
    private Client client;
    private boolean running;
    private MulticastSocket socket;

    public ReadThreadMulti(Client client, MulticastSocket socket) {
        this.client = client;
        this.socket = socket;
        running = true;
    }

    public void run() {
        String clientPrompt = ("<" + client.getNickname() + ">: ");
        while (running) {
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket responsPacket = new DatagramPacket(buffer, BUFFER_SIZE);
                socket.receive(responsPacket);
                String response = new String(buffer, 0, responsPacket.getLength());
                String message = "\r" + "<Sent via multicast> " + response;
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
                    System.out.println("ReadThreadMulti socket closed");
                } else {
                    System.out.println("ReadThreadMulti reading message failure");
                }
                running = false;
            }

        }
    }

    public void kill() {
        running = false;
    }
}
