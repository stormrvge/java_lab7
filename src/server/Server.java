package server;

import commands.*;
import logic.CollectionManager;

import java.io.*;
import java.net.*;
import java.sql.*;

public class Server {
    private final int port;
    private static Socket clientSocket;
    private static ServerSocket server;
    private SQLStatements sqlStatements;
    private final String user;
    private final String password;

    private Connection database;
    private static CollectionManager manager;
    private int numOfClients;


    public Server (int port, String user, String password) {
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public void process() {
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(1000);
            System.out.println("Server started on: " + server.getInetAddress());

            database = DriverManager.getConnection("jdbc:postgresql://pg:5432/studs", user, password);

            Invoker invoker = new Invoker();
            manager = new CollectionManager();
            sqlStatements = new SQLStatements(database, manager);

            registerCommands(invoker);
            BufferedReader inputCmd = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Type \"help\" for list of available commands.");
            while (!server.isClosed()) {
                if (!inputCmd.ready()) {
                    try {
                        clientSocket = server.accept();
                        if (clientSocket != null) {
                            Reader reader = new Reader(this, clientSocket);
                            reader.start();
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.print("");
                    }
                }
                else if (inputCmd.ready()) {
                    Command command = invoker.createCommand(inputCmd.readLine().trim());
                    command.serverCmd(manager);
                }
            }
        } catch (IOException | SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            System.out.println("Server closed.");
        }
    }

    static void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void stopServer() {
        try {
            if (clientSocket != null) {
                closeConnection();
            }
            server.close();
            System.out.println("Server was stopped!");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (NullPointerException e) {
            System.err.println();
        }
    }

    private void registerCommands(Invoker invoker) {
        invoker.register("exit", new CommandExit());
        invoker.register("help", new CommandHelp());
    }

    public static String parseIOException(IOException e) {
        String s = e.getMessage();

        if (s.contains("(") && s.contains(")")) {
            s = s.substring(s.indexOf("(") + 1);
            s = s.substring(0, s.indexOf(")"));
        }
        return s;
    }

    public synchronized CollectionManager getManager() {
        return manager;
    }

    public Connection getDataBase() {
        return database;
    }

    public SQLStatements getSqlStatements() {return sqlStatements;}

    public int getNumOfClients() {
        return numOfClients;
    }

    public void addNumOfClients() {
        this.numOfClients++;
    }
}