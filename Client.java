import java.io.*;
import java.net.*;

public class Client {
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        //establish connection to the server via socket
        socket = new Socket(host, port);

        //output stream to server
        out = new PrintWriter(socket.getOutputStream(), true);
        //input stream from server
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //input stream from client console
        stdIn = new BufferedReader(new InputStreamReader(System.in));

        //start a serverlistener on a new thread
        new Thread(new ServerListener()).start();

        //send user input to server
        while (true) {
            String userInput = stdIn.readLine();
            if (userInput != null) {
                out.println(userInput);
            }
        }
    }

    private class ServerListener implements Runnable {
        public void run() {
            try {
                String fromServer;
                //listen for messages from server
                while ((fromServer = in.readLine()) != null) {
                    //intercept acknowledgement of username acceptance and print connection acceptance msg
                    if ("ack".equalsIgnoreCase(fromServer.trim())){
                        System.out.println("Connection accepted at " + socket.getInetAddress().getHostAddress() + "/" + socket.getPort());
                    //if ClientHandler has signaled a connection closure then exit client
                    } else if ("disc".equalsIgnoreCase(fromServer.trim())) {
                        System.exit(0);
                    }
                    //else print messages from server in console window
                    else {
                        System.out.println(fromServer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Client <host name> <port number>");
            System.exit(1);
        }
        Client client = new Client(args[0], Integer.parseInt(args[1]));
        client.start();
    }
}
