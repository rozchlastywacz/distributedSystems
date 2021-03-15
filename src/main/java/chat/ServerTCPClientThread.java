package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ServerTCPClientThread extends Thread {
    private boolean running;
    private Socket clientSocket;
    private Server server;
    private String nick;
    private PrintWriter out;

    public ServerTCPClientThread(Socket clientSocket, Server server) {
        running = true;
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            nick = in.readLine();
            System.out.println("Received nick: " + nick);
            send("<Server>: Hello, " + nick + " !");
            server.sendUserConnectedMessage(this, nick);
            String message;
            while (running) {
                message = in.readLine();
                if (message.equals("quit")) {
                    running = false;
                } else {
                    server.sendToOthersViaTCP(this, "<" + nick + ">: " + message);
                }
            }

        } catch (Exception e) {
            System.out.println("Client thread communication failure");
        } finally {
            server.removeUser(this);
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Client thread socket closing failure");
                }
            }
            server.sendUserDisconnectedMessage(nick);
        }
    }

    public void send(String message) {
        out.println(message);
    }

    public void kill() {
        running = false;
    }

    public String getNick() {
        return this.nick;
    }

    public int getPort() {
        return clientSocket.getPort();
    }

    public InetAddress gInetAddress() {
        return clientSocket.getInetAddress();
    }

}
