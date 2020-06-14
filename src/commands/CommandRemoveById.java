package commands;

import client.Client;
import logic.CollectionManager;
import logic.Packet;
import logic.User;
import server.Server;

import java.sql.SQLException;

/**
 * This class of remove_by_id command. This class just call method from Control Unit.
 */
public class CommandRemoveById extends Command {
    private boolean require_login = true;

    public boolean getRequireLogin() {
        return require_login;
    }

    @Override
    public String execOnServer(Server server, Object args, User user) {
        try {
            server.remove_route((int) args);
            return "Element with id: " + args + " was removed!";
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return "Element wasn't removed";
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (client.getUser().getLoginState()) {
            Integer id = Integer.parseInt(args[0]);
            return new Packet(this, id);
        } else {
            System.out.println("You must login!");
            return null;
        }
    }
}

