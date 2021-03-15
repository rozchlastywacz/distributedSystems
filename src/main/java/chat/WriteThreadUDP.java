package chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WriteThreadUDP{
    private DatagramSocket socket;
    private int port;

    public WriteThreadUDP(DatagramSocket socket, int port) {
        this.socket = socket;
        this.port = port;
    }

    public void send(String message) {
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("localhost"), port);
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("WriteThreadUDP sending failed");
        }

    }

    public void close(){
        socket.close();
    }
}
