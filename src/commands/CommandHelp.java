package commands;

import client.Client;
import logic.CollectionManager;
import logic.Packet;
import logic.User;
import server.Server;

/**
 * This class of help command. This class just call method from Control Unit.
 */
public class CommandHelp extends Command {
    private boolean require_login = false;

    public boolean getRequireLogin() {
        return require_login;
    }

    @Override
    public Packet execOnClient(Client client, String ... args) {
        return new Packet(this, args, client.getUser());
    }

    @Override
    public String execOnServer(Server server, Object args, User user) {
        return server.getManager().helpClient();
    }

    @Override
    public void serverCmd(CollectionManager collectionManager, Object args) {
        collectionManager.helpServer();
    }
}
