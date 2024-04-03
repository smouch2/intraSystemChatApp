import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    private static final int PORT = 8989;
    //threadsafe list for all clients
    static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws IOException {
        //create server socket on prescribed port
        ServerSocket serverSocket = new ServerSocket(PORT);

        //listen continuously for client connections
        while (true) {
            //when a client connects to the socket accept it
            Socket socket = serverSocket.accept();
            //create a new client handler to deal with it
            ClientHandler clientHandler = new ClientHandler(socket);
            //add client to the AllUsers list
            clients.add(clientHandler);
            //pass the clienthandler to a new thread dedicated to the client
            new Thread(clientHandler).start();
        }
    }
    //method for printing server-console messages
    public static void printServerMessage(String message){
        System.out.println(message);
    }
    //method for broadcasting messages to both server and client consoles
    public static void broadcastMessage(String message) {
        System.out.println(getTimeStamp() + " " + message); // Print the message being broadcasted
        for (ClientHandler aClient : clients) {
            aClient.sendMessage(message);
        }
    }

    public static String getTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss ");
        return formatter.format(new Date());
    }


    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}
