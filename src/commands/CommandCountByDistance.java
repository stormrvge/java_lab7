package commands;

import connection.client.Client;
import logic.Packet;
import logic.User;
import connection.server.Server;

public class CommandCountByDistance extends Command {

    public CommandCountByDistance() {
        super(true);
    }

    public boolean getRequireLogin() {
        return require_login;
    }


    public String execOnServer(Server server, Object object, User user) {
        return server.getManager().count_by_distance((Float) object);
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        if (client.getUser().getLoginState()) {
            if (args.length != 1) {
                System.err.println("Incorrect number of arguments!");
                return null;
            }
            else {
                Float distance = Float.parseFloat(args[0]);
                return new Packet(this, distance, client.getUser());
            }
        } else {
            System.err.println("You must login!");
            return null;
        }
    }
}
