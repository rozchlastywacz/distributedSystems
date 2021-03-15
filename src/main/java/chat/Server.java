package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    final int PORT = 12345;
    private Set<ServerTCPClientThread> clients;
    public Server(){
        clients = new HashSet<>();
    }

    public void run(){
        System.out.println("JAVA TCP SERVER");
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        
        try {
            // create socket
            serverSocket = new ServerSocket(PORT);
            while(true){
                try {
                    // accept client
                    clientSocket = serverSocket.accept();
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
            if (serverSocket != null){
                try {
                    serverSocket.close();
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
