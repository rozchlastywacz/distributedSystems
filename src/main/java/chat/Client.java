package chat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class Client {
    private final String HOST_NAME = "localhost";
    private final int PORT_NUMBER = 12345;
    private final String MULTICAST = "230.0.0.0";
    private final int MULTI_PORT = 4545;
    private String nick;
    public static void main(String[] args) throws IOException {
        new Client().run();
    }

    public void run() {
        try {
            // create socket
            nick = System.console().readLine("Say your name: ");

            Socket socketTCP = new Socket(HOST_NAME, PORT_NUMBER);
            DatagramSocket socketUDP = new DatagramSocket(null);
            socketUDP.bind(new InetSocketAddress(InetAddress.getByName(HOST_NAME), socketTCP.getLocalPort()));

            MulticastSocket multicastSocket = new MulticastSocket(MULTI_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST);
            multicastSocket.joinGroup(group);
            ReadThreadMulti readThreadMulti = new ReadThreadMulti(this, multicastSocket);
            readThreadMulti.start();
            WriteThreadMulti writeThreadMulti = new WriteThreadMulti(multicastSocket, MULTI_PORT, group);
            // System.out.println("port lokalny " + socketTCP.getLocalPort());
            // System.out.println("port zdalny " + socketTCP.getPort());
            
            ReadThreadTCP readThreadTCP = new ReadThreadTCP(this, socketTCP);
            WriteThreadUDP writeThreadUDP = new WriteThreadUDP(socketUDP, socketTCP.getPort());
            WriteThreadTCP writeThreadTCP = new WriteThreadTCP(this, socketTCP, writeThreadUDP, writeThreadMulti);

            ReadThreadUDP readThreadUDP = new ReadThreadUDP(this, socketUDP, socketTCP.getLocalPort());

            readThreadTCP.start();
            writeThreadTCP.start();

            readThreadUDP.start();
            
            writeThreadTCP.join();
            readThreadTCP.kill();
            readThreadTCP.join();

            readThreadUDP.kill();
            readThreadUDP.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNickname(String nick) {
        this.nick = nick;
    }

    public String getNickname() {
        return nick;
    }
}
