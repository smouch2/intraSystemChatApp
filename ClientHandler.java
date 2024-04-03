import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private final String connectionTime;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        //record connection time for AllUsers display
        this.connectionTime = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").format(new Date());
    }

    @Override
    public void run() {
        try {
            //initialize streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //request username
            username = requestUsername();
            //if no username has been provided, return
            if (username == null) {
                return;
            }
            //broadcast welcome message
            Server.broadcastMessage("Welcome " + username);

            String inputLine;
            //listen for client messages
            while ((inputLine = in.readLine()) != null) {
                //check for disconnect command
                if ("Bye".equalsIgnoreCase(inputLine.trim())) {
                    Server.broadcastMessage("Server: Goodbye " + username);
                    closeClientConnection();
                    break;
                //send list of connected users to requesting client
                } else if ("AllUsers".equalsIgnoreCase(inputLine.trim())) {
                    sendActiveUsers();
                } else {
                    //broadcast received message to all users
                    String formattedMessage = username + ": " + inputLine;
                    Server.broadcastMessage(formattedMessage);
                }
            }
            //disconnection message for server log
            Server.printServerMessage(Server.getTimeStamp() + " " + username + " disconnected with a Bye message.");
        } catch (IOException e) {
            System.out.println("Error handling client " + username + ": " + e.getMessage());
        } finally {
            //close streams and remove client from server list
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Server.removeClient(this);
        }
    }

    private String requestUsername() throws IOException {
        //prompt for username and read response
        out.println("Enter your username:");
        String name = in.readLine();
        if (!name.isEmpty()) {
            this.username = name.trim();
            //send ack to client to trigger connection acceptance message
            out.println("ACK");
        }else
        {
            //reject empty username and close client
            out.println("Empty username not allowed.  Closing Connection.");
            closeClientConnection();

        }
        return this.username;
    }

    public void sendMessage(String message) {
        //send timestamped message to client
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        out.println(formatter.format(date) + " " + message);
    }


    private void sendActiveUsers() {
        StringBuilder usersList = new StringBuilder();
        usersList.append("List of the users connected at ").append(new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()))).append("\n");

        int count = 1;
        for (ClientHandler aClient : Server.clients) {
            usersList.append("> ").append(count++).append(") ").append(aClient.username).append(" since ").append(aClient.connectionTime).append("\n");
        }

        //print list to requesting client console
        out.println(usersList.toString());
    }

    private void closeClientConnection() {
        try {
            //send disconnect notice to Client
            out.println("DISC");
            //close streams and socket
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
