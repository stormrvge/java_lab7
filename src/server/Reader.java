package server;

import commands.Command;
import logic.Packet;
import logic.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.*;

public class Reader extends Thread {
    private final Server server;
    private final Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream objectInputStream;

    Reader(Server server, Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        server.addNumOfClients();
        System.out.println("Server accepted client number: " + server.getNumOfClients());

        if (clientSocket != null) {
            try {
                ExecutorService senderExecutor = Executors.newCachedThreadPool();
                ExecutorService handlerExecutor = Executors.newFixedThreadPool(1);

                while (true) {
                    Packet packet = readMessage();
                    Command commandServer = packet.getCommand();
                    User user = packet.getUser();

                    if (commandServer != null && (server.login(user) || !commandServer.getRequireLogin())) {
                        Handler handler = new Handler(packet, server);
                        Future<String> result = handlerExecutor.submit(handler);

                        String message = result.get();

                        out = new ObjectOutputStream(clientSocket.getOutputStream());
                        Sender sender = new Sender(message, out);
                        senderExecutor.submit(sender);
                    } else {
                        Sender sender = new Sender("U must login!", out);
                        senderExecutor.submit(sender);
                    }
                }
            } catch (NullPointerException | IOException e) {
                System.out.println("Client " + server.getNumOfClients() + " was disconnected!");
                Server.closeConnection();
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + e.getMessage());
                Server.closeConnection();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private Packet readMessage() throws IOException, ClassNotFoundException {
        objectInputStream = new ObjectInputStream(clientSocket.getInputStream());

        return (Packet) objectInputStream.readObject();
    }
}
