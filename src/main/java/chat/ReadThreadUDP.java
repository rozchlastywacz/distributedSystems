package chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class ReadThreadUDP extends Thread{
    private final int BUFFER_SIZE = 1024;
    private Client client;
    private boolean running;
    private DatagramSocket socket;
    private int port;

    public ReadThreadUDP(Client client, DatagramSocket socket, int port) {
        this.client = client;
        this.socket = socket;
        running = true;
        this.port = port;
    }

    public void run() {
        String clientPrompt = ("<" + client.getNickname() + ">: ");
        while (running) {
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket responsPacket = new DatagramPacket(buffer, BUFFER_SIZE, socket.getInetAddress(), port);
                socket.receive(responsPacket);
                String response = new String(buffer, 0, responsPacket.getLength());
                String message = "\r" + response;
                if(message.length() < clientPrompt.length()){
                    message = message + new String(new char[clientPrompt.length()-message.length()]).replace('\0', ' ');
                }
                System.out.println(message);
                if (client.getNickname() != null) {
                    System.out.print(clientPrompt);
                }
            } catch (IOException e) {
                if (socket.isClosed()) {
                    System.out.println("ReadThreadUDP socket closed");
                } else {
                    System.out.println("ReadThread reading message failure");
                }
                running = false;
            }

        }
    }

    public void kill() {
        running = false;
    }
}
