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
        return server.getManager().remove_by_id(server, (int) args, user);
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (client.getUser().getLoginState()) {
            Integer id = Integer.parseInt(args[0]);
            return new Packet(this, id,client.getUser());
        } else {
            System.out.println("You must login!");
            return null;
        }
    }
}

