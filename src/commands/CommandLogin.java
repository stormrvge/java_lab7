package commands;

import client.Client;
import logic.Packet;
import logic.User;
import server.Server;

import java.sql.SQLException;

public class CommandLogin extends Command {
    private boolean require_login = false;

    public boolean getRequireLogin() {
        return require_login;
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (args.length != 2) {
            System.err.println("Incorrect number of arguments!");
        } else {
            User user = new User(args[0], args[1]);
            client.setUser(user);
            return new Packet(this, user);
        }
        return null;
    }

    public String execOnServer(Server server, Object object, User user) {
        try {
            if (server.login(user)) {
                return "U login successfully!";
            } else {
                return "Incorrect login or password";
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "exception :(";
        }
    }
}
