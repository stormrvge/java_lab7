package commands;

import connection.client.Client;
import logic.CollectionManager;
import logic.Packet;
import logic.User;
import connection.server.Server;


public class CommandExit extends Command {

    public CommandExit() {
        super(false);
    }

    public boolean getRequireLogin() {
        return require_login;
    }


    public Packet execOnClient(Client client, String ... args) {
        Client.disconnect();
        return null;
    }

    @Override
    public void serverCmd(CollectionManager collectionManager) {
        Server.stopServer();
    }

    @Override
    public String execOnServer(Server server, Object args, User user) {
        return null;
    }
}
