package chat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private final String hostName = "localhost";
    private final int portNumber = 12345;
    private String nick;
    public static void main(String[] args) throws IOException {
        new Client().run();
    }

    public void run() {
        try {
            // create socket
            nick = System.console().readLine("Say your name: ");

            Socket socketTCP = new Socket(hostName, portNumber);
            DatagramSocket socketUDP = new DatagramSocket(null);
            socketUDP.bind(new InetSocketAddress(InetAddress.getByName(hostName), socketTCP.getLocalPort()));
            
            // System.out.println("port lokalny " + socketTCP.getLocalPort());
            // System.out.println("port zdalny " + socketTCP.getPort());
            
            ReadThreadTCP readThreadTCP = new ReadThreadTCP(this, socketTCP);
            WriteThreadUDP writeThreadUDP = new WriteThreadUDP(socketUDP, socketTCP.getPort());
            WriteThreadTCP writeThreadTCP = new WriteThreadTCP(this, socketTCP, writeThreadUDP);

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
