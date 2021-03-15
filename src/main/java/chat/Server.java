package chat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    final int PORT = 12345;
    private Set<ServerTCPClientThread> clients;
    private ServerUDPClientThread UDPThread;
    public Server(){
        clients = new HashSet<>();
    }

    public void run(){
        System.out.println("Arkadiusz Cwikla - chat");
        System.out.println(
            "-------------- INFO START -----------------------\n" +
            "1. [message] + ENTER is sent via TCP\n" +
            "2. U [message] + ENTER is sent via UDP\n" +
            "3. M [message] + ENTER is sent via UDP multicast\n" +
            "-------------- INFO END --------------------------" 
        );
        ServerSocket serverSocketTCP = null;
        DatagramSocket serverSocketUDP = null;
        Socket clientSocket = null;
        
        try {
            // create socket
            serverSocketUDP = new DatagramSocket(PORT);
            UDPThread = new ServerUDPClientThread(serverSocketUDP, this);
            UDPThread.start();
            serverSocketTCP = new ServerSocket(PORT);
            while(true){
                try {
                    // accept client
                    clientSocket = serverSocketTCP.accept();
                    System.out.println("Client connected");
                    
                } catch (Exception e) {
                    System.out.println("Client socket failure");
                } 
                // start new thread for client 
                ServerTCPClientThread clientThread = new ServerTCPClientThread(clientSocket, this);
                clientThread.start();
                clients.add(clientThread);
            }
        } catch (IOException e) {
            System.out.println("Server connection failure");
        }
        finally {
            System.out.println("Server closing");
            if (serverSocketTCP != null){
                try {
                    serverSocketTCP.close();
                } catch (IOException e) {
                    System.out.println("Server socket closing failure");
                }
            }
            clients.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    System.out.println("Server joining client threads failure");
                }
            });
        }
    }

    public void removeUser(ServerTCPClientThread user){
        clients.remove(user);
    }

    public void sendToOthersViaTCP(ServerTCPClientThread sender, String message){
        clients.stream()
                .filter(client -> !client.equals(sender))
                .forEach(client -> client.send(message));
    }

    public void sendToOthersViaUDP(String message, InetAddress address, int port){
        clients.stream()
                .filter(client -> client.getPort()!=port)
                .forEach(client -> UDPThread.send(message, address, client.getPort()));
    }

    public void sendUserDisconnectedMessage(String nick){
        String msg = "<Server>: " + nick + " has been disconnected";
        System.out.println(msg);
        clients.stream()
                .forEach(client -> client.send(msg));
    }
    public void sendUserConnectedMessage(ServerTCPClientThread sender, String nick){
        clients.stream()
                .filter(client -> !client.equals(sender))
                .forEach(client -> client.send("<Server>: " + nick + " has been connected"));
    }
}
