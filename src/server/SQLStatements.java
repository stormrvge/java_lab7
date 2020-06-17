package server;

import logic.CollectionManager;
import logic.Route;
import logic.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLStatements {
    private final Connection database;
    private final CollectionManager manager;
    private PreparedStatement add_user;
    private PreparedStatement login;
    private PreparedStatement add_route;
    private PreparedStatement rm_route;
    private PreparedStatement get_id;
    private PreparedStatement clear_user;
    private PreparedStatement update_id;

    SQLStatements(Connection database, CollectionManager manager) throws SQLException {
        this.database = database;
        this.manager = manager;
        initStatements();
        load();
    }

    void initStatements() throws SQLException {
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

    void load() throws SQLException {
        ResultSet res = database.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
                .executeQuery("SELECT * FROM collection");
        manager.load(res);
    }
}
