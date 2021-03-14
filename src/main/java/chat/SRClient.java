package chat;

import java.io.IOException;
import java.net.Socket;

public class SRClient {
    private final String hostName = "localhost";
    private final int portNumber = 12345;
    private String nick;

    // public static final String ANSI_RESET = "\u001B[0m";
    // public static final String ANSI_BLACK = "\u001B[30m";
    // public static final String ANSI_RED = "\u001B[31m";
    // public static final String ANSI_GREEN = "\u001B[32m";
    // public static final String ANSI_YELLOW = "\u001B[33m";
    // public static final String ANSI_BLUE = "\u001B[34m";
    // public static final String ANSI_PURPLE = "\u001B[35m";
    // public static final String ANSI_CYAN = "\u001B[36m";
    // public static final String ANSI_WHITE = "\u001B[37m";
    public static void main(String[] args) throws IOException {
        new SRClient().run();
    }

    public void run() {
        try {
            // create socket
            Socket socket = new Socket(hostName, portNumber);
            nick = System.console().readLine("Say your name: ");
            ReadThread readThread = new ReadThread(this, socket);
            WriteThread writeThread = new WriteThread(this, socket);
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
