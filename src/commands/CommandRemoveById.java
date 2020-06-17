package commands;

import connection.client.Client;
import logic.Packet;
import logic.User;
import connection.server.Server;

/**
 * This class of remove_by_id command. This class just call method from Control Unit.
 */
public class CommandRemoveById extends Command {

    public CommandRemoveById() {
        super(true);
    }

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

