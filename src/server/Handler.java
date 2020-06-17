package server;

import logic.Packet;

import java.util.concurrent.Callable;

public class Handler implements Callable<String> {
    private final Packet packet;
    private final Server server;

    Handler(Packet packet, Server server) {
        this.packet = packet;
        this.server = server;
    }

    @Override
    public String call() {
        return packet.getCommand().execOnServer(server, packet.getArgument(), packet.getUser());
    }
}
