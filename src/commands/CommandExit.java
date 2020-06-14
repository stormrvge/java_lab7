package commands;

import client.Client;
import logic.CollectionManager;
import logic.Packet;
import server.Server;


public class CommandExit extends Command {
    private boolean require_login = false;

    public boolean getRequireLogin() {
        return require_login;
    }


    public Packet execOnClient(Client client, String ... args) {
        Client.disconnect();
        return new Packet(this, args);
    }

    @Override
    public void serverCmd(CollectionManager collectionManager, Object args) {
        Server.stopServer();
    }
}
