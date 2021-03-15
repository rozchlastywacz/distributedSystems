package chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;


public class WriteThreadMulti {
    private MulticastSocket socket;
    private int port;
    private InetAddress group;

    public WriteThreadMulti(MulticastSocket socket, int port, InetAddress group) {
        this.socket = socket;
        this.port = port;
        this.group = group;
    }

    public void send(String message) {
        try {
            socket.leaveGroup(group);
            message = message.replace("M ", "");
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), group, port);
            socket.send(packet);
            socket.joinGroup(group);
        } catch (IOException e) {
            System.out.println("WriteThreadMulti sending failed");
        }
    }
    public void close(){
        socket.close();
    }
}
