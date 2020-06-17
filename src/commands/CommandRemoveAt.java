package commands;

import client.Client;
import logic.Packet;
import logic.User;
import server.Server;

public class CommandRemoveAt extends Command {
    private boolean require_login = true;

    public boolean getRequireLogin() {
        return require_login;
    }

    @Override
    public String execOnServer(Server server, Object args, User user) {
        return server.getManager().remove_at(server, (Integer) args, user);
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (client.getUser().getLoginState()) {
            Integer index = Integer.parseInt(args[0]);
            return new Packet(this, index, client.getUser());
        } else {
            System.err.println("You must login!");
            return null;
        }
    }
}
