package commands;

import connection.client.Client;
import logic.CollectionManager;
import logic.Packet;
import logic.User;
import connection.server.Server;

import java.io.Serializable;

public abstract class Command implements Serializable {
    final boolean require_login;

    protected Command(boolean require_login) {
        this.require_login = require_login;
    }

    abstract public boolean getRequireLogin();
    abstract public String execOnServer(Server server, Object args, User user);
    abstract public Packet execOnClient(Client client, String ... args);
    public void serverCmd(CollectionManager collectionManager) {}
}
