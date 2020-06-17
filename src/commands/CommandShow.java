package commands;

import connection.client.Client;
import logic.Packet;
import logic.User;
import connection.server.Server;

/**
 * This class of show command. This class just call method from Control Unit.
 */
public class CommandShow extends Command {

    public CommandShow() {
        super(true);
    }

    public boolean getRequireLogin() {
        return require_login;
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (client.getUser().getLoginState()) {
            return new Packet(this, args, client.getUser());
        } else {
            System.err.println("You must login!");
            return null;
        }
    }

    @Override
    public String execOnServer(Server server, Object args, User user) {
        return server.getManager().show();
    }
}
