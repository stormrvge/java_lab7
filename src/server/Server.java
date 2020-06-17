package server;

import commands.*;
import logic.CollectionManager;
import logic.Route;
import logic.User;

import java.io.*;
import java.net.*;
import java.sql.*;

public class Server {
    private int port;
    private static Socket clientSocket;
    private static ServerSocket server;
    private BufferedReader reader;
    private String user;
    private String password;

    private Connection database;
    private Invoker invoker;
    private static CollectionManager manager;
    private int numOfClients;

    private PreparedStatement add_user;
    private PreparedStatement login;
    private PreparedStatement add_route;
    private PreparedStatement rm_route;
    private PreparedStatement get_id;
    private PreparedStatement clear_user;
    private PreparedStatement update_id;

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

            //database = DriverManager.getConnection("jdbc:postgresql://pg:5432/studs",
                    //user, password);
            database = DriverManager.getConnection("jdbc:postgresql://localhost:5432/lab7",
                    "lab7", "lab7");

            invoker = new Invoker();
            manager = new CollectionManager();

            initStatements();
            load();
            registerCommands(invoker);
            reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Type \"help\" for list of available commands.");
            while (!server.isClosed()) {
                if (!reader.ready()) {
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
                else if (reader.ready()) {
                    Command command = invoker.createCommand(reader.readLine().trim());
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

    private void initStatements() throws SQLException {
        add_user = database.prepareStatement("INSERT INTO users (login, password) VALUES " +
                "(?, ?)");
        login = database.prepareStatement("SELECT * FROM users WHERE login LIKE ? AND password LIKE ?");
        add_route = database.prepareStatement("INSERT INTO collection (id, name, coordinatex, coordinatey, " +
                "locationfromx, locationfromy, locationfromz, locationtox, locationtoy, locationtoz, distance, owner) " +
                "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        rm_route = database.prepareStatement("DELETE FROM collection WHERE id = ? AND owner = ?");
        get_id = database.prepareStatement("SELECT currval('collection_id_seq')");
        clear_user = database.prepareStatement("DELETE FROM collection WHERE owner = ?");
        update_id = database.prepareStatement("UPDATE collection SET name = ?, coordinatex = ?, coordinatey = ?, " +
                "locationfromx = ?, locationfromy = ?, locationfromz = ?, locationtox = ?, locationtoy = ?, " +
                "locationtoz = ?, distance = ? WHERE id = ? AND owner = ?");
    }

    private void registerCommands(Invoker invoker) {
        invoker.register("exit", new CommandExit());
        invoker.register("help", new CommandHelp());
    }

    public void addUser(User user) throws SQLException {
        add_user.setString(1, user.getUsername());
        add_user.setString(2, user.getPassword());
        add_user.executeUpdate();
    }

    public void updateId(int id, Route object, User user) throws SQLException {
        object.update_id(update_id, user, id);
    }

    public void save(Route object, User user) throws SQLException {
        object.add(add_route, user);
    }

    public void remove_route(int id, String owner) throws SQLException {
        rm_route.setInt(1, id);
        rm_route.setString(2, owner);
        rm_route.executeUpdate();
    }

    public boolean login(User user) throws SQLException {
        login.setString(1, user.getUsername());
        login.setString(2, user.getPassword());
        ResultSet res = login.executeQuery();
        return (res.next());
    }

    public int getId() throws SQLException {
        ResultSet res = get_id.executeQuery();
        if (res.next()) return res.getInt(1);
        else return -1;
    }

    public void clearUserCollection(String owner) throws SQLException {
        clear_user.setString(1,owner);
        clear_user.executeUpdate();
    }

    private void load() throws SQLException {
        ResultSet res = database.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                .executeQuery("SELECT * FROM collection");
        manager.load(res);
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

    public int getNumOfClients() {
        return numOfClients;
    }
    public void addNumOfClients() {
        this.numOfClients++;
    }
}