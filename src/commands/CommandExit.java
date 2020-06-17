package commands;

import client.Client;
import logic.CollectionManager;
import logic.Packet;
import server.Server;


public class CommandExit extends Command {
    private final boolean require_login = false;

    public boolean getRequireLogin() {
        return require_login;
    }


    public Packet execOnClient(Client client, String ... args) {
        Client.disconnect();
        return new Packet(this, args, null);
    }

    @Override
    public void serverCmd(CollectionManager collectionManager) {
        Server.stopServer();
    }
}
