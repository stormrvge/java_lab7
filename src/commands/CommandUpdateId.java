package commands;

import client.Client;
import logic.Packet;
import logic.Route;
import logic.User;
import server.Server;


public class CommandUpdateId extends Command {
    private boolean require_login = true;

    public boolean getRequireLogin() {
        return require_login;
    }

    public String execOnServer(Server server, Object object, User user) {
        Object[] objects = (Object[]) object;
        return server.getManager().update_id((Integer) objects[0], (Route) objects[1], server, user);
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        try {
            if (client.getUser().getLoginState()) {
                int id = Integer.parseInt(args[0]);
                Route route = Route.generateObjectUserInput();
                Object[] objects = new Object[] {id, route};
                return new Packet(this, objects, client.getUser());
            } else {
                System.err.println("You must login!");
                return null;
            }
        } catch (NumberFormatException e) {
            System.err.println("Wrong format for argument.");
            return null;
        }
    }
}