package commands;

import client.Client;
import logic.CollectionManager;
import logic.Packet;
import logic.User;
import server.Server;

import java.io.Serializable;

public abstract class Command implements Serializable {
    public boolean getRequireLogin() {
        return false;
    }

    public String execOnServer(Server server, Object args, User user) {
        return null;
    }

    public Packet execOnClient(Client client, String ... args) {
        return new Packet(this, null, null);
    }

    public void serverCmd(CollectionManager collectionManager) {

    }
}
