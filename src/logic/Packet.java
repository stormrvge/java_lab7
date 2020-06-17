package logic;

import commands.Command;

import java.io.Serializable;

public class Packet implements Serializable {
    private final Command command;
    private final Object argument;
    private final User user;

    public Packet(Command command, Object argument, User user) {
        this.command = command;
        this.argument = argument;
        this.user = user;
    }

    public Command getCommand() {
        return command;
    }

    public Object getArgument() {
        return argument;
    }

    public User getUser() {
        return user;
    }
}

