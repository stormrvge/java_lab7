package commands;

import connection.client.Client;
import logic.Packet;
import logic.User;
import connection.server.Server;

import java.sql.SQLException;

public class CommandRegister extends Command {

    public CommandRegister() {
        super(false);
    }

    public boolean getRequireLogin() {
        return require_login;
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (args.length != 2) {
            System.err.println("You cant take " + (args.length - 1) + " arguments");
            return null;
        } else {
            User user = new User(args[0], CommandLogin.hash(args[1]));
            return new Packet(this, null, user);
        }
    }

    public String execOnServer(Server server, Object object, User user) {
        try {
            server.getSqlStatements().addUser(user);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "User hasn't been registered!";
        }
        return "User has been registered!";
    }
}

