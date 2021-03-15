package chat;

import java.io.IOException;
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
            Socket socket = new Socket(hostName, portNumber);
            nick = System.console().readLine("Say your name: ");
            ReadThreadTCP readThread = new ReadThreadTCP(this, socket);
            WriteThreadTCP writeThread = new WriteThreadTCP(this, socket);
            readThread.start();
            writeThread.start();
            writeThread.join();
            readThread.kill();
            readThread.join();

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
