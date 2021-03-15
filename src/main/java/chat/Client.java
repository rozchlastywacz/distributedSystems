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
            nick = System.console().readLine("Say your name: ");

            Socket socketTCP = new Socket(HOST_NAME, PORT_NUMBER);
            ReadThreadTCP readThreadTCP = new ReadThreadTCP(this, socketTCP);

            DatagramSocket socketUDP = new DatagramSocket(null);
            socketUDP.bind(new InetSocketAddress(InetAddress.getByName(HOST_NAME), socketTCP.getLocalPort()));
            WriteUDP writeUDP = new WriteUDP(socketUDP, socketTCP.getPort());
            ReadThreadUDP readThreadUDP = new ReadThreadUDP(this, socketUDP, socketTCP.getLocalPort());

            MulticastSocket multicastSocket = new MulticastSocket(MULTI_PORT);
            InetAddress group = InetAddress.getByName(MULTICAST);
            multicastSocket.joinGroup(group);
            ReadThreadMulti readThreadMulti = new ReadThreadMulti(this, multicastSocket);
            WriteMulti writeMulti = new WriteMulti(multicastSocket, MULTI_PORT, group);

            WriteThread writeThread = new WriteThread(this, socketTCP, writeUDP, writeMulti);

            execute(readThreadTCP, readThreadUDP, readThreadMulti, writeThread);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void execute(ReadThreadTCP readThreadTCP, ReadThreadUDP readThreadUDP, ReadThreadMulti readThreadMulti,
            WriteThread writeThread) throws InterruptedException {
        writeThread.start();
        readThreadTCP.start();
        readThreadUDP.start();
        readThreadMulti.start();

        writeThread.join();

        readThreadTCP.kill();
        readThreadTCP.join();

        readThreadUDP.kill();
        readThreadUDP.join();

        readThreadMulti.kill();
        readThreadMulti.join();
    }

    public void setNickname(String nick) {
        this.nick = nick;
    }

    public String getNickname() {
        return nick;
    }
}
