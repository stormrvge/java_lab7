package logic;

import commands.Command;

import java.io.Serializable;

public class Packet implements Serializable {
    private Command command;
    private Object argument;
    private User user;

    public Packet() {
        this.command = null;
        this.argument = null;
        this.user = null;
    }

    public Packet(User user) {
        this.command = null;
        this.argument = null;
        this.user = user;
    }

    public Packet(Object argument) {
        this.command = null;
        this.argument = argument;
    }

    public Packet(Command command) {
        this.command = command;
        this.argument = null;
    }

    public Packet(Command command, Object argument) {
        this.command = command;
        this.argument = argument;
    }

    public Packet(Command command, User user) {
        this.command = command;
        this.argument = null;
        this.user = user;
    }

    public Packet(Command command, Object argument, User user) {
        this.command = command;
        this.argument = argument;
        this.user = user;
    }


    public void wrap(Command command) {
        this.command = command;
    }

    public void wrap(Object argument) {
        this.argument = argument;
    }

    public void wrap(Command command, Object argument) {
        this.command = command;
        this.argument = argument;
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

