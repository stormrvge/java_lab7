package commands;

import client.Client;
import logic.Packet;
import logic.User;
import server.Server;

/**
 * This class of print_unique_distance command. This class just call method from Control Unit.
 */
public class CommandPrintUniqueDistance extends Command {
    private final boolean require_login = true;

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
        return server.getManager().print_unique_distance();
    }
}
