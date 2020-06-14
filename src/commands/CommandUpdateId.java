package commands;

import client.Client;
import logic.Packet;
import logic.Route;
import server.Server;


public class CommandUpdateId extends Command {
    private boolean require_login = true;

    public boolean getRequireLogin() {
        return require_login;
    }

    public String execOnServer(Server server, Object object) {
        Object[] objects = (Object[]) object;
        return server.getManager().update_id((Integer) objects[0], (Route) objects[1]);
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (client.getUser().getLoginState()) {
            Integer id = Integer.parseInt(args[0]);
            Route route = Route.generateObjectUserInput();
            Object[] objects = new Object[] {id, route};
            return new Packet(this, objects, client.getUser());
        } else {
            System.err.println("You must login!");
            return null;
        }
    }
}