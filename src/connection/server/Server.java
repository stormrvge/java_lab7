package connection.server;

import commands.*;
import connection.server.threads.Reader;
import logic.CollectionManager;
import logic.Invoker;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Properties;

public class Server {
    private final int port;
    private static Socket clientSocket;
    private static ServerSocket server;
    private SQLStatements sqlStatements;
    private String url;
    private String user;
    private String password;

    private final File connectionProperties;
    private final Properties properties;
    private Connection database;
    private static CollectionManager manager;
    private int numOfClients;


    public Server (int port, String user, String password) {
        this.port = port;
        this.user = user;
        this.password = password;
        properties = new Properties();
        connectionProperties = new File("connection.properties");
    }

    public void process() {
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(1000);
            System.out.println("Server started on: " + server.getInetAddress());


            properties.load(new FileInputStream(connectionProperties));
            url = properties.getProperty("url");
            user = properties.getProperty("login");
            password = properties.getProperty("password");

            database = DriverManager.getConnection(url, user, password);

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
                            connection.server.threads.Reader reader = new Reader(this, clientSocket);
                            reader.start();
                        }
                    } catch (SocketTimeoutException ignored) {}
                }
                else if (inputCmd.ready()) {
                    Command command = invoker.createCommand(inputCmd.readLine().trim());
                    try {
                        command.serverCmd(manager);
                    }  catch (NullPointerException ignored) {}
                }
            }
        } catch (IOException | SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            System.out.println("Server closed.");
        }
    }

    public static void closeConnection() {
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