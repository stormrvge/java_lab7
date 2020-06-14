package commands;

import client.Client;
import logic.Packet;
import server.Server;

public class CommandCountByDistance extends Command {
    private boolean require_login = true;

    public boolean getRequireLogin() {
        return require_login;
    }


    public String execOnServer(Server server, Object object) {
        return server.getManager().count_by_distance((Float) object);
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (client.getUser().getLoginState()) {
            Float distance = Float.parseFloat(args[0]);
            return new Packet(this, distance);
        } else {
            System.err.println("You must login!");
            return null;
        }

    }
}
