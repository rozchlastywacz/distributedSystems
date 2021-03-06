package chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerUDPThread extends Thread {
    private final int BUFFER_SIZE = 1024;
    private boolean running;
    private DatagramSocket serverSocketUDP;
    private Server server;
    private String nick;

    public ServerUDPThread(DatagramSocket serverSocketUDP, Server server) {
        running = true;
        this.server = server;
        this.serverSocketUDP = serverSocketUDP;
    }

    public void run() {
        try {
            while (running) {
                byte[] receiveBuffer = new byte[BUFFER_SIZE];

                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocketUDP.receive(receivePacket);

                String message = new String(receiveBuffer, 0, receivePacket.getLength()).replace("U ", "");

                if (message.equals("quit")) {
                    running = false;
                } else {
                    server.sendToOthersViaUDP("<Server via UDP> " + message, receivePacket.getAddress(),
                            receivePacket.getPort());
                }
            }

        } catch (Exception e) {
            System.out.println("Client thread UDP communication finished");
        } finally {
            if (serverSocketUDP != null) {
                serverSocketUDP.close();
                System.out.println("Server socket UDP closed");
            }
        }
    }

    public void send(String message, InetAddress address, int port) {
        try {
            serverSocketUDP.send(new DatagramPacket(message.getBytes(), message.length(), address, port));
        } catch (IOException e) {
            System.out.println("UDP Thread resend failed");
        }
        ;
    }

    public void kill() {
        running = false;
    }

    public String getNick() {
        return this.nick;
    }

}
