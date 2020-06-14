package commands;

import client.Client;
import logic.Packet;
import logic.Route;
import logic.User;
import server.Server;

public class CommandAddIfMin extends Command {
    private boolean require_login = true;

    public boolean getRequireLogin() {
        return require_login;
    }

    public String execOnServer(Server server, Object object, User user) {
        return server.getManager().add_if_min(server, (Route) object, user);
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (client.getUser().getLoginState()) {
            Route route = Route.generateObjectUserInput();
            return new Packet(this, route);
        } else {
            System.err.println("You must login!");
            return null;
        }
    }
}